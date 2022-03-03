package analyse;

import com.toomasr.sgf4j.parser.Game;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

@Component
@Slf4j
public class RunKataGo implements CommandLineRunner {

    private static final String reportAnalysisWinratesAs = "SIDETOMOVE";
    private final MoveMetricsExtractor moveMetricsExtractor;
    private final AnalyseResultExporter analyseResultExporter;
    private final ApplicationConfig applicationConfig;

    public RunKataGo(ApplicationConfig applicationConfig, MoveMetricsExtractor moveMetricsExtractor, AnalyseResultExporter analyseResultExporter) {
        this.applicationConfig = applicationConfig;
        this.moveMetricsExtractor = moveMetricsExtractor;
        this.analyseResultExporter = analyseResultExporter;
    }

    static int calculateAnalyseTimeMs(Integer noOfMove, Integer runTimeSec, Integer moveNo) {
        BigDecimal runTimeMs = new BigDecimal(runTimeSec * 1000);
        BigDecimal part1TimeWeight = new BigDecimal("0.1");
        BigDecimal part2TimeWeight = new BigDecimal("0.7");
        BigDecimal part3TimeWeight = new BigDecimal("0.2");
        Integer part1RunTime = runTimeMs.multiply(part1TimeWeight).intValue();
        Integer part2RunTime = runTimeMs.multiply(part2TimeWeight).intValue();
        Integer part3RunTime = runTimeMs.multiply(part3TimeWeight).intValue();
        BigDecimal part1MoveWeight = new BigDecimal("0.1");
        BigDecimal part2MoveWeight = new BigDecimal("0.6");
        // BigDecimal part3MoveWeight = new BigDecimal("0.3");
        Integer part1MoveEnd = part1MoveWeight.multiply(new BigDecimal(noOfMove)).intValue();
        Integer part2MoveEnd = part1MoveWeight.add(part2MoveWeight).multiply(new BigDecimal(noOfMove)).intValue();
        Integer part1NoOfMove = part1MoveEnd;
        Integer part2NoOfMove = part2MoveEnd - part1MoveEnd;
        Integer part3NoOfMove = noOfMove - part2MoveEnd;
        Integer part1RunTimePerMove = part1RunTime / part1NoOfMove;
        Integer part2RunTimePerMove = part2RunTime / part2NoOfMove;
        Integer part3RunTimePerMove = part3RunTime / part3NoOfMove;
        if (moveNo < part1MoveEnd) {
            return part1RunTimePerMove;
        } else if (moveNo < part2MoveEnd) {
            return part2RunTimePerMove;
        } else {
            return part3RunTimePerMove;
        }
    }

    @Override
    public void run(String... args) {
        log.info("Start Analyse");
        String sgfNameStr = "";
        String runTimeSecStr = "";
        for (String arg : args) {
            if (arg.startsWith("-sgfName=")) {
                sgfNameStr = arg.replace("-sgfName=", "");
            } else if (arg.startsWith("-runTimeSec=")) {
                runTimeSecStr = arg.replace("-runTimeSec=", "");
            }
        }
        if (runTimeSecStr.isEmpty()) {
            throw new IllegalArgumentException("No run time");
        }
        if (sgfNameStr.isEmpty()) {
            throw new IllegalArgumentException("No sgf name");
        }
        final Integer runTimeSec = Integer.valueOf(runTimeSecStr);
        final String sgfName = sgfNameStr;
        Game game = SgfParser.parseGame(getSgfPath(sgfName));
        if (game.getProperty("KM") == null) {
            throw new IllegalArgumentException("no komi");
        }
        if (game.getProperty("RU") == null) {
            throw new IllegalArgumentException("no rule");
        }
        AnalyseProcessState analyseProcessState = new AnalyseProcessState();
        try {

            ProcessBuilder processBuilder = new ProcessBuilder(applicationConfig.getKataGoPath(), "gtp", "-config", applicationConfig.getConfigFilePath(),
                    "-model", applicationConfig.getWeightPath(),
                    "-override-config", "reportAnalysisWinratesAs=" + reportAnalysisWinratesAs);
            Process kataGoProcess = processBuilder.start();
            ReadWinrateExecutor readWinrateExecutor = new ReadWinrateExecutor(kataGoProcess.getInputStream(), analyseProcessState, moveMetricsExtractor);
            readWinrateExecutor.start();
            CheckReadinessExecutor checkReadinessExecutor = new CheckReadinessExecutor(kataGoProcess.getErrorStream(), analyseProcessState);
            checkReadinessExecutor.start();
            RunMoveExecutor runMoveExecutor = new RunMoveExecutor(kataGoProcess.getOutputStream(), game, analyseProcessState, runTimeSec);
            runMoveExecutor.start();
            while (!analyseProcessState.isEnd) {
                Thread.sleep(2000);
            }
            readWinrateExecutor.stop();
            checkReadinessExecutor.stop();
            runMoveExecutor.stop();
            kataGoProcess.destroy();
            analyseResultExporter.export(AnalyseResult.builder().sgfName(sgfName)
                    .moveMetricsList(analyseProcessState.moveMetricsList).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getSgfPath(final String sgfName) {
        return applicationConfig.getSgfFolderPath().resolve(sgfName + ".sgf");
    }
}
