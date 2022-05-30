package com.samabcde.analyse.calculate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

public final class CalculateUtils {
    private CalculateUtils() {

    }

    public static BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal average = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            average = average.add(value);
        }
        return average.divide(BigDecimal.valueOf(values.size()), MathContext.DECIMAL64);
    }
}
