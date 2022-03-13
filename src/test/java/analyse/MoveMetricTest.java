package analyse;

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
            MoveMetric moveMetric = new MoveMetric(10, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(10, moveMetric.getMoveNo());
            assertEquals(BigDecimal.valueOf(89.64), moveMetric.getWinrate());
            assertEquals(BigDecimal.valueOf(3.14), moveMetric.getScoreMean());
        }
    }

    @Nested
    public class TestGetBlackWinrate {

        @BeforeEach
        public void setup() {

        }

        @Test
        public void Given_moveMetricWithOddNumberMove_Should_return_100minusWinrate() {
            MoveMetric moveMetricWithOddNumber = new MoveMetric(9, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(10.36), moveMetricWithOddNumber.getBlackWinrate());
        }

        @Test
        public void Given_moveMetricWithEvenNumberMove_Should_return_Winrate() {
            MoveMetric moveMetricWithEvenNumber = new MoveMetric(10, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(89.64), moveMetricWithEvenNumber.getBlackWinrate());
        }
    }

    @Nested
    public class TestGetBlackScoreMean {

        @BeforeEach
        public void setup() {

        }


        @Test
        public void Given_moveMetricWithOddNumberMove_Should_return_NegateOfScoreMean() {
            MoveMetric moveMetricWithOddNumber = new MoveMetric(9, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(-3.14), moveMetricWithOddNumber.getBlackScoreMean());
        }

        @Test
        public void Given_moveMetricWithEvenNumberMove_Should_return_ScoreMean() {
            MoveMetric moveMetricWithEvenNumber = new MoveMetric(10, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(3.14), moveMetricWithEvenNumber.getBlackScoreMean());
        }

    }
}