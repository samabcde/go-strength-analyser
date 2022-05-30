package analyse.result;

import analyse.calculate.CalculateUtils;
import analyse.calculate.GameScore;
import analyse.core.AnalyseMetadata;
import analyse.metric.MoveMetrics;
import analyse.sgf.Rank;
import analyse.sgf.SgfParser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

@Data
@Builder
public class AnalyseResult {
    private final AnalyseMetadata metadata;
    private final List<MoveMetrics> moveMetricsList;

    private final GameScore gameScore;
    @JsonIgnore
    public Rank getBlackRank() {
        return Rank.valueByCode(SgfParser.parseGame(metadata.getSgf()).getProperty("BR", "NR"));
    }
    @JsonIgnore
    public Rank getWhiteRank() {
        return Rank.valueByCode(SgfParser.parseGame(metadata.getSgf()).getProperty("WR", "NR"));
    }
    private AnalyseResult(AnalyseMetadata metadata, List<MoveMetrics> moveMetricsList, GameScore gameScore) {
        this.metadata = metadata;
        this.moveMetricsList = moveMetricsList;
        this.gameScore = gameScore;
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
