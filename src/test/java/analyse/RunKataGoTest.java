package analyse;

import analyse.engine.KataGoFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ApplicationConfig.class)
@TestPropertySource({"classpath:application.properties", "classpath:test.properties"})
class RunKataGoTest {

    @Autowired
    private ApplicationConfig applicationConfig;

    private RunKataGo runKataGo;

    private FakeKataGo fakeKataGo;

    @BeforeEach
    void setup() {
        MoveMetricExtractor moveMetricExtractor = new MoveMetricExtractor();
        AnalyseResultExporter analyseResultExporter = new AnalyseResultExporter(applicationConfig);
        KataGoFactory kataGoFactory = mock(KataGoFactory.class);
        fakeKataGo = new FakeKataGo();
        when(kataGoFactory.createKataGoProcess()).thenReturn(fakeKataGo);
        Executors.newSingleThreadExecutor().execute(() -> fakeKataGo.start());
        System.out.println(applicationConfig);
        runKataGo = new RunKataGo(applicationConfig, moveMetricExtractor, analyseResultExporter, kataGoFactory);
    }

    @AfterEach
    void teardown() {
        fakeKataGo.destroy();
    }

    @Test
    public void runSuccess() {
        runKataGo.run("-runTimeSec=10", "-sgfName=runKataGoTest");
        assertThat(Path.of(applicationConfig.getOutputFileFolder() + "/runKataGoTest.txt")).isNotEmptyFile();
    }

}