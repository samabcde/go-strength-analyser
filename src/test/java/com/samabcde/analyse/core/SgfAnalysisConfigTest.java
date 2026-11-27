package com.samabcde.analyse.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SgfAnalysisConfigTest {
    @Test
    void defaultsToThreeVariations() {
        assertEquals(3, new SgfAnalysisConfig().getMaxVariations());
    }

    @Test
    void acceptsVariationLimitsFromZeroToThree() {
        SgfAnalysisConfig config = new SgfAnalysisConfig();
        config.setMaxVariations(0);
        assertEquals(0, config.getMaxVariations());
        config.setMaxVariations(3);
        assertEquals(3, config.getMaxVariations());
    }

    @Test
    void rejectsVariationLimitsOutsideZeroToThree() {
        SgfAnalysisConfig config = new SgfAnalysisConfig();
        assertThrows(IllegalArgumentException.class, () -> config.setMaxVariations(-1));
        assertThrows(IllegalArgumentException.class, () -> config.setMaxVariations(4));
    }
}
