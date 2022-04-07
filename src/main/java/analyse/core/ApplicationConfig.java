package analyse.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "path")
public class ApplicationConfig {
    private Resource kataGoPath;
    private Resource sgfFolder;
    private Resource weightPath;
    private Resource configFilePath;
    private Resource outputFileFolder;
    private Resource analyseInfoFolder;

    public void setKataGoPath(Resource kataGoPath) {
        this.kataGoPath = kataGoPath;
    }

    public void setSgfFolder(Resource sgfFolder) {
        this.sgfFolder = sgfFolder;
    }

    public void setWeightPath(Resource weightPath) {
        this.weightPath = weightPath;
    }

    public void setConfigFilePath(Resource configFilePath) {
        this.configFilePath = configFilePath;
    }

    public void setOutputFileFolder(Resource outputFileFolder) {
        this.outputFileFolder = outputFileFolder;
    }

    public void setAnalyseInfoFolder(Resource analyseInfoFolder) {
        this.analyseInfoFolder = analyseInfoFolder;
    }

    public String getKataGoPath() {
        try {
            return kataGoPath.getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSgfFolder() {
        try {
            return sgfFolder.getFile().getAbsolutePath() + File.separator;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getSgfFolderPath() {
        try {
            return sgfFolder.getFile().toPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getWeightPath() {
        try {
            return weightPath.getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getConfigFilePath() {
        try {
            return configFilePath.getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getOutputFileFolder() {
        try {
            return outputFileFolder.getFile().getAbsolutePath() + File.separator;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAnalyseInfoFolder() {
        try {
            return analyseInfoFolder.getFile().getAbsolutePath() + File.separator;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
