package analyse.metric;

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

    public BigDecimal getBlackScoreLead() {
        return this.candidate.getBlackScoreLead();
    }

    public BigDecimal getRateChange() {
        return this.candidate.getBlackWinrate().subtract(this.ai.getBlackWinrate());
    }

    public BigDecimal getStrengthScore() {
        return CalculateUtils.average(List.of(getWinrateStrengthScore(), getScoreLeadStrengthScore())).multiply(BigDecimal.valueOf(10000));
    }

    public BigDecimal getWinrateStrengthScore() {
        return calculateStrengthScore(candidate.getRespectiveWinrate(), pass.getRespectiveWinrate(), ai.getRespectiveWinrate());
    }

    public BigDecimal getWeightedWinrateStrengthScore() {
        return getWinrateStrengthScore().multiply(getWinrateWeight(), MathContext.DECIMAL64);
    }

    public BigDecimal getScoreLeadStrengthScore() {
        return calculateStrengthScore(candidate.getRespectiveScoreLead(), pass.getRespectiveScoreLead(), ai.getRespectiveScoreLead());
    }

    public BigDecimal getWeightedScoreLeadStrengthScore() {
        return getScoreLeadStrengthScore().multiply(getScoreLeadWeight(), MathContext.DECIMAL64);
    }

    public BigDecimal getWinrateWeight() {
        if (pass.getRespectiveWinrate().compareTo(ai.getRespectiveWinrate()) >= 0) {
            return BigDecimal.ZERO;
        }
        return ai.getRespectiveWinrate().subtract(pass.getRespectiveWinrate());
    }

    public BigDecimal getScoreLeadWeight() {
        if (pass.getRespectiveScoreLead().compareTo(ai.getRespectiveScoreLead()) >= 0) {
            return BigDecimal.ZERO;
        }
        return ai.getRespectiveScoreLead().subtract(pass.getRespectiveScoreLead());
    }

//    // y = (x - x1)/(x2 - x1)
//    private BigDecimal calculateStrengthScore(BigDecimal x, BigDecimal x1, BigDecimal x2) {
//        if (x1.compareTo(x2) >= 0) {
//            return BigDecimal.ONE;
//        }
//        if (x.compareTo(x2) > 0) {
//            x = x2;
//        }
//        if (x.compareTo(x1) < 0) {
//            x = x1;
//        }
//        BigDecimal y = (x.subtract(x1)).divide(x2.subtract(x1), MathContext.DECIMAL64);
//        return y;
//    }

    // y = (2x - x1 - x2)/(x2 - x1)
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
        BigDecimal y = (x.multiply(BigDecimal.valueOf(2)).subtract(x1).subtract(x2)).divide(x2.subtract(x1), MathContext.DECIMAL64);
        return y;
    }

    public String details() {
        BigDecimal strengthScore = getStrengthScore();
        BigDecimal winrateStrengthScore = getWinrateStrengthScore();
        BigDecimal scoreLeadStrengthScore = getScoreLeadStrengthScore();
        BigDecimal aiWinrate = ai.getRespectiveWinrate();
        BigDecimal candidateWinrate = candidate.getRespectiveWinrate();
        BigDecimal passWinrate = pass.getRespectiveWinrate();
        BigDecimal aiScoreLead = ai.getRespectiveScoreLead();
        BigDecimal candidateScoreLead = candidate.getRespectiveScoreLead();
        BigDecimal passScoreLead = pass.getRespectiveScoreLead();
        BigDecimal winrateWeight = getWinrateWeight();
        BigDecimal scoreLeadWeight = getScoreLeadWeight();
        return strengthScore + "\t" + winrateStrengthScore + "\t" + scoreLeadStrengthScore + "\t" + aiWinrate + "\t" + candidateWinrate + "\t" + passWinrate
                + "\t" + aiScoreLead + "\t" + candidateScoreLead + "\t" + passScoreLead + "\t" + winrateWeight + "\t" + scoreLeadWeight;
    }

    public boolean isBlack() {
        return this.moveNo % 2 == 1;
    }

    public boolean isWhite() {
        return !isBlack();
    }
}