package com.samabcde.analyse.metric;

import com.samabcde.analyse.calculate.GameScore;
import com.samabcde.analyse.calculate.MoveScore;
import com.samabcde.analyse.formula.Formula;
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
