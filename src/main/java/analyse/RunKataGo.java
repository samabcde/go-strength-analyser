package analyse;

import com.toomasr.sgf4j.parser.Game;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class RunKataGo implements CommandLineRunner {

    private static volatile boolean isEnd = false;
    private static volatile boolean isReady = false;
    private static volatile int completeAnalyzeMoveNo = 0;
    private static volatile AtomicBoolean isCompleteAnalyze = new AtomicBoolean(false);
    private static volatile AtomicReference<MoveMetrics> lastMoveWinrate = new AtomicReference<>(null);
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
        final String komi = game.getProperty("KM");
        if (komi == null) {
            throw new IllegalArgumentException("no komi");
        }
        final String rule = game.getProperty("RU");
        if (rule == null) {
            throw new IllegalArgumentException("no rule");
        }
        final Integer noOfMove = game.getNoMoves();
        try {

            ProcessBuilder processBuilder = new ProcessBuilder(applicationConfig.getKataGoPath(), "gtp", "-config", applicationConfig.getConfigFilePath(),
                    "-model", applicationConfig.getWeightPath(),
                    "-override-config", "reportAnalysisWinratesAs=" + reportAnalysisWinratesAs);
            Process kataGoProcess = processBuilder.start();
            ExecutorService readInputNewSingleThreadExecutor = Executors.newSingleThreadExecutor();
            final List<MoveMetrics> moveMetricsList = new ArrayList<>();
            readInputNewSingleThreadExecutor.execute(() -> {
                try (BufferedReader input = new BufferedReader(
                        new InputStreamReader(kataGoProcess.getInputStream()));) {
                    String line;
                    int moveNo = 0;
                    while ((line = input.readLine()) != null) {
                        if (line.startsWith("info move")) {
                            log.info("I " + line);
                            lastMoveWinrate.set(moveMetricsExtractor.extractMoveMetrics(moveNo + 1, line));
                            if (isCompleteAnalyze.getAndSet(false)) {
                                log.info(line);
                                moveNo++;
                            }
                        }
                    }

                } catch (IOException e) {
                    log.error("Read kata output error", e);
                }
            });
            ExecutorService readErrorNewSingleThreadExecutor = Executors.newSingleThreadExecutor();
            readErrorNewSingleThreadExecutor.execute(() -> {
                try (BufferedReader input = new BufferedReader(
                        new InputStreamReader(kataGoProcess.getErrorStream()));) {
                    String line;

                    while ((line = input.readLine()) != null) {
                        log.info(line);
                        if (line.startsWith("NN eval")) {
                            log.info("E " + line);
                        }
                        if (line.startsWith("GTP ready, beginning main protocol loop")) {
                            isReady = true;
                        }
                    }
                } catch (IOException e) {
                    log.error("Read kata ready input error", e);
                }
            });
            ExecutorService writeOutputSingleThreadExecutor = Executors.newSingleThreadExecutor();
            writeOutputSingleThreadExecutor.execute(() -> {
                while (!isReady) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try (BufferedWriter output = new BufferedWriter(
                        new OutputStreamWriter(kataGoProcess.getOutputStream()));) {
                    List<String> moveCommands = SgfParser.toMoveCommands(game);
                    output.newLine();
                    output.append("boardsize 19");
                    output.newLine();
                    for (int i = 1; i <= noOfMove; i++) {
                        while (isCompleteAnalyze.get()) {
                            Thread.sleep(50);
                        }
                        String moveCommand = moveCommands.get(i - 1);
                        log.info("analyze move " + i + " " + moveCommand);
                        int analyseTimeMs = calculateAnalyseTimeMs(noOfMove, runTimeSec, i);
                        output.append("play " + moveCommand);
                        output.newLine();
                        output.append("komi " + komi);
                        output.newLine();
                        output.append("kata-analyze " + 50);
                        output.newLine();
                        output.flush();
                        Thread.sleep(analyseTimeMs);
                        isCompleteAnalyze.compareAndSet(false, true);
                        while (lastMoveWinrate.get() == null) {
                            Thread.sleep(50);
                        }
                        Thread.sleep(50);
                        moveMetricsList.add(lastMoveWinrate.getAndSet(null));
                    }
                    output.append("quit");
                    isEnd = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            while (!isEnd) {
                Thread.sleep(2000);
            }
            readInputNewSingleThreadExecutor.shutdown();
            readErrorNewSingleThreadExecutor.shutdown();
            writeOutputSingleThreadExecutor.shutdown();
            kataGoProcess.destroy();
            analyseResultExporter.export(AnalyseResult.builder().sgfName(sgfName)
                    .moveMetricsList(moveMetricsList).build());
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
