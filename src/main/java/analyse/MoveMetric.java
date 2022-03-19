package analyse;

import lombok.*;

import java.math.BigDecimal;

@ToString
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class MoveMetric {
    @Getter
    private final Integer moveNo;
    @Getter
    private final BigDecimal winrate;
    @Getter
    private final BigDecimal scoreLead;
    @Getter
    private final String move;

    public MoveMetric(int moveNo, BigDecimal winrate, BigDecimal scoreLead) {
        this.moveNo = moveNo;
        this.winrate = winrate;
        this.scoreLead = scoreLead;
        this.move = "";
    }

    public BigDecimal getBlackWinrate() {
        return this.moveNo % 2 == 0 ? this.winrate : new BigDecimal("1").subtract(winrate);
    }

    public BigDecimal getBlackWinratePercentage() {
        return this.getBlackWinrate().multiply(new BigDecimal("100"));
    }

    public BigDecimal getBlackScoreLead() {
        return this.moveNo % 2 == 0 ? scoreLead : scoreLead.negate();
    }

    public BigDecimal getRespectiveWinrate() {
        return new BigDecimal("1").subtract(winrate);
    }

    public BigDecimal getRespectiveScoreLead() {
        return scoreLead.negate();
    }

    public boolean isBlack() {
        return this.moveNo % 2 == 0;
    }

    public boolean isWhite() {
        return !isBlack();
    }
}