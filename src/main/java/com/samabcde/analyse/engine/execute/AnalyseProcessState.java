package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.info.MoveInfo;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AnalyseProcessState {
    public volatile boolean isEnd = false;
    public volatile boolean isErrorOccur = false;
    volatile boolean isReady = false;
    volatile AtomicReference<AnalyseKey> currentAnalyseKey = new AtomicReference<>(null);
    volatile AtomicBoolean isCompleteAnalyze = new AtomicBoolean(false);
    volatile AtomicReference<MoveMetric> lastMoveMetric = new AtomicReference<>(null);
    volatile AtomicReference<MoveInfo> lastMoveInfo = new AtomicReference<>(null);
    public final List<MoveMetrics> moveMetricsList = new ArrayList<>();
    public final List<MoveInfo> moveInfoList = new ArrayList<>();
}