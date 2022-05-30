package analyse;

import analyse.calculate.GameScore;
import analyse.calculate.MoveScore;
import analyse.core.ApplicationConfig;
import analyse.engine.KataGoFactory;
import analyse.engine.RunKataGo;
import analyse.info.AnalyseInfoExporter;
import analyse.info.AnalyseInfoImporter;
import analyse.metric.MoveMetricExtractor;
import analyse.metric.MoveMetricsScoreCalculator;
import analyse.result.AnalyseResultExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ApplicationConfig.class)
@TestPropertySource({"classpath:application.properties", "classpath:test.properties"})
class RunKataGoTest {

    @Autowired
    private ApplicationConfig applicationConfig;

    private RunKataGo runKataGo;

    private FakeKataGo fakeKataGo;

    @Mock
    AnalyseResultExporter analyseResultExporter;

    @Mock
    MoveMetricsScoreCalculator moveMetricsScoreCalculator;

    @Mock
    AnalyseInfoExporter analyseInfoExporter;

    @Mock
    KataGoFactory kataGoFactory;

    @BeforeEach
    void setup() {
        MoveMetricExtractor moveMetricExtractor = new MoveMetricExtractor();
        AnalyseInfoImporter analyseInfoImporter = new AnalyseInfoImporter();
        fakeKataGo = new FakeKataGo();
        when(kataGoFactory.createKataGoProcess()).thenReturn(fakeKataGo);
        when(moveMetricsScoreCalculator.calculateMoveScore(any())).thenReturn(new MoveScore(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        when(moveMetricsScoreCalculator.calculateGameScore(any())).thenReturn(new GameScore(BigDecimal.ZERO, BigDecimal.ZERO));
        Executors.newSingleThreadExecutor().execute(() -> fakeKataGo.start());
        runKataGo = new RunKataGo(applicationConfig, moveMetricExtractor, analyseInfoExporter, analyseInfoImporter, analyseResultExporter, kataGoFactory, moveMetricsScoreCalculator);
    }

    @AfterEach
    void teardown() {
        fakeKataGo.destroy();
    }

    @Test
    public void runSuccess() {
        runKataGo.run("kata_go", "-runTimeSec=1", "-sgfName=runKataGoTest");
        verify(analyseInfoExporter).export(any());
        verify(analyseResultExporter).export(any());
    }

}