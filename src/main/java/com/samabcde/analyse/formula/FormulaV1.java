package com.samabcde.analyse.formula;

import com.samabcde.analyse.calculate.CalculateUtils;
import com.samabcde.analyse.calculate.GameScore;
import com.samabcde.analyse.calculate.MoveScore;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetrics;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class FormulaV1 implements Formula {
    private static final BigDecimal BASELINE = new BigDecimal("10000");

    @Override
    public Version version() {
        return Version.V1;
    }

    private BigDecimal calculateMove(MoveMetrics moveMetrics, Function<MoveMetric, BigDecimal> metricExtractor) {
        return calculateStrengthScore(metricExtractor.apply(moveMetrics.getCandidate()),
                metricExtractor.apply(moveMetrics.getPass()),
                metricExtractor.apply(moveMetrics.getAi()));
    }

    @Override
    public MoveScore calculateMove(MoveMetrics moveMetrics) {
        BigDecimal winrateScore = this.calculateMove(moveMetrics, MoveMetric::getRespectiveWinrate);
        BigDecimal scoreLeadScore = this.calculateMove(moveMetrics, MoveMetric::getRespectiveScoreLead);
        BigDecimal integratedScore = CalculateUtils.average(List.of(winrateScore, scoreLeadScore));
        return new MoveScore(winrateScore, scoreLeadScore, integratedScore);
    }

    // y = (2x - x1 - x2)/(x2 - x1)
    private BigDecimal calculateStrengthScore(BigDecimal x, BigDecimal x1, BigDecimal x2) {
        if (x1.compareTo(x2) >= 0) {
            return BigDecimal.ONE;
        }
        if (x.compareTo(x2) > 0) {
            x = x2;
        }
        if (x.compareTo(x1) < 0) {
            x = x1;
        }
        BigDecimal y = (x.multiply(BigDecimal.valueOf(2)).subtract(x1).subtract(x2)).divide(x2.subtract(x1), MathContext.DECIMAL64);
        return y;
    }

    @Override
    public GameScore calculateGame(List<MoveMetrics> moveMetricsList) {
        BigDecimal blackScore = calculateSide(moveMetricsList, MoveMetrics::isBlack);
        BigDecimal whiteScore = calculateSide(moveMetricsList, MoveMetrics::isWhite);
        return new GameScore(blackScore, whiteScore);
    }

    private BigDecimal calculateSide(List<MoveMetrics> moveMetricsList, Predicate<MoveMetrics> sideFilter) {
        BigDecimal winrateWeightSum = moveMetricsList.stream().filter(sideFilter).map(MoveMetrics::getWinrateWeight).reduce(BigDecimal::add).get();
        BigDecimal winrateStrengthScore = moveMetricsList.stream().filter(sideFilter).map(MoveMetrics::getWeightedWinrateStrengthScore).reduce(BigDecimal::add).get().divide(winrateWeightSum, MathContext.DECIMAL64);

        BigDecimal scoreLeadWeightSum = moveMetricsList.stream().filter(sideFilter).map(MoveMetrics::getScoreLeadWeight).reduce(BigDecimal::add).get();
        BigDecimal scoreLeadStrengthScore = moveMetricsList.stream().filter(sideFilter).map(MoveMetrics::getWeightedScoreLeadStrengthScore).reduce(BigDecimal::add).get().divide(scoreLeadWeightSum, MathContext.DECIMAL64);
        return CalculateUtils.average(List.of(winrateStrengthScore, scoreLeadStrengthScore)).multiply(BASELINE);
    }
}
