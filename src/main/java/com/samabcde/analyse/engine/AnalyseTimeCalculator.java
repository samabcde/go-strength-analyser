package com.samabcde.analyse.engine;

import static com.google.common.collect.Range.range;
import com.google.common.collect.BoundType;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import java.math.BigDecimal;
import java.math.MathContext;

public class AnalyseTimeCalculator {
    private RangeMap<Integer, Integer> rangeToAnalyseMsMap;

    public AnalyseTimeCalculator(int noOfMove, int runTimeSec) {
        this.rangeToAnalyseMsMap = calculateAnalyseTimeMs(noOfMove, runTimeSec);
    }

    public int getMoveAnalyseTimeMs(int moveNo) {
        Integer analyseTime = rangeToAnalyseMsMap.get(moveNo);
        if (analyseTime == null) {
            throw new IllegalArgumentException("moveNo: [" + moveNo + "] not in range: " + rangeToAnalyseMsMap);
        }
        return analyseTime;
    }

    private static RangeMap<Integer, Integer> calculateAnalyseTimeMs(int noOfMove, int runTimeSec) {
        RangeMap<Integer, Integer> rangeMap = TreeRangeMap.create();
        BigDecimal runTimeMs = new BigDecimal(runTimeSec * 1000).divide(BigDecimal.valueOf(3), MathContext.DECIMAL64);
        BigDecimal part1TimeWeight = new BigDecimal("0.1");
        BigDecimal part2TimeWeight = new BigDecimal("0.7");
        BigDecimal part3TimeWeight = new BigDecimal("0.2");
        Integer part1RunTime = runTimeMs.multiply(part1TimeWeight).intValue();
        Integer part2RunTime = runTimeMs.multiply(part2TimeWeight).intValue();
        Integer part3RunTime = runTimeMs.multiply(part3TimeWeight).intValue();
        BigDecimal part1MoveWeight = new BigDecimal("0.1");
        BigDecimal part2MoveWeight = new BigDecimal("0.6");
        Integer part1MoveEnd = part1MoveWeight.multiply(new BigDecimal(noOfMove)).intValue();
        Integer part2MoveEnd = part1MoveWeight.add(part2MoveWeight).multiply(new BigDecimal(noOfMove)).intValue();

        Integer part1NoOfMove = part1MoveEnd;
        if (part1NoOfMove == 0) {
            rangeMap.put(range(0, BoundType.CLOSED, noOfMove, BoundType.CLOSED), 0);
            return rangeMap;
        }
        Integer part2NoOfMove = part2MoveEnd - part1MoveEnd;
        Integer part3NoOfMove = noOfMove - part2MoveEnd;
        Integer part1RunTimePerMove = part1RunTime / part1NoOfMove;
        Integer part2RunTimePerMove = part2RunTime / part2NoOfMove;
        Integer part3RunTimePerMove = part3RunTime / part3NoOfMove;
        rangeMap.put(range(0, BoundType.CLOSED, part1MoveEnd, BoundType.OPEN), part1RunTimePerMove);
        rangeMap.put(range(part1MoveEnd, BoundType.CLOSED, part2MoveEnd, BoundType.OPEN), part2RunTimePerMove);
        rangeMap.put(range(part2MoveEnd, BoundType.CLOSED, noOfMove, BoundType.CLOSED), part3RunTimePerMove);
        return rangeMap;
    }
}
