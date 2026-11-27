package com.samabcde.analyse.metric;

import com.samabcde.analyse.info.MoveInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MoveMetricExtractor {
    private static final Pattern METRICS_PATTERN = Pattern.compile("move (\\S+) visits (\\d+) edgeVisits (\\d+) utility (\\S+) winrate (\\S+) scoreMean (\\S+) scoreStdev (\\S+) scoreLead (\\S+) scoreSelfplay (\\S+) prior (\\S+) lcb (\\S+) utilityLcb (\\S+) weight (\\S+)(?: isSymmetryOf \\S+)? order (\\d+) pv (.*)");

    public MoveMetric extractMoveMetric(MoveInfo moveInfo) {
        String raw = moveInfo.info();
        Map<Integer, ParsedInfo> infosByOrder = new TreeMap<>();
        Arrays.stream(raw.split("info "))
                .filter(info -> !info.isBlank())
                .map(this::parseInfo)
                .forEach(info -> infosByOrder.put(info.order(), info));
        List<ParsedInfo> infos = List.copyOf(infosByOrder.values());
        if (infos.isEmpty()) {
            throw new IllegalArgumentException("invalid info:" + raw);
        }
        ParsedInfo best = infos.getFirst();
        return MoveMetric.builder()
                .analyseKey(moveInfo.analyseKey())
                .bestMove(best.move())
                .winrate(best.winrate())
                .scoreLead(best.scoreLead())
                .bestVariations(infos.stream().map(info -> new MoveVariation(info.order(), info.pv())).toList())
                .build();
    }

    private ParsedInfo parseInfo(String info) {
        Matcher matcher = METRICS_PATTERN.matcher(info.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Can't match pattern with info:" + info);
        }
        List<String> pv = Arrays.stream(matcher.group(15).trim().split("\\s+"))
                .filter(move -> !move.isBlank())
                .toList();
        return new ParsedInfo(
                matcher.group(1),
                new BigDecimal(matcher.group(5)),
                new BigDecimal(matcher.group(8)),
                Integer.parseInt(matcher.group(14)),
                pv);
    }

    private record ParsedInfo(String move, BigDecimal winrate, BigDecimal scoreLead, int order, List<String> pv) {
    }
}
