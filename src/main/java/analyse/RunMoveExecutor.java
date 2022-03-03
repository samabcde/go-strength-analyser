package analyse;

import com.toomasr.sgf4j.parser.Game;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RunMoveExecutor {
    private final OutputStream outputStream;
    private final AnalyseProcessState analyseProcessState;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Integer runTimeSec;
    private final Game game;

    public RunMoveExecutor(OutputStream outputStream, Game game, AnalyseProcessState analyseProcessState, Integer runTimeSec) {
        this.outputStream = outputStream;
        this.game = game;
        this.analyseProcessState = analyseProcessState;
        this.runTimeSec = runTimeSec;
    }

    void start() {
        final String komi = game.getProperty("KM");
        final String rule = game.getProperty("RU");
        final Integer noOfMove = game.getNoMoves();
        final List<MoveMetrics> moveMetricsList = analyseProcessState.moveMetricsList;
        executorService.execute(() -> {
            while (!analyseProcessState.isReady) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try (BufferedWriter output = new BufferedWriter(
                    new OutputStreamWriter(outputStream))) {
                List<String> moveCommands = SgfParser.toMoveCommands(game);
                output.newLine();
                output.append("boardsize 19");
                output.newLine();
                for (int i = 1; i <= noOfMove; i++) {
                    while (analyseProcessState.isCompleteAnalyze.get()) {
                        Thread.sleep(50);
                    }
                    String moveCommand = moveCommands.get(i - 1);
                    log.info("analyze move " + i + " " + moveCommand);
                    int analyseTimeMs = RunKataGo.calculateAnalyseTimeMs(noOfMove, runTimeSec, i);
                    output.append("play " + moveCommand);
                    output.newLine();
                    output.append("komi " + komi);
                    output.newLine();
                    output.append("kata-analyze " + 50);
                    output.newLine();
                    output.flush();
                    Thread.sleep(analyseTimeMs);
                    analyseProcessState.isCompleteAnalyze.compareAndSet(false, true);
                    while (analyseProcessState.lastMoveWinrate.get() == null) {
                        Thread.sleep(50);
                    }
                    Thread.sleep(50);
                    moveMetricsList.add(analyseProcessState.lastMoveWinrate.getAndSet(null));
                }
                output.append("quit");
                analyseProcessState.isEnd = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    void stop() {
        executorService.shutdown();
    }
}
