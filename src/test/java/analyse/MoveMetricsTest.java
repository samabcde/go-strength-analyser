package analyse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveMetricsTest {

    @Nested
    public class TestConstructor {
        @Test
        public void Should_construct_With_no_error() {
            MoveMetrics moveMetrics = new MoveMetrics(10, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(10, moveMetrics.getMoveNo());
            assertEquals(BigDecimal.valueOf(89.64), moveMetrics.getWinrate());
            assertEquals(BigDecimal.valueOf(3.14), moveMetrics.getScoreMean());
        }
    }

    @Nested
    public class TestGetBlackWinrate {

        @BeforeEach
        public void setup() {

        }

        @Test
        public void Given_moveMetricsWithOddNumberMove_Should_return_100minusWinrate() {
            MoveMetrics moveMetricsWithOddNumber = new MoveMetrics(9, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(10.36), moveMetricsWithOddNumber.getBlackWinrate());
        }

        @Test
        public void Given_moveMetricsWithEvenNumberMove_Should_return_Winrate() {
            MoveMetrics moveMetricsWithEvenNumber = new MoveMetrics(10, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(89.64), moveMetricsWithEvenNumber.getBlackWinrate());
        }
    }

    @Nested
    public class TestGetBlackScoreMean {

        @BeforeEach
        public void setup() {

        }


        @Test
        public void Given_moveMetricsWithOddNumberMove_Should_return_NegateOfScoreMean() {
            MoveMetrics moveMetricsWithOddNumber = new MoveMetrics(9, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(-3.14), moveMetricsWithOddNumber.getBlackScoreMean());
        }

        @Test
        public void Given_moveMetricsWithEvenNumberMove_Should_return_ScoreMean() {
            MoveMetrics moveMetricsWithEvenNumber = new MoveMetrics(10, BigDecimal.valueOf(89.64), BigDecimal.valueOf(3.14));
            assertEquals(BigDecimal.valueOf(3.14), moveMetricsWithEvenNumber.getBlackScoreMean());
        }

    }
}