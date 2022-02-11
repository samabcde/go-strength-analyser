package analyse;

import com.toomasr.sgf4j.parser.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RunKataGo implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(RunKataGo.class);

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

    private static BigDecimal extractScoreMean(String line) {
        Pattern pattern = Pattern.compile("scoreMean ([0-9-\\.]+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String scoreMean = matcher.group(1);
            return new BigDecimal(scoreMean);
        } else {
            throw new IllegalArgumentException("");
        }
    }

    private static BigDecimal extractWinrate(String line) {
        Pattern pattern = Pattern.compile("winrate ([0-9\\.]+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String winrate = matcher.group(1);
            return new BigDecimal(winrate).multiply(new BigDecimal("100"));
        } else {
            throw new IllegalArgumentException("");
        }
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
        logger.info("Start Analyse");
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
                            logger.info("I " + line);
                            BigDecimal winrate = extractWinrate(line);
                            BigDecimal scoreMean = extractScoreMean(line);
                            lastMoveWinrate.set(new MoveMetrics(moveNo + 1, new BigDecimal("100").subtract(winrate),
                                    scoreMean));
                        }
                        if (isCompleteAnalyze.getAndSet(false)) {
                            logger.info(line);
                            moveNo++;
                        }
                        // logger.info("debug out" + line);
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
                        logger.info(line);
                        if (line.startsWith("NN eval")) {
                            logger.info("E " + line);
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
                        logger.info("analyze move " + i);
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
            logger.info("Win Rate:");
            List<MoveMetrics> winrateChanges = MoveMetrics.calculateWinrateChanges(moveMetrics);
            for (MoveMetrics winrate : moveMetrics) {
                logger.info(
                        winrate.getMoveNo() + "\t" + winrate.getBlackWinrate() + "\t" + winrate.getBlackScoreMean());
            }

            winrateChanges.sort(Comparator.comparing(MoveMetrics::getRateChange));
            logger.info("Bad move");
            for (int i = 0; i < 3; i++) {
                MoveMetrics winrate = winrateChanges.get(i);
                System.out.print(winrate.getMoveNo() + 1 + "("
                        + winrate.getRateChange().setScale(2, RoundingMode.HALF_EVEN) + "%)");
                if (i < 2) {
                    System.out.print(", ");
                }
            }
            logger.info("");
            logger.info("Good move");
            for (int i = winrateChanges.size() - 1; i > winrateChanges.size() - 4; i--) {
                MoveMetrics winrate = winrateChanges.get(i);
                System.out.print(winrate.getMoveNo() + 1 + "("
                        + winrate.getRateChange().setScale(2, RoundingMode.HALF_EVEN) + "%)");
                if (i > winrateChanges.size() - 3) {
                    System.out.print(", ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getSgfPath(final String sgfName) {
        return sgfFolder + sgfName + ".sgf";
    }
}
