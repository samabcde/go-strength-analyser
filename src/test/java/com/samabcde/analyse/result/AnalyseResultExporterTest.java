package com.samabcde.analyse.result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samabcde.analyse.calculate.GameScore;
import com.samabcde.analyse.calculate.MoveScore;
import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.core.AnalyseMetadata;
import com.samabcde.analyse.core.AnalyseTarget;
import com.samabcde.analyse.core.ApplicationConfig;
import com.samabcde.analyse.core.SgfAnalysisConfig;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetrics;
import com.samabcde.analyse.metric.MoveVariation;
import com.samabcde.analyse.sgf.SgfParser;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyseResultExporterTest {
    private static final String SGF = "(;GM[1]FF[4]CA[UTF-8]AP[Sabaki:0.52.0]RU[japanese]KM[6.5]SZ[19]DT[2022-02-20];B[pd];W[dp];B[pq])";

    @Mock
    ApplicationConfig applicationConfig;

    AnalyseResultExporter analyseResultExporter;
    SgfAnalysisConfig sgfAnalysisConfig;

    @TempDir
    Path anotherTempDir;

    @BeforeEach
    void setup() {
        sgfAnalysisConfig = new SgfAnalysisConfig();
        sgfAnalysisConfig.setMaxVariations(2);
        analyseResultExporter = new AnalyseResultExporter(applicationConfig, sgfAnalysisConfig);
        assertTrue(anotherTempDir.resolve("sgfFolderPath").toFile().mkdir());
        assertTrue(anotherTempDir.resolve("outputFileFolder").toFile().mkdir());
    }

    @Test
    void exportAddsVersionedMetricsAndCompleteVariationBranches() throws Exception {
        when(applicationConfig.getSgfFolderPath()).thenReturn(anotherTempDir.resolve("sgfFolderPath"));
        when(applicationConfig.getOutputFileFolder()).thenReturn(anotherTempDir.resolve("outputFileFolder").toString() + File.separator);
        AnalyseMetadata metadata = new AnalyseMetadata("dummy", SGF, 1);
        MoveMetric rootMetric = metric(0, AnalyseTarget.CANDIDATE, "", "0.5", "0",
                List.of(
                        variation(0, "Q16", "D4", "Q3"),
                        variation(1, "D4", "Q16"),
                        variation(2, "K10", "K4")));
        List<MoveMetrics> moveMetrics = List.of(
                moveMetrics(1, "Q16", "0.4", "-2", List.of(
                        variation(0, "D4", "Q3"),
                        variation(1, "Q16", "D4"),
                        variation(2, "K10", "K4"))),
                moveMetrics(2, "D4", "0.4", "3", List.of(
                        variation(0, "Q3", "D4"),
                        variation(1, "K10", "Q16"),
                        variation(2, "D16", "Q4"))),
                moveMetrics(3, "Q3", "0.3", "-1", List.of()));
        AnalyseResult analyseResult = AnalyseResult.builder()
                .metadata(metadata)
                .moveMetricsList(moveMetrics)
                .rootMetric(rootMetric)
                .gameScore(new GameScore(BigDecimal.ONE, BigDecimal.ONE))
                .build();

        analyseResultExporter.export(analyseResult);

        Path sgfPath = anotherTempDir.resolve("sgfFolderPath").resolve("dummy_with_analyse.sgf");
        assertTrue(sgfPath.toFile().exists());
        String savedSgf = Files.readString(sgfPath);
        assertTrue(savedSgf.contains("SBKV[60.00]"), savedSgf);
        assertTrue(anotherTempDir.resolve("outputFileFolder").resolve("dummy.txt").toFile().exists());
        assertTrue(anotherTempDir.resolve("outputFileFolder").resolve("dummy.json").toFile().exists());

        Game exportedGame = SgfParser.parseGame(sgfPath);
        GameNode firstMove = exportedGame.getFirstMove();
        List<JsonNode> tags = readAnalysisTags(savedSgf);
        JsonNode firstMetrics = tags.stream().filter(tag -> tag.get("kind").asText().equals("metrics")).findFirst().orElseThrow();
        assertEquals("go-strength-analyser", firstMetrics.get("schema").asText());
        assertEquals(1, firstMetrics.get("version").asInt());
        assertEquals("metrics", firstMetrics.get("kind").asText());
        assertEquals(1, firstMetrics.get("moveNo").asInt());
        assertEquals("B", firstMetrics.get("mover").asText());
        assertEquals(0.6, firstMetrics.get("winrateBlack").asDouble(), 0.000001);
        assertEquals(0.1, firstMetrics.get("deltaWinrateMover").asDouble(), 0.000001);
        assertEquals(2.0, firstMetrics.get("deltaScoreLeadMover").asDouble(), 0.000001);
        assertTrue(firstMetrics.get("followsBest").asBoolean());

        assertEquals(2, exportedGame.getRootNode().getChildren().size());
        assertEquals(2, firstMove.getChildren().size());
        JsonNode variationMetadata = tags.stream().filter(tag -> tag.get("kind").asText().equals("bestVariation")
                        && tag.get("moveNo").asInt() == 1)
                .findFirst().orElseThrow();
        assertEquals("bestVariation", variationMetadata.get("kind").asText());
        assertEquals(1, variationMetadata.get("moveNo").asInt());
        GameNode bestVariation = exportedGame.getRootNode().getChildren().stream()
                .filter(node -> "pd".equals(node.getProperty("B")))
                .findFirst().orElseThrow();
        assertNotNull(bestVariation.getNextNode());
        assertNotNull(bestVariation.getNextNode().getNextNode());

        JsonNode secondMoveMetrics = tags.stream().filter(tag -> tag.get("kind").asText().equals("metrics"))
                .skip(1).findFirst().orElseThrow();
        assertTrue(secondMoveMetrics.get("followsBest").asBoolean());
        assertEquals(0.2, secondMoveMetrics.get("deltaWinrateMover").asDouble(), 0.000001);
    }

    @Test
    void exportRejectsAnalysisGeneratedForDifferentMoveCoordinates() {
        AnalyseResult analyseResult = AnalyseResult.builder()
                .metadata(new AnalyseMetadata("mismatched", SGF, 1))
                .moveMetricsList(List.of(
                        moveMetrics(1, "Q16", "0.4", "-2", List.of()),
                        moveMetrics(2, "Q4", "0.4", "3", List.of()),
                        moveMetrics(3, "Q3", "0.3", "-1", List.of())))
                .gameScore(new GameScore(BigDecimal.ONE, BigDecimal.ONE))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> analyseResultExporter.export(analyseResult));

        assertTrue(exception.getMessage().contains("move 2: analysis has Q4, SGF requires D4"));
        assertTrue(exception.getMessage().contains("Re-run the analysis"));
    }

    private static List<JsonNode> readAnalysisTags(String sgf) throws Exception {
        Matcher matcher = Pattern.compile("XGSA\\[(\\{[^\\]]*})\\]").matcher(sgf);
        ObjectMapper objectMapper = new ObjectMapper();
        List<JsonNode> tags = new ArrayList<>();
        while (matcher.find()) {
            tags.add(objectMapper.readTree(matcher.group(1)));
        }
        return tags;
    }

    private static MoveMetrics moveMetrics(int moveNo, String move, String winrate, String scoreLead, List<MoveVariation> variations) {
        return MoveMetrics.builder()
                .moveNo(moveNo)
                .moveScore(new MoveScore(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE))
                .ai(metric(moveNo, AnalyseTarget.AI, move, "0.5", "0", List.of()))
                .candidate(metric(moveNo, AnalyseTarget.CANDIDATE, move, winrate, scoreLead, variations))
                .pass(metric(moveNo, AnalyseTarget.PASS, "pass", "0.5", "0", List.of()))
                .build();
    }

    private static MoveMetric metric(int moveNo, AnalyseTarget target, String move, String winrate, String scoreLead, List<MoveVariation> variations) {
        return MoveMetric.builder()
                .analyseKey(new AnalyseKey(target, moveNo, move))
                .winrate(new BigDecimal(winrate))
                .scoreLead(new BigDecimal(scoreLead))
                .bestMove(variations.isEmpty() ? move : variations.getFirst().firstMove())
                .bestVariations(variations)
                .build();
    }

    private static MoveVariation variation(int order, String... moves) {
        return new MoveVariation(order, List.of(moves));
    }
}
