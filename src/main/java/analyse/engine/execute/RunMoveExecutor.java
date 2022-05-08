package analyse.engine.execute;

import analyse.core.AnalyseKey;
import analyse.core.AnalyseTarget;
import analyse.engine.RunKataGo;
import analyse.info.MoveInfo;
import analyse.metric.MoveMetric;
import analyse.metric.MoveMetricExtractor;
import analyse.metric.MoveMetricsScoreCalculator;
import analyse.metric.MoveMetrics;
import analyse.sgf.SgfParser;
import com.toomasr.sgf4j.parser.Game;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class RunMoveExecutor extends AbstractExecutor {
    private final OutputStream outputStream;
    private final MoveMetricExtractor moveMetricExtractor;
    private final MoveMetricsScoreCalculator moveMetricsScoreCalculator;
    private final Integer runTimeSec;
    private final Game game;

    public RunMoveExecutor(OutputStream outputStream, Game game, AnalyseProcessState analyseProcessState, Integer runTimeSec, MoveMetricExtractor moveMetricExtractor, ThreadFactory threadFactory, MoveMetricsScoreCalculator moveMetricsScoreCalculator) {
        super(analyseProcessState, threadFactory);
        this.outputStream = outputStream;
        this.game = game;
        this.runTimeSec = runTimeSec;
        this.moveMetricExtractor = moveMetricExtractor;
        this.moveMetricsScoreCalculator = moveMetricsScoreCalculator;
    }

    @Override
    protected Runnable task() {
        return () -> {
            final String komi = game.getProperty("KM");
            final String rule = game.getProperty("RU");
            final Integer noOfMove = game.getNoMoves();
            while (!analyseProcessState.isReady) {
                sleep(50);
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
                MoveMetric initial = analyzeMove(output, "", noOfMove, new AnalyseKey(AnalyseTarget.CANDIDATE, 0, ""));
                String aiMove = initial.getBestMove();
                for (int moveNo = 1; moveNo <= noOfMove; moveNo++) {
                    String moveCommand = moveCommands.get(moveNo - 1);
                    MoveMetric candidate = analyzeMove(output, moveCommand, noOfMove, new AnalyseKey(AnalyseTarget.CANDIDATE, moveNo, moveCommand.split(" ")[1]));
                    MoveMetric ai = analyzeMove(output, moveCommand.split(" ")[0] + " " + aiMove, noOfMove, new AnalyseKey(AnalyseTarget.AI, moveNo, aiMove));
                    aiMove = candidate.getBestMove();
                    MoveMetric pass = analyzeMove(output, moveCommand.split(" ")[0] + " " + "pass", noOfMove, new AnalyseKey(AnalyseTarget.PASS, moveNo, "pass"));
                    analyseProcessState.moveMetricsList.add(moveMetricsScoreCalculator.calculateScore(MoveMetrics.builder().moveNo(moveNo).ai(ai).candidate(candidate).pass(pass).build()));
                    output.append("play " + moveCommand);
                    output.newLine();
                    output.flush();
                }
                output.append("quit");
                analyseProcessState.isEnd = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private MoveMetric analyzeMove(BufferedWriter output, String moveCommand, int noOfMove, AnalyseKey analyseKey) throws IOException {
        analyseProcessState.currentAnalyseKey.set(analyseKey);
        log.info("analyze " + analyseKey.analyseTarget() + " move " + analyseKey.moveNo() + " with " + analyseKey.move());
        int analyseTimeMs = RunKataGo.calculateAnalyseTimeMs(noOfMove, runTimeSec, analyseKey.moveNo());

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
        while (analyseProcessState.lastMoveInfo.get() == null) {
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
        MoveInfo moveInfo = analyseProcessState.lastMoveInfo.getAndSet(null);
        if (!moveInfo.analyseKey().equals(analyseProcessState.currentAnalyseKey.get())) {
            throw new IllegalStateException("result analyse key:" + moveInfo.analyseKey() + " current: " + analyseProcessState.currentAnalyseKey.get() + " not match");
        }
        analyseProcessState.moveInfoList.add(moveInfo);
        return moveMetricExtractor.extractMoveMetric(moveInfo);
    }

}
