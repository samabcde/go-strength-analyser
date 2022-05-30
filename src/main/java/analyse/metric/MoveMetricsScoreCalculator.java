package analyse.metric;

import analyse.calculate.GameScore;
import analyse.calculate.MoveScore;
import analyse.formula.Formula;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MoveMetricsScoreCalculator {
    private final Formula formula;

    public MoveMetricsScoreCalculator(Formula formula) {
        this.formula = formula;
    }

    public MoveScore calculateMoveScore(MoveMetrics moveMetrics) {
        return formula.calculateMove(moveMetrics);
    }

    public GameScore calculateGameScore(List<MoveMetrics> moveMetricsList) {
        return formula.calculateGame(moveMetricsList);
    }
}
