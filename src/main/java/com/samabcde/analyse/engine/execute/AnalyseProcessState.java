package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.info.MoveInfo;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AnalyseProcessState {
    private static final Pattern ORDER_PATTERN = Pattern.compile("\\border (\\d+) pv ");

    private boolean isEnd = false;
    private boolean isErrorOccur = false;
    private boolean isReady = false;
    private final AtomicReference<AnalyseKey> currentAnalyseKey = new AtomicReference<>(null);
    private final AtomicBoolean isCompleteAnalyze = new AtomicBoolean(false);
    private final AtomicReference<MoveInfo> lastMoveInfo = new AtomicReference<>(null);
    private final Map<Integer, String> latestInfoByOrder = new TreeMap<>();
    private String latestUnrankedInfo;
    private AnalyseKey accumulatedInfoKey;
    public final List<MoveMetrics> moveMetricsList = new ArrayList<>();
    public final List<MoveInfo> moveInfoList = new ArrayList<>();
    public MoveMetric rootMetric;

    public synchronized void setLastMoveInfo(MoveInfo moveInfo) {
        if (!moveInfo.analyseKey().equals(accumulatedInfoKey)) {
            latestInfoByOrder.clear();
            latestUnrankedInfo = null;
            accumulatedInfoKey = moveInfo.analyseKey();
        }
        Arrays.stream(moveInfo.info().split("(?=info move )"))
                .filter(info -> !info.isBlank())
                .forEach(info -> {
                    Matcher matcher = ORDER_PATTERN.matcher(info);
                    if (matcher.find()) {
                        latestInfoByOrder.put(Integer.parseInt(matcher.group(1)), info.trim());
                    } else {
                        latestUnrankedInfo = info.trim();
                    }
                });
        List<String> latestInfo = new ArrayList<>(latestInfoByOrder.values());
        if (latestUnrankedInfo != null) {
            latestInfo.add(latestUnrankedInfo);
        }
        this.lastMoveInfo.set(new MoveInfo(moveInfo.analyseKey(), String.join(" ", latestInfo)));
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
        latestInfoByOrder.clear();
        latestUnrankedInfo = null;
        accumulatedInfoKey = null;
        this.notifyAll();
        return moveInfo;
    }
}
