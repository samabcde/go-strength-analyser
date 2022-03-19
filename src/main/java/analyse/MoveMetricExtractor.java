package analyse;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MoveMetricExtractor {
    private static final Pattern metricsPattern = Pattern.compile("move ([A-Z0-9]+) visits ([0-9]+) utility ([e0-9-.]+) winrate ([e0-9-.]+) scoreMean ([e0-9-.]+) scoreStdev ([e0-9-.]+) scoreLead ([e0-9-.]+) scoreSelfplay ([e0-9-.]+) prior ([e0-9-.]+) lcb ([e0-9-.]+) utilityLcb ([e0-9-.]+) order ([0-9]) pv ([a-zA-Z 0-9]+)");

    public MoveMetric extractMoveMetric(Integer moveNo, String info) {
        String[] moveInfos = info.split("info ");
        if (moveInfos.length < 2) {
            throw new IllegalArgumentException("Can't match pattern with info:" + info);
        }
        String moveInfo = moveInfos[1];
        Matcher matcher = metricsPattern.matcher(moveInfo);
        if (matcher.matches()) {
            return MoveMetric.builder().moveNo(moveNo).move(matcher.group(1)).winrate(new BigDecimal(matcher.group(4))).scoreLead(new BigDecimal(matcher.group(7))).build();
        } else {
            throw new IllegalArgumentException("Can't match pattern with info:" + info);
        }
    }
}
