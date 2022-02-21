package analyse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
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
        String outputFilePath = applicationConfig.getOutputFileFolder() + analyseResult.getSgfName() + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(applicationConfig.getOutputFileFolder() + analyseResult.getSgfName() + ".txt"))) {
            List<MoveMetrics> moveMetricsList = analyseResult.getMoveMetricsList();
            writer.println("Win Rate:");
            List<MoveMetrics> winrateChanges = MoveMetrics.calculateWinrateChanges(moveMetricsList);
            for (MoveMetrics moveMetric : moveMetricsList) {
                writer.println(
                        moveMetric.getMoveNo() + "\t" + moveMetric.getBlackWinrate() + "\t" + moveMetric.getBlackScoreMean());
            }

            winrateChanges.sort(Comparator.comparing(MoveMetrics::getRateChange));
            writer.println("Bad move");
            for (int i = 0; i < 3; i++) {
                MoveMetrics winrate = winrateChanges.get(i);
                writer.print(winrate.getMoveNo() + 1 + "("
                        + winrate.getRateChange().setScale(2, RoundingMode.HALF_EVEN) + "%)");
                if (i < 2) {
                    writer.print(", ");
                }
            }
            writer.println("");
            writer.println("Good move");
            for (int i = winrateChanges.size() - 1; i > winrateChanges.size() - 4; i--) {
                MoveMetrics winrate = winrateChanges.get(i);
                writer.print(winrate.getMoveNo() + 1 + "("
                        + winrate.getRateChange().setScale(2, RoundingMode.HALF_EVEN) + "%)");
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
}
