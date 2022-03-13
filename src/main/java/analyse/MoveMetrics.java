package analyse;

import analyse.calculate.CalculateUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

@ToString
@Builder
@EqualsAndHashCode
public class MoveMetrics {
    private MoveMetric ai;
    private MoveMetric pass;
    private MoveMetric candidate;
    @Getter
    private final Integer moveNo;

    public BigDecimal getBlackWinrate() {
        return this.candidate.getBlackWinrate();
    }

    public BigDecimal getBlackWinratePercentage() {
        return this.candidate.getBlackWinratePercentage();
    }

    public BigDecimal getBlackScoreMean() {
        return this.candidate.getBlackScoreMean();
    }

    public BigDecimal getRateChange() {
        return this.candidate.getBlackWinrate().subtract(this.ai.getBlackWinrate());
    }

    public BigDecimal getStrengthScore() {
        return CalculateUtils.average(List.of(getWinrateStrengthScore(), getScoreLeadStrengthScore())).multiply(BigDecimal.valueOf(1000));
    }

    private BigDecimal getWinrateStrengthScore() {
        return calculateStrengthScore(candidate.getRespectiveWinrate(), pass.getRespectiveWinrate(), ai.getRespectiveWinrate());
    }

    private BigDecimal getScoreLeadStrengthScore() {
        return calculateStrengthScore(candidate.getRespectiveScoreMean(), pass.getRespectiveScoreMean(), ai.getRespectiveScoreMean());
    }

    // y = (x - x1)/(x2 - x1)
    private BigDecimal calculateStrengthScore(BigDecimal x, BigDecimal x1, BigDecimal x2) {
        if (x1.compareTo(x2) >= 0) {
            return BigDecimal.ONE;
        }
        if (x.compareTo(x2) > 0) {
            x = x2;
        }
        if (x.compareTo(x1) < 0) {
            x = x1;
        }
        BigDecimal y = (x.subtract(x1)).divide(x2.subtract(x1), MathContext.DECIMAL64);
        return y;
    }

    public String details() {
        BigDecimal strengthScore = getStrengthScore();
        BigDecimal winrateStrengthScore = getWinrateStrengthScore();
        BigDecimal scoreLeadStrengthScore = getScoreLeadStrengthScore();
        BigDecimal aiWinrate = ai.getRespectiveWinrate();
        BigDecimal candidateWinrate = candidate.getRespectiveWinrate();
        BigDecimal passWinrate = pass.getRespectiveWinrate();
        BigDecimal aiScoreMean = ai.getRespectiveScoreMean();
        BigDecimal candidateScoreMean = candidate.getRespectiveScoreMean();
        BigDecimal passScoreMean = pass.getRespectiveScoreMean();

        return strengthScore + "\t" + winrateStrengthScore + "\t" + scoreLeadStrengthScore + "\t" + aiWinrate + "\t" + candidateWinrate + "\t" + passWinrate
                + "\t" + aiScoreMean + "\t" + candidateScoreMean + "\t" + passScoreMean;
    }

    public boolean isBlack() {
        return this.moveNo % 2 == 1;
    }

    public boolean isWhite() {
        return !isBlack();
    }
}