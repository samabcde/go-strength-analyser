package analyse.metric;

import analyse.calculate.CalculateUtils;
import analyse.formula.Formula;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MoveMetricsScoreCalculator {
    private final Formula formula;

    public MoveMetricsScoreCalculator(Formula formula) {
        this.formula = formula;
    }

    public MoveMetrics calculateScore(MoveMetrics moveMetrics) {
        moveMetrics.setScoreLeadStrengthScore(formula.calculateMove(moveMetrics, MoveMetric::getRespectiveScoreLead));
        moveMetrics.setWinrateStrengthScore(formula.calculateMove(moveMetrics, MoveMetric::getRespectiveWinrate));
        moveMetrics.setStrengthScore(CalculateUtils.average(List.of(moveMetrics.getWinrateStrengthScore(), moveMetrics.getScoreLeadStrengthScore())));
        return moveMetrics;
    }
}
