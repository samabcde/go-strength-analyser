package analyse;

import analyse.core.ApplicationConfig;
import analyse.engine.KataGoFactory;
import analyse.engine.RunKataGo;
import analyse.info.AnalyseInfoExporter;
import analyse.metric.MoveMetricExtractor;
import analyse.result.AnalyseResultExporter;
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
class RunKataGoIntegrationTest {

    @Autowired
    private ApplicationConfig applicationConfig;

    private RunKataGo runKataGo;

    @BeforeEach
    void setup() {
        MoveMetricExtractor moveMetricExtractor = new MoveMetricExtractor();
        AnalyseInfoExporter analyseInfoExporter = new AnalyseInfoExporter(applicationConfig);
        AnalyseResultExporter analyseResultExporter = new AnalyseResultExporter(applicationConfig);
        KataGoFactory kataGoFactory = new KataGoFactory(applicationConfig);
        runKataGo = new RunKataGo(applicationConfig, moveMetricExtractor, analyseInfoExporter, analyseResultExporter, kataGoFactory);
    }

    @Test
    public void runPro() {
        runKataGo.run("kata_go", "-runTimeSec=1", "-sgfName=smj_ysc");
        assertThat(Path.of(applicationConfig.getOutputFileFolder() + "/smj_ysc.txt")).isNotEmptyFile();
    }

}