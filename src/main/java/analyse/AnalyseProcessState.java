package analyse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AnalyseProcessState {
    volatile boolean isEnd = false;
    volatile boolean isReady = false;
    volatile int currentMoveNo = 0;
    volatile AtomicBoolean isCompleteAnalyze = new AtomicBoolean(false);
    volatile AtomicReference<MoveMetric> lastMoveMetric = new AtomicReference<>(null);
    final List<MoveMetrics> moveMetricsList = new ArrayList<>();
}