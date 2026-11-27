package com.samabcde.analyse.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "analysis.sgf")
public class SgfAnalysisConfig {
    public static final int MAX_VARIATIONS_LIMIT = 3;

    private int maxVariations = MAX_VARIATIONS_LIMIT;

    public int getMaxVariations() {
        return maxVariations;
    }

    public void setMaxVariations(int maxVariations) {
        if (maxVariations < 0 || maxVariations > MAX_VARIATIONS_LIMIT) {
            throw new IllegalArgumentException("analysis.sgf.max-variations must be between 0 and " + MAX_VARIATIONS_LIMIT);
        }
        this.maxVariations = maxVariations;
    }
}
