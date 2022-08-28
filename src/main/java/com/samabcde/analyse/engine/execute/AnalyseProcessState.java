package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.info.MoveInfo;
import com.samabcde.analyse.metric.MoveMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AnalyseProcessState {
    private boolean isEnd = false;
    private boolean isErrorOccur = false;
    private boolean isReady = false;
    private final AtomicReference<AnalyseKey> currentAnalyseKey = new AtomicReference<>(null);
    private final AtomicBoolean isCompleteAnalyze = new AtomicBoolean(false);
    private final AtomicReference<MoveInfo> lastMoveInfo = new AtomicReference<>(null);
    public final List<MoveMetrics> moveMetricsList = new ArrayList<>();
    public final List<MoveInfo> moveInfoList = new ArrayList<>();

    public synchronized void setLastMoveInfo(MoveInfo moveInfo) {
        this.lastMoveInfo.set(moveInfo);
        notifyAll();
    }

    public synchronized MoveInfo getLastMoveInfo() {
        return this.lastMoveInfo.get();
    }

    public synchronized void setCurrentAnalyseKey(AnalyseKey analyseKey) {
        this.currentAnalyseKey.set(analyseKey);
        notifyAll();
    }

    public synchronized AnalyseKey getCurrentAnalyseKey() {
        return this.currentAnalyseKey.get();
    }

    public synchronized void completeAnalyze() {
        this.isCompleteAnalyze.set(true);
        notifyAll();
    }

    public synchronized boolean isCompleteAnalyze() {
        return this.isCompleteAnalyze.get();
    }

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

    public synchronized void waitUntilCompleteAnalyseThenReset() {
        while (!this.isCompleteAnalyze.compareAndSet(true, false)) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void waitUntilLastMoveInfoSet() {
        while (this.lastMoveInfo.get() == null) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized MoveInfo getAndResetLastMoveInfo() {
        MoveInfo moveInfo = this.lastMoveInfo.getAndSet(null);
        this.notifyAll();
        return moveInfo;
    }
}
