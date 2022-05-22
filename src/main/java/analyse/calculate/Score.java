package analyse.core;

import java.math.BigDecimal;

public record Score(BigDecimal winrateScore, BigDecimal scoreLeadScore, BigDecimal integrated) {
}
