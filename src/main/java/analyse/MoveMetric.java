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
    private final BigDecimal scoreMean;
    @Getter
    private final String move;

    public MoveMetric(int moveNo, BigDecimal winrate, BigDecimal scoreMean) {
        this.moveNo = moveNo;
        this.winrate = winrate;
        this.scoreMean = scoreMean;
        this.move = "";
    }

    public BigDecimal getBlackWinrate() {
        return this.moveNo % 2 == 0 ? this.winrate : new BigDecimal("1").subtract(winrate);
    }

    public BigDecimal getBlackWinratePercentage() {
        return this.getBlackWinrate().multiply(new BigDecimal("100"));
    }

    public BigDecimal getBlackScoreMean() {
        return this.moveNo % 2 == 0 ? scoreMean : scoreMean.negate();
    }

    public BigDecimal getRespectiveWinrate() {
        return new BigDecimal("1").subtract(winrate);
    }

    public BigDecimal getRespectiveScoreMean() {
        return scoreMean.negate();
    }

    public boolean isBlack() {
        return this.moveNo % 2 == 0;
    }

    public boolean isWhite() {
        return !isBlack();
    }
}