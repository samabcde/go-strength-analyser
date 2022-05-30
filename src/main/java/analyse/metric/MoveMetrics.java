package analyse.metric;

import analyse.calculate.MoveScore;
import lombok.*;

import java.math.BigDecimal;
import java.math.MathContext;

@ToString
@Builder
@EqualsAndHashCode
public class MoveMetrics {
    @Getter
    private MoveMetric ai;
    @Getter
    private MoveMetric pass;
    @Getter
    private MoveMetric candidate;
    @Getter
    private final Integer moveNo;

    @Getter
    @Setter
    private MoveScore moveScore;

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
        return this.candidate.getRespectiveWinrate().subtract(this.ai.getRespectiveWinrate());
    }

    public BigDecimal getWinrateStrengthScore() {
        return moveScore.winrateScore();
    }

    public BigDecimal getScoreLeadStrengthScore() {
        return moveScore.scoreLeadScore();
    }

    public BigDecimal getStrengthScore() {
        return moveScore.integrated();
    }

    public BigDecimal getWeightedWinrateStrengthScore() {
        return getWinrateStrengthScore().multiply(getWinrateWeight(), MathContext.DECIMAL64);
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