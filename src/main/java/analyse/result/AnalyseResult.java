package analyse.result;

import analyse.calculate.CalculateUtils;
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
    private static final BigDecimal BASELINE = new BigDecimal("10000");
    private final AnalyseMetadata metadata;
    private final List<MoveMetrics> moveMetricsList;

    private final BigDecimal blackStrengthScore;
    private final BigDecimal whiteStrengthScore;

    @JsonIgnore
    public Rank getBlackRank() {
        return Rank.valueByCode(SgfParser.parseGame(metadata.getSgf()).getProperty("BR", "NR"));
    }

    @JsonIgnore
    public Rank getWhiteRank() {
        return Rank.valueByCode(SgfParser.parseGame(metadata.getSgf()).getProperty("WR", "NR"));
    }

    // TODO add calculate module
    private AnalyseResult(AnalyseMetadata metadata, List<MoveMetrics> moveMetricsList, BigDecimal blackStrengthScore, BigDecimal whiteStrengthScore) {
        this.metadata = metadata;
        this.moveMetricsList = moveMetricsList;
        this.blackStrengthScore = calculateBlackStrengthScore();
        this.whiteStrengthScore = calculateWhiteStrengthScore();
    }

    public String getSgfName() {
        return metadata.getSgfName();
    }

    private BigDecimal calculateBlackStrengthScore() {
        BigDecimal winrateWeightSum = moveMetricsList.stream().filter(MoveMetrics::isBlack).map(MoveMetrics::getWinrateWeight).reduce(BigDecimal::add).get();
        BigDecimal winrateStrengthScore = moveMetricsList.stream().filter(MoveMetrics::isBlack).map(MoveMetrics::getWeightedWinrateStrengthScore).reduce(BigDecimal::add).get().divide(winrateWeightSum, MathContext.DECIMAL64);

        BigDecimal scoreLeadWeightSum = moveMetricsList.stream().filter(MoveMetrics::isBlack).map(MoveMetrics::getScoreLeadWeight).reduce(BigDecimal::add).get();
        BigDecimal scoreLeadStrengthScore = moveMetricsList.stream().filter(MoveMetrics::isBlack).map(MoveMetrics::getWeightedScoreLeadStrengthScore).reduce(BigDecimal::add).get().divide(scoreLeadWeightSum, MathContext.DECIMAL64);
        return CalculateUtils.average(List.of(winrateStrengthScore, scoreLeadStrengthScore)).multiply(BASELINE);
    }

    private BigDecimal calculateWhiteStrengthScore() {
        BigDecimal winrateWeightSum = moveMetricsList.stream().filter(MoveMetrics::isWhite).map(MoveMetrics::getWinrateWeight).reduce(BigDecimal::add).get();
        BigDecimal winrateStrengthScore = moveMetricsList.stream().filter(MoveMetrics::isWhite).map(MoveMetrics::getWeightedWinrateStrengthScore).reduce(BigDecimal::add).get().divide(winrateWeightSum, MathContext.DECIMAL64);

        BigDecimal scoreLeadWeightSum = moveMetricsList.stream().filter(MoveMetrics::isWhite).map(MoveMetrics::getScoreLeadWeight).reduce(BigDecimal::add).get();
        BigDecimal scoreLeadStrengthScore = moveMetricsList.stream().filter(MoveMetrics::isWhite).map(MoveMetrics::getWeightedScoreLeadStrengthScore).reduce(BigDecimal::add).get().divide(scoreLeadWeightSum, MathContext.DECIMAL64);
        return CalculateUtils.average(List.of(winrateStrengthScore, scoreLeadStrengthScore)).multiply(BASELINE);
    }

}
