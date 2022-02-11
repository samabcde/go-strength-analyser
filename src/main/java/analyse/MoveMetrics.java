package analyse;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class MoveMetrics {

    @Getter
    private final Integer moveNo;
    @Getter
    private final BigDecimal winrate;
    @Getter
    private final BigDecimal scoreMean;
    @Getter
    private BigDecimal rateChange;
    @Getter
    private BigDecimal scoreMeanChange;


    static List<MoveMetrics> calculateWinrateChanges(List<MoveMetrics> winrates) {
        List<MoveMetrics> winrateChanges = new ArrayList<>();
        for (int i = 0; i < winrates.size() - 1; i++) {
            winrates.get(i).calculateRateChange(winrates.get(i + 1).getBlackWinrate());
            winrateChanges.add(winrates.get(i));
        }
        return winrateChanges;
    }

    public MoveMetrics(Integer moveNo, BigDecimal winrate, BigDecimal scoreMean) {
        this.moveNo = moveNo;
        this.winrate = winrate;
        this.scoreMean = scoreMean;
    }

    public BigDecimal getBlackWinrate() {
        return this.moveNo % 2 == 0 ? new BigDecimal("100").subtract(winrate) : this.winrate;
    }

    public BigDecimal getBlackScoreMean() {
        return this.moveNo % 2 == 0 ? scoreMean.negate() : scoreMean;
    }

    public void calculateRateChange(BigDecimal nextBlackWinrate) {
        BigDecimal blackRateChange = (nextBlackWinrate.subtract(this.getBlackWinrate()));
        this.rateChange = this.moveNo % 2 == 1 ? blackRateChange.negate() : blackRateChange;
    }

    public void calculateScoreMeanChange(BigDecimal nextBlackScoreMean) {
        BigDecimal blackScoreMeanChange = (nextBlackScoreMean.subtract(this.getBlackScoreMean()));
        this.scoreMeanChange = this.moveNo % 2 == 1 ? blackScoreMeanChange.negate() : blackScoreMeanChange;
    }
}