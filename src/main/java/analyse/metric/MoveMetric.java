package analyse.metric;

import analyse.core.AnalyseKey;
import lombok.*;

import java.math.BigDecimal;

@ToString
@Builder
@AllArgsConstructor
@EqualsAndHashCode
/*
 * Contain data needed to calculate go strength score
 * */
public class MoveMetric {
    @Getter
    private final AnalyseKey analyseKey;
    @Getter
    private final BigDecimal winrate;
    @Getter
    private final BigDecimal scoreLead;
    @Getter
    private final String bestMove;

    public MoveMetric(AnalyseKey analyseKey, BigDecimal winrate, BigDecimal scoreLead) {
        this.analyseKey = analyseKey;
        this.winrate = winrate;
        this.scoreLead = scoreLead;
        this.bestMove = "";
    }

    public int getMoveNo() {
        return this.analyseKey.moveNo();
    }

    public BigDecimal getBlackWinrate() {
        return this.getMoveNo() % 2 == 0 ? this.winrate : new BigDecimal("1").subtract(winrate);
    }

    public BigDecimal getBlackWinratePercentage() {
        return this.getBlackWinrate().multiply(new BigDecimal("100"));
    }

    public BigDecimal getBlackScoreLead() {
        return this.getMoveNo() % 2 == 0 ? scoreLead : scoreLead.negate();
    }

    public BigDecimal getRespectiveWinrate() {
        return new BigDecimal("1").subtract(winrate);
    }

    public BigDecimal getRespectiveScoreLead() {
        return scoreLead.negate();
    }

    public boolean isBlack() {
        return this.getMoveNo() % 2 == 0;
    }

    public boolean isWhite() {
        return !isBlack();
    }
}