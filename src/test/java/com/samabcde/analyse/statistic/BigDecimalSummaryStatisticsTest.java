package com.samabcde.analyse.statistic;

import com.samabcde.analyse.statistic.BigDecimalSummaryStatistics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.stream.Collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BigDecimalSummaryStatisticsTest {

    @Test
    void statistics() {
        Collector<BigDecimal, BigDecimalSummaryStatistics, BigDecimalSummaryStatistics> collector = BigDecimalSummaryStatistics.statistics();
        assertInstanceOf(BigDecimalSummaryStatistics.class, collector.supplier().get());

        BigDecimalSummaryStatistics statistics = mock(BigDecimalSummaryStatistics.class);
        collector.accumulator().accept(statistics, BigDecimal.ONE);
        verify(statistics).accept(BigDecimal.ONE);

        BigDecimalSummaryStatistics original = mock(BigDecimalSummaryStatistics.class);
        BigDecimalSummaryStatistics toCombine = mock(BigDecimalSummaryStatistics.class);
        collector.combiner().apply(original, toCombine);
        verify(original).merge(toCombine);
    }

    @Test
    void accept() {
        BigDecimalSummaryStatistics statistics = new BigDecimalSummaryStatistics();
        statistics.accept(BigDecimal.ZERO);
        statistics.accept(BigDecimal.ONE);
        assertEquals(2, statistics.getCount());
        assertEquals(BigDecimal.ZERO, statistics.getMin());
        assertEquals(BigDecimal.ONE, statistics.getMax());
        assertEquals(new BigDecimal("0.5"), statistics.getAverage());
        assertEquals(new BigDecimal("0.5"), statistics.getStandardDeviation());
        assertEquals(new BigDecimal("1"), statistics.getSum());
        assertEquals(new BigDecimal("1"), statistics.getSumOfSquare());
    }

    @Test
    void merge() {
        BigDecimalSummaryStatistics statistics1 = new BigDecimalSummaryStatistics();
        BigDecimalSummaryStatistics statistics2 = new BigDecimalSummaryStatistics();
        statistics1.accept(BigDecimal.ZERO);
        statistics2.accept(BigDecimal.ONE);
        statistics1.merge(statistics2);
        assertEquals(2, statistics1.getCount());
        assertEquals(BigDecimal.ZERO, statistics1.getMin());
        assertEquals(BigDecimal.ONE, statistics1.getMax());
        assertEquals(new BigDecimal("0.5"), statistics1.getAverage());
        assertEquals(new BigDecimal("0.5"), statistics1.getStandardDeviation());
        assertEquals(new BigDecimal("1"), statistics1.getSum());
        assertEquals(new BigDecimal("1"), statistics1.getSumOfSquare());
    }

    @Test
    void mergeEmpty() {
        BigDecimalSummaryStatistics statistics1 = new BigDecimalSummaryStatistics();
        BigDecimalSummaryStatistics statistics2 = new BigDecimalSummaryStatistics();
        statistics1.accept(BigDecimal.ZERO);
        statistics1.merge(statistics2);
        assertEquals(1, statistics1.getCount());
        assertEquals(BigDecimal.ZERO, statistics1.getMin());
        assertEquals(BigDecimal.ZERO, statistics1.getMax());
        assertEquals(BigDecimal.ZERO, statistics1.getAverage());
        assertEquals(BigDecimal.ZERO, statistics1.getStandardDeviation());
        assertEquals(BigDecimal.ZERO, statistics1.getSum());
        assertEquals(BigDecimal.ZERO, statistics1.getSumOfSquare());
    }
}