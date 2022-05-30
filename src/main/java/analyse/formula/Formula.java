package analyse.formula;

import analyse.calculate.GameScore;
import analyse.calculate.MoveScore;
import analyse.metric.MoveMetrics;

import java.util.List;

public interface Formula {
    Version version();

    MoveScore calculateMove(MoveMetrics moveMetrics);

    GameScore calculateGame(List<MoveMetrics> moveMetricsList);
}
