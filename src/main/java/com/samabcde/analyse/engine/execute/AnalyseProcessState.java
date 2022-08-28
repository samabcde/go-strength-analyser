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
    private volatile boolean isEnd = false;
    private volatile boolean isErrorOccur = false;
    private volatile boolean isReady = false;
    volatile AtomicReference<AnalyseKey> currentAnalyseKey = new AtomicReference<>(null);
    volatile AtomicBoolean isCompleteAnalyze = new AtomicBoolean(false);
    volatile AtomicReference<MoveMetric> lastMoveMetric = new AtomicReference<>(null);
    volatile AtomicReference<MoveInfo> lastMoveInfo = new AtomicReference<>(null);
    public final List<MoveMetrics> moveMetricsList = new ArrayList<>();
    public final List<MoveInfo> moveInfoList = new ArrayList<>();

    public synchronized void ready() {
        this.isReady = true;
        notifyAll();
    }

    public synchronized void end() {
        this.isEnd = true;
        notifyAll();
    }

    public synchronized void errorOccur() {
        this.isErrorOccur = true;
        notifyAll();
    }

    public synchronized boolean isReady() {
        return this.isReady;
    }

    public synchronized boolean isEnd() {
        return this.isEnd;
    }

    public synchronized boolean isErrorOccur() {
        return this.isErrorOccur;
    }

    public synchronized void waitUntilReady() {
        while (!this.isReady) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void waitUntilEndOrErrorOccur() {
        while (!this.isEnd && !this.isErrorOccur) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
}
