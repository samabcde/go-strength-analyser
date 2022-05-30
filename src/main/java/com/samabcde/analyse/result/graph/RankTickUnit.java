package com.samabcde.analyse.result.graph;


import com.samabcde.analyse.sgf.Rank;
import org.jfree.chart.axis.NumberTickUnit;

public class RankTickUnit extends NumberTickUnit {
    public RankTickUnit(double size) {
        super(size);
    }

    @Override
    public String valueToString(double value) {
        return Rank.valueByLevel((int) value).code();
    }
}
