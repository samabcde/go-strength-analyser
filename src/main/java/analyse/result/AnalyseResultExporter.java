package analyse.result;

import analyse.core.ApplicationConfig;
import analyse.metric.MoveMetrics;
import analyse.sgf.SgfParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class AnalyseResultExporter {

    private final ApplicationConfig applicationConfig;

    public AnalyseResultExporter(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public void export(AnalyseResult analyseResult) {
        exportToText(analyseResult);
        exportToJson(analyseResult);
        exportToSgf(analyseResult);
    }

    private void exportToSgf(AnalyseResult analyseResult) {
        Game game = SgfParser.parseGame(analyseResult.getMetadata().getSgf());
        GameNode current = game.getFirstMove();
        List<MoveMetrics> moveMetricsList = analyseResult.getMoveMetricsList();
        for (MoveMetrics moveMetric : moveMetricsList) {
            if (current == null) {
                throw new IllegalStateException("current is null, move no" + moveMetric.getMoveNo());
            }
            current.addProperty("SBKV", formatPercentageWithoutSign(moveMetric.getBlackWinrate()));
            current = current.getNextNode();
        }
        game.saveToFile(applicationConfig.getSgfFolderPath().resolve(analyseResult.getSgfName() + "_with_analyse" + ".sgf"));
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
            throw new RuntimeException(e);
        }
    }

    private void exportToJson(AnalyseResult analyseResult) {
        String outputFilePath = applicationConfig.getOutputFileFolder() + analyseResult.getSgfName() + ".json";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try (FileWriter fileWriter = new FileWriter(outputFilePath)) {
            objectMapper.writeValue(fileWriter, analyseResult);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
