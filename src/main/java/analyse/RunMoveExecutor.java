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
        executorService.execute(() -> {
            while (!analyseProcessState.isReady) {
                try {
                    Thread.sleep(50);
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
                output.append("komi " + komi);
                output.newLine();
                output.flush();
                MoveMetric initial = analyzeMove(output, "", noOfMove);
                String aiMove = initial.getMove();
                for (int i = 1; i <= noOfMove; i++) {
                    analyseProcessState.currentMoveNo = i;
                    String moveCommand = moveCommands.get(i - 1);
                    MoveMetric candidate = analyzeMove(output, moveCommand, noOfMove);
                    MoveMetric ai = analyzeMove(output, moveCommand.split(" ")[0] + " " + aiMove, noOfMove);
                    aiMove = candidate.getMove();
                    MoveMetric pass = analyzeMove(output, moveCommand.split(" ")[0] + " " + "pass", noOfMove);
                    analyseProcessState.moveMetricsList.add(MoveMetrics.builder().moveNo(i).ai(ai).candidate(candidate).pass(pass).build());
                    output.append("play " + moveCommand);
                    output.newLine();
                    output.flush();
                }
                output.append("quit");
                analyseProcessState.isEnd = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private MoveMetric analyzeMove(BufferedWriter output, String moveCommand, int noOfMove) throws IOException {
        log.info("analyze move " + analyseProcessState.currentMoveNo + " with " + moveCommand);
        int analyseTimeMs = RunKataGo.calculateAnalyseTimeMs(noOfMove, runTimeSec, analyseProcessState.currentMoveNo);
        if (!moveCommand.isEmpty()) {
            output.append("play " + moveCommand);
            output.newLine();
            output.flush();
        }
        // 1 ms = 0.001s
        // 1 centiSec = 0.01s
        int intervalCentiSec = Math.max(analyseTimeMs / 10, 1);
        output.append("kata-analyze " + intervalCentiSec);
        output.newLine();
        output.flush();
        sleep(analyseTimeMs);
        while (analyseProcessState.lastMoveMetric.get() == null) {
            sleep(5);
        }
        output.append("protocol_version");
        output.newLine();
        output.flush();
        if (!moveCommand.isEmpty()) {
            output.append("undo");
            output.newLine();
            output.flush();
        }
        while (!analyseProcessState.isCompleteAnalyze.compareAndSet(true, false)) {
            sleep(5);
        }
        MoveMetric result = analyseProcessState.lastMoveMetric.getAndSet(null);
        if (result.getMoveNo() != analyseProcessState.currentMoveNo) {
            throw new IllegalStateException("result move no.:" + result.getMoveNo() + " current: " + analyseProcessState.currentMoveNo);
        }
        return result;
    }

    void stop() {
        executorService.shutdown();
    }

    private static void sleep(long milisec) {
        try {
            Thread.sleep(milisec);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
