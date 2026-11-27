package com.samabcde.analyse.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.samabcde.analyse.calculate.GameScore;
import com.samabcde.analyse.core.AnalyseMetadata;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetrics;
import com.samabcde.analyse.sgf.Rank;
import com.samabcde.analyse.sgf.SgfParser;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AnalyseResult {
    private final AnalyseMetadata metadata;
    private final List<MoveMetrics> moveMetricsList;

    private final GameScore gameScore;

    @JsonIgnore
    private final MoveMetric rootMetric;

    @JsonIgnore
    public Rank getBlackRank() {
        return Rank.valueByCode(SgfParser.parseGame(metadata.getSgf()).getProperty("BR", "NR"));
    }

    @JsonIgnore
    public Rank getWhiteRank() {
        return Rank.valueByCode(SgfParser.parseGame(metadata.getSgf()).getProperty("WR", "NR"));
    }

    private AnalyseResult(AnalyseMetadata metadata, List<MoveMetrics> moveMetricsList, GameScore gameScore, MoveMetric rootMetric) {
        this.metadata = metadata;
        this.moveMetricsList = moveMetricsList;
        this.gameScore = gameScore;
        this.rootMetric = rootMetric;
    }

    public String getSgfName() {
        return metadata.getSgfName();
    }

    public BigDecimal getBlackStrengthScore() {
        return gameScore.blackStrengthScore();
    }

    public BigDecimal getWhiteStrengthScore() {
        return gameScore.whiteStrengthScore();
    }

}
