package com.samabcde.analyse.engine;

import com.samabcde.analyse.core.ApplicationConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class KataGoFactory {
    private static final String reportAnalysisWinratesAs = "SIDETOMOVE";
    private final ApplicationConfig applicationConfig;

    public KataGoFactory(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public Process createKataGoProcess() {
        ProcessBuilder processBuilder = new ProcessBuilder(applicationConfig.getKataGoPath(), "gtp",
                "-config", applicationConfig.getConfigFilePath(),
                "-model", applicationConfig.getWeightPath(),
                "-override-config", "reportAnalysisWinratesAs=" + reportAnalysisWinratesAs);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
