package analyse.result;

import analyse.calculate.CalculateUtils;
import analyse.core.AnalyseMetadata;
import analyse.metric.MoveMetrics;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

@Data
@Builder
public class AnalyseResult {
    private final AnalyseMetadata metadata;
    private final String sgfName;
    private final List<MoveMetrics> moveMetricsList;

    public BigDecimal getBlackStrengthScore() {
        BigDecimal winrateWeightSum = moveMetricsList.stream().filter(MoveMetrics::isBlack).map(MoveMetrics::getWinrateWeight).reduce(BigDecimal::add).get();
        BigDecimal winrateStrengthScore = moveMetricsList.stream().filter(MoveMetrics::isBlack).map(MoveMetrics::getWeightedWinrateStrengthScore).reduce(BigDecimal::add).get().divide(winrateWeightSum, MathContext.DECIMAL64);

        BigDecimal scoreLeadWeightSum = moveMetricsList.stream().filter(MoveMetrics::isBlack).map(MoveMetrics::getScoreLeadWeight).reduce(BigDecimal::add).get();
        BigDecimal scoreLeadStrengthScore = moveMetricsList.stream().filter(MoveMetrics::isBlack).map(MoveMetrics::getWeightedScoreLeadStrengthScore).reduce(BigDecimal::add).get().divide(scoreLeadWeightSum, MathContext.DECIMAL64);
        return CalculateUtils.average(List.of(winrateStrengthScore, scoreLeadStrengthScore));
    }

    public BigDecimal getWhiteStrengthScore() {
        BigDecimal winrateWeightSum = moveMetricsList.stream().filter(MoveMetrics::isWhite).map(MoveMetrics::getWinrateWeight).reduce(BigDecimal::add).get();
        BigDecimal winrateStrengthScore = moveMetricsList.stream().filter(MoveMetrics::isWhite).map(MoveMetrics::getWeightedWinrateStrengthScore).reduce(BigDecimal::add).get().divide(winrateWeightSum, MathContext.DECIMAL64);

        BigDecimal scoreLeadWeightSum = moveMetricsList.stream().filter(MoveMetrics::isWhite).map(MoveMetrics::getScoreLeadWeight).reduce(BigDecimal::add).get();
        BigDecimal scoreLeadStrengthScore = moveMetricsList.stream().filter(MoveMetrics::isWhite).map(MoveMetrics::getWeightedScoreLeadStrengthScore).reduce(BigDecimal::add).get().divide(scoreLeadWeightSum, MathContext.DECIMAL64);
        return CalculateUtils.average(List.of(winrateStrengthScore, scoreLeadStrengthScore));
    }
}
