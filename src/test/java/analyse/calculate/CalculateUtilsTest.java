package analyse.calculate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateUtilsTest {
    @Nested
    public class Average {
        @Test
        public void Given_EmptyList_Should_ReturnZero() {
            assertThat(CalculateUtils.average(Collections.emptyList())).isEqualTo("0");
        }

        @Test
        public void Given_ListOfNumber_Should_ReturnCorrectAverage() {
            List<BigDecimal> input = List.of("0", "2", "2", "4").stream().map(BigDecimal::new).toList();
            assertThat(CalculateUtils.average(input)).isEqualTo("2");
        }
    }
}