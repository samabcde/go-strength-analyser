package com.samabcde.analyse;

import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.core.AnalyseTarget;
import com.samabcde.analyse.metric.MoveMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveMetricTest {

    @Nested
    public class TestConstructor {
        @Test
        public void Should_construct_With_no_error() {
            MoveMetric moveMetric = new MoveMetric(new AnalyseKey(AnalyseTarget.AI, 10, "A1"), BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(10, moveMetric.getMoveNo());
            assertEquals(BigDecimal.valueOf(89.64), moveMetric.getWinrate());
            assertEquals(BigDecimal.valueOf(3.14), moveMetric.getScoreLead());
        }
    }

    @Nested
    public class TestGetBlackWinrate {

        @BeforeEach
        public void setup() {

        }

        @Test
        public void Given_moveMetricWithOddNumberMove_Should_return_100minusWinrate() {
            MoveMetric moveMetricWithOddNumber = new MoveMetric(new AnalyseKey(AnalyseTarget.AI, 9, "A1"), BigDecimal.valueOf(0.8964), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(0.1036), moveMetricWithOddNumber.getBlackWinrate());
        }

        @Test
        public void Given_moveMetricWithEvenNumberMove_Should_return_Winrate() {
            MoveMetric moveMetricWithEvenNumber = new MoveMetric(new AnalyseKey(AnalyseTarget.AI, 10, "A1"), BigDecimal.valueOf(0.8964), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(0.8964), moveMetricWithEvenNumber.getBlackWinrate());
        }
    }

    @Nested
    public class TestGetBlackMoveScoreLead {

        @BeforeEach
        public void setup() {

        }


        @Test
        public void Given_moveMetricWithOddNumberMove_Should_return_NegateOfScoreLead() {
            MoveMetric moveMetricWithOddNumber = new MoveMetric(new AnalyseKey(AnalyseTarget.AI, 9, "A1"), BigDecimal.valueOf(0.8964), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(-3.14), moveMetricWithOddNumber.getBlackScoreLead());
        }

        @Test
        public void Given_moveMetricWithEvenNumberMove_Should_return_ScoreLead() {
            MoveMetric moveMetricWithEvenNumber = new MoveMetric(new AnalyseKey(AnalyseTarget.AI, 10, "A1"), BigDecimal.valueOf(0.8964), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(3.14), moveMetricWithEvenNumber.getBlackScoreLead());
        }

    }
}