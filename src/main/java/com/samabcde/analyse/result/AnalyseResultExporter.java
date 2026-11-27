package com.samabcde.analyse.result;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.samabcde.analyse.core.ApplicationConfig;
import com.samabcde.analyse.core.SgfAnalysisConfig;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetrics;
import com.samabcde.analyse.metric.MoveVariation;
import com.samabcde.analyse.sgf.SgfParser;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class AnalyseResultExporter {

    private final ApplicationConfig applicationConfig;
    private final SgfAnalysisConfig sgfAnalysisConfig;
    private final ObjectMapper objectMapper;
    private final ObjectMapper sgfObjectMapper;

    public AnalyseResultExporter(ApplicationConfig applicationConfig, SgfAnalysisConfig sgfAnalysisConfig) {
        this.applicationConfig = applicationConfig;
        this.sgfAnalysisConfig = sgfAnalysisConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.sgfObjectMapper = new ObjectMapper();
    }

    public void export(AnalyseResult analyseResult) {
        exportToText(analyseResult);
        exportToJson(analyseResult);
        exportToSgf(analyseResult);
    }

    private void exportToSgf(AnalyseResult analyseResult) {
        Game game = SgfParser.parseGame(analyseResult.getMetadata().getSgf());
        GameNode current = game.getFirstMove();
        GameNode variationParent = game.getRootNode();
        MoveMetric previousPosition = analyseResult.getRootMetric();
        int boardSize = Integer.parseInt(game.getProperty("SZ", "19"));
        List<String> playedMoves = SgfParser.toMoveCommands(game);
        List<MoveMetrics> moveMetricsList = analyseResult.getMoveMetricsList();
        for (MoveMetrics moveMetric : moveMetricsList) {
            if (current == null) {
                throw new IllegalStateException("current is null, move no" + moveMetric.getMoveNo());
            }
            if (moveMetric.getMoveNo() < 1 || moveMetric.getMoveNo() > playedMoves.size()) {
                throw new IllegalStateException("Analysis move number " + moveMetric.getMoveNo()
                        + " is outside the SGF main line (1-" + playedMoves.size() + ")");
            }
            boolean blackMove = current.getProperties().containsKey("B");
            if (!blackMove && !current.getProperties().containsKey("W")) {
                throw new IllegalStateException("No move property for move no " + moveMetric.getMoveNo());
            }
            String actualMove = moveMetric.getCandidate().getAnalyseKey().move();
            String expectedMove = playedMoves.get(moveMetric.getMoveNo() - 1).split(" ")[1];
            if (!actualMove.equalsIgnoreCase(expectedMove)) {
                throw new IllegalStateException("Analysis move does not match SGF at move " + moveMetric.getMoveNo()
                        + ": analysis has " + actualMove + ", SGF requires " + expectedMove
                        + ". Re-run the analysis to regenerate metrics for this SGF.");
            }
            MoveVariation bestVariation = getBestVariation(previousPosition);
            Boolean followsBest = bestVariation == null || bestVariation.firstMove() == null
                    ? null
                    : actualMove.equalsIgnoreCase(bestVariation.firstMove());

            BigDecimal deltaWinrateMover = null;
            BigDecimal deltaScoreLeadMover = null;
            if (previousPosition != null) {
                BigDecimal deltaWinrateBlack = moveMetric.getBlackWinrate().subtract(previousPosition.getBlackWinrate());
                BigDecimal deltaScoreLeadBlack = moveMetric.getBlackScoreLead().subtract(previousPosition.getBlackScoreLead());
                deltaWinrateMover = blackMove ? deltaWinrateBlack : deltaWinrateBlack.negate();
                deltaScoreLeadMover = blackMove ? deltaScoreLeadBlack : deltaScoreLeadBlack.negate();
            }

            current.addProperty("SBKV", formatPercentageWithoutSign(moveMetric.getBlackWinrate()));
            current.addProperty("XGSA", serializeSgfProperty(new MetricTag(
                    "go-strength-analyser",
                    1,
                    "metrics",
                    moveMetric.getMoveNo(),
                    blackMove ? "B" : "W",
                    moveMetric.getBlackWinrate(),
                    moveMetric.getBlackScoreLead(),
                    deltaWinrateMover,
                    deltaScoreLeadMover,
                    followsBest)));
            if (previousPosition != null) {
                addBestVariationBranches(variationParent, previousPosition.getBestVariations(), moveMetric.getMoveNo(),
                        blackMove ? "B" : "W", boardSize);
            }
            variationParent = current;
            previousPosition = moveMetric.getCandidate();
            current = current.getNextNode();
        }
        game.saveToFile(applicationConfig.getSgfFolderPath().resolve(analyseResult.getSgfName() + "_with_analyse" + ".sgf"));
    }

    private void addBestVariationBranches(GameNode parent, List<MoveVariation> variations, int moveNo, String firstColor, int boardSize) {
        int maxVariations = sgfAnalysisConfig.getMaxVariations();
        if (maxVariations == 0) {
            return;
        }
        variations.stream()
                .sorted(Comparator.comparingInt(MoveVariation::order))
                .limit(maxVariations)
                .forEach(variation -> addVariationBranch(parent, variation, moveNo, firstColor, boardSize));
    }

    private void addVariationBranch(GameNode parent, MoveVariation variation, int moveNo, String firstColor, int boardSize) {
        if (variation.moves().isEmpty() || variation.moves().getFirst().equalsIgnoreCase("resign")) {
            return;
        }
        GameNode current = new GameNode(parent);
        current.addProperty(firstColor, toSgfMove(variation.moves().getFirst(), boardSize));
        current.addProperty("XGSA", serializeSgfProperty(new VariationTag(
                "go-strength-analyser", 1, "bestVariation", moveNo, variation.order() + 1)));
        parent.addChild(current);

        String color = firstColor.equals("B") ? "W" : "B";
        for (int i = 1; i < variation.moves().size(); i++) {
            String move = variation.moves().get(i);
            if (move.equalsIgnoreCase("resign")) {
                break;
            }
            GameNode next = new GameNode(current);
            next.addProperty(color, toSgfMove(move, boardSize));
            current.addChild(next);
            current = next;
            color = color.equals("B") ? "W" : "B";
        }
    }

    private static MoveVariation getBestVariation(MoveMetric moveMetric) {
        if (moveMetric == null || moveMetric.getBestVariations().isEmpty()) {
            return null;
        }
        return moveMetric.getBestVariations().stream()
                .min(Comparator.comparingInt(MoveVariation::order))
                .orElse(null);
    }

    private static String toSgfMove(String gtpMove, int boardSize) {
        if (gtpMove.equalsIgnoreCase("pass")) {
            return "";
        }
        if (gtpMove.length() < 2) {
            throw new IllegalArgumentException("Invalid KataGo move: " + gtpMove);
        }
        String columns = "ABCDEFGHJKLMNOPQRSTUVWXYZ";
        int x = columns.indexOf(Character.toUpperCase(gtpMove.charAt(0)));
        int row;
        try {
            row = Integer.parseInt(gtpMove.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid KataGo move: " + gtpMove, e);
        }
        int y = boardSize - row;
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) {
            throw new IllegalArgumentException("KataGo move is outside the " + boardSize + "x" + boardSize + " board: " + gtpMove);
        }
        return new String(new char[]{(char) ('a' + x), (char) ('a' + y)});
    }

    private String serializeSgfProperty(Object value) {
        try {
            return sgfObjectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize SGF analysis property", e);
        }
    }

    private record MetricTag(String schema, int version, String kind, int moveNo, String mover,
                             BigDecimal winrateBlack, BigDecimal scoreLeadBlack, BigDecimal deltaWinrateMover,
                             BigDecimal deltaScoreLeadMover, Boolean followsBest) {
    }

    private record VariationTag(String schema, int version, String kind, int moveNo, int rank) {
    }

    private void exportToText(AnalyseResult analyseResult) {
        String outputFilePath = applicationConfig.getOutputFileFolder() + analyseResult.getSgfName() + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath))) {
            List<MoveMetrics> moveMetricsList = analyseResult.getMoveMetricsList();
            writer.println("Strength Score:");
            writer.println("Black:" + analyseResult.getBlackStrengthScore());
            writer.println("White:" + analyseResult.getWhiteStrengthScore());
            writer.println("Win Rate:");
            List<MoveMetrics> winrateChanges = new ArrayList<>(moveMetricsList);
            for (MoveMetrics moveMetric : moveMetricsList) {
                writer.println(
                        moveMetric.getMoveNo() + "\t" + moveMetric.getBlackWinratePercentage() + "\t" + moveMetric.getBlackScoreLead() + "\t" + moveMetric.details());
            }

            winrateChanges.sort(Comparator.comparing(MoveMetrics::getRateChange));
            writer.println("Bad move");
            for (int i = 0; i < 3; i++) {
                MoveMetrics winrate = winrateChanges.get(i);
                writer.print(winrate.getMoveNo() + "("
                        + formatPercentage(winrate.getRateChange()) + ")");
                if (i < 2) {
                    writer.print(", ");
                }
            }
            writer.println("");
            writer.println("Good move");
            for (int i = winrateChanges.size() - 1; i > winrateChanges.size() - 4; i--) {
                MoveMetrics winrate = winrateChanges.get(i);
                writer.print(winrate.getMoveNo() + "("
                        + formatPercentage(winrate.getRateChange()) + ")");
                if (i > winrateChanges.size() - 3) {
                    writer.print(", ");
                }
            }
            writer.flush();
            log.info("Exported to :{}", outputFilePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void exportToJson(AnalyseResult analyseResult) {
        String outputFilePath = applicationConfig.getOutputFileFolder() + analyseResult.getSgfName() + ".json";
        try (FileWriter fileWriter = new FileWriter(outputFilePath)) {
            objectMapper.writeValue(fileWriter, analyseResult);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String formatPercentage(BigDecimal value) {
        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMinimumFractionDigits(2);
        defaultFormat.setMaximumFractionDigits(2);
        return defaultFormat.format(value);
    }

    private static String formatPercentageWithoutSign(BigDecimal value) {
        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMinimumFractionDigits(2);
        defaultFormat.setMaximumFractionDigits(2);
        return defaultFormat.format(value).replaceAll("%", "");
    }

}
