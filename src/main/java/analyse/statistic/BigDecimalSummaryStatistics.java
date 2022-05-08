package analyse.statistic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class BigDecimalSummaryStatistics implements Consumer<BigDecimal> {

    public static Collector<BigDecimal, ?, BigDecimalSummaryStatistics> statistics() {
        return Collector.of(BigDecimalSummaryStatistics::new,
                BigDecimalSummaryStatistics::accept, BigDecimalSummaryStatistics::merge);
    }

    private BigDecimal sum = BigDecimal.ZERO;
    private BigDecimal sumOfSquare = BigDecimal.ZERO;
    private BigDecimal min;
    private BigDecimal max;
    private long count;

    @Override
    public void accept(BigDecimal t) {
        if (count == 0) {
            Objects.requireNonNull(t);
            count = 1;
            sum = t;
            sumOfSquare = t.pow(2);
            min = t;
            max = t;
        } else {
            sum = sum.add(t);
            sumOfSquare = sumOfSquare.add(t.pow(2));
            if (min.compareTo(t) > 0) min = t;
            if (max.compareTo(t) < 0) max = t;
            count++;
        }
    }

    public BigDecimalSummaryStatistics merge(BigDecimalSummaryStatistics s) {
        if (s.count > 0) {
            if (count == 0) {
                count = s.count;
                sum = s.sum;
                sumOfSquare = s.sumOfSquare;
                min = s.min;
                max = s.max;
            } else {
                sum = sum.add(s.sum);
                sumOfSquare = sumOfSquare.add(s.sum);
                if (min.compareTo(s.min) > 0) min = s.min;
                if (max.compareTo(s.max) < 0) max = s.max;
                count += s.count;
            }
        }
        return this;
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public BigDecimal getAverage() {
        return count < 2 ? sum : sum.divide(BigDecimal.valueOf(count), MathContext.DECIMAL64);
    }

    public BigDecimal getMin() {
        return min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public BigDecimal getSumOfSquare() {
        return sumOfSquare;
    }

    public final BigDecimal getStandardDeviation() {
        if (getCount() == 0 || getCount() == 1) {
            return BigDecimal.ZERO;
        }
        return ((getSumOfSquare().divide(BigDecimal.valueOf(getCount()), MathContext.DECIMAL64)).subtract(getAverage().pow(2))).sqrt(MathContext.DECIMAL64);
    }

    @Override
    public String toString() {
        return count == 0 ? "empty" : (count + " elements between " + min + " and " + max + ", sum=" + sum);
    }
}