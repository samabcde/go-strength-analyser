package analyse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ToString
@Builder
@EqualsAndHashCode
public class MoveMetrics {

    @Getter
    private final Integer moveNo;
    @Getter
    private final BigDecimal winrate;
    @Getter
    private final BigDecimal scoreMean;
    @EqualsAndHashCode.Exclude
    @Getter
    private BigDecimal rateChange;

    static List<MoveMetrics> calculateWinrateChanges(List<MoveMetrics> winrates) {
        List<MoveMetrics> winrateChanges = new ArrayList<>();
        for (int i = 0; i < winrates.size() - 1; i++) {
            winrates.get(i).calculateRateChange(winrates.get(i + 1).getBlackWinrate());
            winrateChanges.add(winrates.get(i));
        }
        return winrateChanges;
    }

    public MoveMetrics(Integer moveNo, BigDecimal winrate, BigDecimal scoreMean, BigDecimal rateChange) {
        this.moveNo = moveNo;
        this.winrate = winrate;
        this.scoreMean = scoreMean;
        this.rateChange = rateChange;
    }

    public MoveMetrics(Integer moveNo, BigDecimal winrate, BigDecimal scoreMean) {
        this.moveNo = moveNo;
        this.winrate = winrate;
        this.scoreMean = scoreMean;
    }

    public BigDecimal getBlackWinrate() {
        return this.moveNo % 2 == 0 ? this.winrate : new BigDecimal("100").subtract(winrate);
    }

    public BigDecimal getBlackScoreMean() {
        return this.moveNo % 2 == 0 ? scoreMean : scoreMean.negate();
    }

    public void calculateRateChange(BigDecimal nextBlackWinrate) {
        BigDecimal blackRateChange = (nextBlackWinrate.subtract(this.getBlackWinrate()));
        this.rateChange = this.moveNo % 2 == 1 ? blackRateChange.negate() : blackRateChange;
    }

}