package analyse;

import com.toomasr.sgf4j.parser.Game;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Log
public class RunKataGo implements CommandLineRunner {

    @Value("${path.kataGoPath}")
    private String kataGoPath;
    @Value("${path.sgfFolder}")
    private String sgfFolder;
    @Value("${path.weightPath}")
    private String weightPath;
    @Value("${path.configFilePath}")
    private String configFilePath;
    public static volatile boolean isEnd = false;
    public static volatile boolean isReady = false;
    public static volatile int completeAnalyzeMoveNo = 0;
    private static volatile AtomicBoolean isCompleteAnalyze = new AtomicBoolean(false);
    private static volatile AtomicReference<MoveMetrics> lastMoveWinrate = new AtomicReference<>(null);
    private static final String reportAnalysisWinratesAs = "SIDETOMOVE";
    private final MoveMetricsExtractor moveMetricsExtractor;

    public RunKataGo(MoveMetricsExtractor moveMetricsExtractor) {
        this.moveMetricsExtractor = moveMetricsExtractor;
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
        final Integer noOfMove = game.getNoMoves();
        try {

            ProcessBuilder processBuilder = new ProcessBuilder(kataGoPath, "gtp", "-config", configFilePath,
                    "-model", weightPath,
                    "-override-config", "reportAnalysisWinratesAs=" + reportAnalysisWinratesAs);
            Process kataGoProcess = processBuilder.start();
            ExecutorService readInputNewSingleThreadExecutor = Executors.newSingleThreadExecutor();
            final List<MoveMetrics> moveMetrics = new ArrayList<>();
            readInputNewSingleThreadExecutor.execute(() -> {
                try (BufferedReader input = new BufferedReader(
                        new InputStreamReader(kataGoProcess.getInputStream()));) {
                    String line;
                    int moveNo = 0;
                    MoveMetrics moveWinRate = null;
                    while ((line = input.readLine()) != null) {
                        if (line.startsWith("info move")) {
                            log.info("I " + line);
                            lastMoveWinrate.set(moveMetricsExtractor.extractMoveMetrics(moveNo + 1, line));
                        }
                        if (isCompleteAnalyze.getAndSet(false)) {
                            log.info(line);
                            moveNo++;
                        }
                        // log.info("debug out" + line);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
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
                    e.printStackTrace();
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

                    output.newLine();
                    output.append("boardsize 19");
                    output.newLine();
                    for (int i = 1; i <= noOfMove; i++) {
                        while (isCompleteAnalyze.get()) {
                            Thread.sleep(50);
                        }
                        log.info("analyze move " + i);
                        int analyseTimeMs = calculateAnalyseTimeMs(noOfMove, runTimeSec, i);
                        String sgfPath = getSgfPath(sgfName);
                        output.append("loadsgf " + sgfPath + " " + i);
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
                        output.append("name");
                        output.newLine();
                        output.flush();
                        Thread.sleep(50);
                        moveMetrics.add(lastMoveWinrate.getAndSet(null));
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
            log.info("Win Rate:");
            List<MoveMetrics> winrateChanges = MoveMetrics.calculateWinrateChanges(moveMetrics);
            for (MoveMetrics winrate : moveMetrics) {
                log.info(
                        winrate.getMoveNo() + "\t" + winrate.getBlackWinrate() + "\t" + winrate.getBlackScoreMean());
            }

            winrateChanges.sort(Comparator.comparing(MoveMetrics::getRateChange));
            log.info("Bad move");
            for (int i = 0; i < 3; i++) {
                MoveMetrics winrate = winrateChanges.get(i);
                System.out.print(winrate.getMoveNo() + 1 + "("
                        + winrate.getRateChange().setScale(2, RoundingMode.HALF_EVEN) + "%)");
                if (i < 2) {
                    System.out.print(", ");
                }
            }
            log.info("");
            log.info("Good move");
            for (int i = winrateChanges.size() - 1; i > winrateChanges.size() - 4; i--) {
                MoveMetrics winrate = winrateChanges.get(i);
                System.out.print(winrate.getMoveNo() + 1 + "("
                        + winrate.getRateChange().setScale(2, RoundingMode.HALF_EVEN) + "%)");
                if (i > winrateChanges.size() - 3) {
                    System.out.print(", ");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSgfPath(final String sgfName) {
        return sgfFolder + sgfName + ".sgf";
    }
}
