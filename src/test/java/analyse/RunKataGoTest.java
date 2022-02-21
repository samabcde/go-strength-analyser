package analyse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ApplicationConfig.class)
@TestPropertySource({"classpath:application.properties", "classpath:test.properties"})
class RunKataGoTest {

    @Autowired
    ApplicationConfig applicationConfig;

    @Test
    public void runSuccess() {
        MoveMetricsExtractor moveMetricsExtractor = new MoveMetricsExtractor();
        AnalyseResultExporter analyseResultExporter = new AnalyseResultExporter(applicationConfig);
        System.out.println(applicationConfig);
        RunKataGo runKataGo = new RunKataGo(applicationConfig, moveMetricsExtractor, analyseResultExporter);
        runKataGo.run("-runTimeSec=1", "-sgfName=runKataGoTest");
    }

}