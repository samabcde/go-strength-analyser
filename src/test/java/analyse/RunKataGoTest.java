package analyse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ApplicationConfig.class)
@TestPropertySource({"classpath:application.properties", "classpath:test.properties"})
class RunKataGoTest {

    @Autowired
    private ApplicationConfig applicationConfig;

    private RunKataGo runKataGo;

    @BeforeEach
    void setup() {
        MoveMetricExtractor moveMetricExtractor = new MoveMetricExtractor();
        AnalyseResultExporter analyseResultExporter = new AnalyseResultExporter(applicationConfig);
        System.out.println(applicationConfig);
        runKataGo = new RunKataGo(applicationConfig, moveMetricExtractor, analyseResultExporter);
    }

    @Test
    public void runSuccess() {
        runKataGo.run("-runTimeSec=150", "-sgfName=runKataGoTest");
        assertThat(Path.of(applicationConfig.getOutputFileFolder() + "/runKataGoTest.txt")).isNotEmptyFile();
    }

    @Test
    public void runPro() {
        runKataGo.run("-runTimeSec=1800", "-sgfName=smj_ysc");
        assertThat(Path.of(applicationConfig.getOutputFileFolder() + "/smj_ysc.txt")).isNotEmptyFile();
    }

    @Test
    public void runWeak() {
        runKataGo.run("-runTimeSec=150", "-sgfName=20k");
        assertThat(Path.of(applicationConfig.getOutputFileFolder() + "/20k.txt")).isNotEmptyFile();
    }

    @Test
    public void runWeak2() {
        runKataGo.run("-runTimeSec=150", "-sgfName=25k");
        assertThat(Path.of(applicationConfig.getOutputFileFolder() + "/25k.txt")).isNotEmptyFile();
    }

    @Test
    public void runKataSelf() {
        runKataGo.run("-runTimeSec=150", "-sgfName=kata_self");
        assertThat(Path.of(applicationConfig.getOutputFileFolder() + "/kata_self.txt")).isNotEmptyFile();
    }
}