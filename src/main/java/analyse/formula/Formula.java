package analyse.formula;

import analyse.metric.MoveMetric;
import analyse.metric.MoveMetrics;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

public interface Formula {
    Version version();

    BigDecimal calculateMove(MoveMetrics moveMetrics, Function<MoveMetric, BigDecimal> metricExtractor);

    BigDecimal calculateGame(List<MoveMetrics> moveMetricsList);
}
