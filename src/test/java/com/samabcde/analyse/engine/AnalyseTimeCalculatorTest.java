package com.samabcde.analyse.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AnalyseTimeCalculatorTest {
    @ParameterizedTest
    @CsvSource(textBlock = """
            noOfMove, runTimeSec, moveNo, expectedAnalyseTimeMsInteger
                   0,          0,      0,                            0
                  10,          1,      0,                           33
                  10,          1,      1,                           38
                  10,          1,      2,                           38
                  10,          1,      3,                           38
                  10,          1,      4,                           38
                  10,          1,      5,                           38
                  10,          1,      6,                           38
                  10,          1,      7,                           22
                  10,          1,      8,                           22
                  10,          1,      9,                           22
                  10,          1,     10,                           22
            """, useHeadersInDisplayName = true)
    public void validMoveNoShouldReturnCorrectTime(Integer noOfMove, Integer runTimeSec, Integer moveNo, Integer expectedAnalyseTimeMs) {
        AnalyseTimeCalculator calculator = new AnalyseTimeCalculator(noOfMove, runTimeSec);
        assertEquals(expectedAnalyseTimeMs, calculator.getMoveAnalyseTimeMs(moveNo));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            noOfMove, runTimeSec, moveNo
                  10,          1,     -1
                  10,          1,     11
            """, useHeadersInDisplayName = true)
    public void test(Integer noOfMove, Integer runTimeSec, Integer moveNo) {
        AnalyseTimeCalculator calculator = new AnalyseTimeCalculator(noOfMove, runTimeSec);
        assertThrows(IllegalArgumentException.class, () -> calculator.getMoveAnalyseTimeMs(moveNo));
    }
}