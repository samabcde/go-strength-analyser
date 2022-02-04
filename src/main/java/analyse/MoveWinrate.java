package analyse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MoveWinrate {

	private final Integer moveNo;
	private final BigDecimal winrate;
	private BigDecimal blackWinrate;
	private BigDecimal scoreMean;
	private BigDecimal rateChange;
	private BigDecimal scoreMeanChange;

	public BigDecimal getRateChange() {
		return rateChange;
	}

	static List<MoveWinrate> calculateWinrateChanges(List<MoveWinrate> winrates) {
		List<MoveWinrate> winrateChanges = new ArrayList<MoveWinrate>();
		for (int i = 0; i < winrates.size() - 1; i++) {
			winrates.get(i).calculateRateChange(winrates.get(i + 1).getBlackWinrate());
			winrateChanges.add(winrates.get(i));
		}
		return winrateChanges;
	}

	public MoveWinrate(Integer moveNo, BigDecimal winrate) {
		this.moveNo = moveNo;
		this.winrate = winrate;
	}

	public MoveWinrate(Integer moveNo, BigDecimal winrate, BigDecimal scoreMean) {
		this.moveNo = moveNo;
		this.winrate = winrate;
		this.scoreMean = scoreMean;
	}

	public Integer getMoveNo() {
		return moveNo;
	}

	public BigDecimal getWinrate() {
		return winrate;
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