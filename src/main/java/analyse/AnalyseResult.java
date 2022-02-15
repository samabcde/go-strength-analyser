package analyse;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalyseResult {
    private final List<MoveMetrics> moveMetricsList;
}
