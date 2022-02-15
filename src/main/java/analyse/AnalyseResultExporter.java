package analyse;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Log
@Component
public class AnalyseResultExporter {
    public void export(AnalyseResult analyseResult) {
        List<MoveMetrics> moveMetricsList = analyseResult.getMoveMetricsList();
        log.info("Win Rate:");
        List<MoveMetrics> winrateChanges = MoveMetrics.calculateWinrateChanges(moveMetricsList);
        for (MoveMetrics moveMetric : moveMetricsList) {
            log.info(
                    moveMetric.getMoveNo() + "\t" + moveMetric.getBlackWinrate() + "\t" + moveMetric.getBlackScoreMean());
        }

        winrateChanges.sort(Comparator.comparing(MoveMetrics::getRateChange));
        log.info("Bad move");
        for (int i = 0; i < 3; i++) {
            MoveMetrics winrate = winrateChanges.get(i);
            System.out.print(winrate.getMoveNo() + 1 + "("
                    + winrate.getRateChange().setScale(2, RoundingMode.HALF_EVEN) + "%)");
            if (i < 2) {
                System.out.print(", ");
            }
        }
        log.info("");
        log.info("Good move");
        for (int i = winrateChanges.size() - 1; i > winrateChanges.size() - 4; i--) {
            MoveMetrics winrate = winrateChanges.get(i);
            System.out.print(winrate.getMoveNo() + 1 + "("
                    + winrate.getRateChange().setScale(2, RoundingMode.HALF_EVEN) + "%)");
            if (i > winrateChanges.size() - 3) {
                System.out.print(", ");
            }
        }
    }
}
