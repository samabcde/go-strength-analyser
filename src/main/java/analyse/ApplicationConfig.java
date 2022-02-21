package analyse;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "path")
public class ApplicationConfig {
    private String kataGoPath;
    private String sgfFolder;
    private String weightPath;
    private String configFilePath;
    private String outputFileFolder;

    public void setKataGoPath(String kataGoPath) {
        this.kataGoPath = kataGoPath;
    }

    public void setSgfFolder(String sgfFolder) {
        this.sgfFolder = sgfFolder;
    }

    public void setWeightPath(String weightPath) {
        this.weightPath = weightPath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public void setOutputFileFolder(String outputFileFolder) {
        this.outputFileFolder = outputFileFolder;
    }

    public String getKataGoPath() {
        return kataGoPath;
    }

    public String getSgfFolder() {
        return sgfFolder;
    }

    public Path getSgfFolderPath() {
        if (sgfFolder.startsWith("classpath:")) {
            try {
                return Path.of(
                        getClass().getResource(sgfFolder.replace("classpath:", "")).toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return Path.of(sgfFolder);
    }

    public String getWeightPath() {
        return weightPath;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public String getOutputFileFolder() {
        return outputFileFolder;
    }

}
