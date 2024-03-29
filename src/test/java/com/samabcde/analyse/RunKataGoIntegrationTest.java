package com.samabcde.analyse;

import com.samabcde.analyse.core.ApplicationConfig;
import com.samabcde.analyse.engine.KataGoFactory;
import com.samabcde.analyse.engine.RunKataGo;
import com.samabcde.analyse.formula.FormulaV1;
import com.samabcde.analyse.info.AnalyseInfoExporter;
import com.samabcde.analyse.info.AnalyseInfoImporter;
import com.samabcde.analyse.metric.MoveMetricExtractor;
import com.samabcde.analyse.metric.MoveMetricsScoreCalculator;
import com.samabcde.analyse.result.AnalyseResultExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ApplicationConfig.class)
@TestPropertySource({"classpath:application.properties", "classpath:test.properties"})
class RunKataGoIntegrationTest {

    @Autowired
    private ApplicationConfig applicationConfig;

    private RunKataGo runKataGo;

    FakeKataGo fakeKataGo = new FakeKataGo();
    ExecutorService executorService;

    @BeforeEach
    void setup() {

        MoveMetricExtractor moveMetricExtractor = new MoveMetricExtractor();
        AnalyseInfoExporter analyseInfoExporter = new AnalyseInfoExporter(applicationConfig);
        AnalyseInfoImporter analyseInfoImporter = new AnalyseInfoImporter();
        AnalyseResultExporter analyseResultExporter = new AnalyseResultExporter(applicationConfig);
        MoveMetricsScoreCalculator moveMetricsScoreCalculator = new MoveMetricsScoreCalculator(new FormulaV1());
        KataGoFactory kataGoFactory = mock(KataGoFactory.class);
        when(kataGoFactory.createKataGoProcess()).thenReturn(fakeKataGo);
        runKataGo = new RunKataGo(applicationConfig, moveMetricExtractor, analyseInfoExporter, analyseInfoImporter, analyseResultExporter, kataGoFactory, moveMetricsScoreCalculator);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> fakeKataGo.start());
        executorService.shutdown();
    }

    @AfterEach
    void tearDown() {
        fakeKataGo.destroy();
        executorService.shutdownNow();
    }

    @Test
    public void runPro() {
        runKataGo.run("kata_go", "-runTimeSec=1", "-sgfName=smj_ysc");
        assertThat(Path.of(applicationConfig.getOutputFileFolder() + "/smj_ysc.txt")).isNotEmptyFile();
    }

}