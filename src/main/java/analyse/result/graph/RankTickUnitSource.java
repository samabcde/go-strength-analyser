package analyse.result.graph;

import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;

public class RankTickUnitSource implements TickUnitSource {
    @Override
    public TickUnit getLargerTickUnit(TickUnit unit) {
        return new RankTickUnit(1);
    }

    @Override
    public TickUnit getCeilingTickUnit(TickUnit unit) {
        return new RankTickUnit(1);
    }

    @Override
    public TickUnit getCeilingTickUnit(double size) {
        return new RankTickUnit(1);
    }
}
