package analyse;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MoveMetricsExtractor {
    private static final Pattern metricsPattern = Pattern.compile("move ([A-Z0-9]+) visits ([0-9]+) utility ([e0-9-\\.]+) winrate ([e0-9-\\.]+) scoreMean ([e0-9-\\.]+) scoreStdev ([e0-9-\\.]+) scoreLead ([e0-9-\\.]+) scoreSelfplay ([e0-9-\\.]+) prior ([e0-9-\\.]+) lcb ([e0-9-\\.]+) utilityLcb ([e0-9-\\.]+) order ([0-9]) pv ([A-Z 0-9]+)");

    public MoveMetrics extractMoveMetrics(Integer moveNo, String info) {
        String[] moveInfos = info.split("info ");
        if (moveInfos.length < 2) {
            throw new IllegalArgumentException("Can't match pattern with info:" + info);
        }
        String moveInfo = moveInfos[1];
        Matcher matcher = metricsPattern.matcher(moveInfo);
        if (matcher.matches()) {
            return MoveMetrics.builder().moveNo(moveNo).winrate(new BigDecimal(matcher.group(4)).multiply(new BigDecimal("100"))).scoreMean(new BigDecimal(matcher.group(5))).build();
        } else {
            throw new IllegalArgumentException("Can't match pattern with info:" + info);
        }
    }
}
