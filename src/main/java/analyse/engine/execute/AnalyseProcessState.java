package analyse.engine.execute;

import analyse.core.AnalyseKey;
import analyse.info.MoveInfo;
import analyse.metric.MoveMetric;
import analyse.metric.MoveMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AnalyseProcessState {
    public volatile boolean isEnd = false;
    public volatile boolean isErrorOccur = false;
    volatile boolean isReady = false;
    //    volatile int currentMoveNo = 0;
    volatile AtomicReference<AnalyseKey> currentAnalyseKey = new AtomicReference<>(null);
    volatile AtomicBoolean isCompleteAnalyze = new AtomicBoolean(false);
    volatile AtomicReference<MoveMetric> lastMoveMetric = new AtomicReference<>(null);
    volatile AtomicReference<MoveInfo> lastMoveInfo = new AtomicReference<>(null);
    public final List<MoveMetrics> moveMetricsList = new ArrayList<>();
    public final List<MoveInfo> moveInfoList = new ArrayList<>();
}