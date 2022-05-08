package analyse.formula;

import analyse.metric.MoveMetric;
import analyse.metric.MoveMetrics;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.function.Function;

@Component
public class FormulaV1 implements Formula {
    @Override
    public Version version() {
        return Version.V1;
    }

    @Override
    public BigDecimal calculateMove(MoveMetrics moveMetrics, Function<MoveMetric, BigDecimal> metricExtractor) {
        return calculateStrengthScore(metricExtractor.apply(moveMetrics.getCandidate()),
                metricExtractor.apply(moveMetrics.getPass()),
                metricExtractor.apply(moveMetrics.getAi()));
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
    public BigDecimal calculateGame(List<MoveMetrics> moveMetricsList) {
        return null;
    }
}
