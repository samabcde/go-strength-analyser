package analyse;

import analyse.calculate.CalculateUtils;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class AnalyseResult {
    private final String sgfName;
    private final List<MoveMetrics> moveMetricsList;

    public BigDecimal getBlackStrengthScore() {
        return CalculateUtils.average(moveMetricsList.stream().filter(MoveMetrics::isBlack).map(MoveMetrics::getStrengthScore).collect(Collectors.toList()));
    }

    public BigDecimal getWhiteStrengthScore() {
        return CalculateUtils.average(moveMetricsList.stream().filter(MoveMetrics::isWhite).map(MoveMetrics::getStrengthScore).collect(Collectors.toList()));
    }
}
