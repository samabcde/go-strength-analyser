package com.samabcde.analyse.formula;

import com.samabcde.analyse.calculate.GameScore;
import com.samabcde.analyse.calculate.MoveScore;
import com.samabcde.analyse.metric.MoveMetrics;

import java.util.List;

public interface Formula {
    Version version();

    MoveScore calculateMove(MoveMetrics moveMetrics);

    GameScore calculateGame(List<MoveMetrics> moveMetricsList);
}
