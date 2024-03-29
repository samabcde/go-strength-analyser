package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.core.AnalyseTarget;
import com.samabcde.analyse.engine.AnalyseTimeCalculator;
import com.samabcde.analyse.info.MoveInfo;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetricExtractor;
import com.samabcde.analyse.metric.MoveMetrics;
import com.samabcde.analyse.metric.MoveMetricsScoreCalculator;
import com.samabcde.analyse.sgf.SgfParser;
import com.toomasr.sgf4j.parser.Game;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class RunMoveExecutor extends AbstractExecutor {
    private final BufferedWriter writer;
    private final MoveMetricExtractor moveMetricExtractor;
    private final MoveMetricsScoreCalculator moveMetricsScoreCalculator;
    private final Integer runTimeSec;
    private final Game game;
    private final AnalyseTimeCalculator calculator;

    public RunMoveExecutor(BufferedWriter writer, Game game, AnalyseProcessState analyseProcessState, Integer runTimeSec, MoveMetricExtractor moveMetricExtractor, ThreadFactory threadFactory, MoveMetricsScoreCalculator moveMetricsScoreCalculator) {
        super(analyseProcessState, threadFactory);
        this.writer = writer;
        this.game = game;
        this.runTimeSec = runTimeSec;
        this.moveMetricExtractor = moveMetricExtractor;
        this.moveMetricsScoreCalculator = moveMetricsScoreCalculator;
        this.calculator = new AnalyseTimeCalculator(game.getNoMoves(), runTimeSec);
    }

    @Override
    protected Runnable task() {
        return () -> {
            final String komi = game.getProperty("KM");
            final String rule = game.getProperty("RU");
            final Integer noOfMove = game.getNoMoves();
            analyseProcessState.waitUntilReady();
            try (BufferedWriter output = writer) {
                output.newLine();
                output.append("boardsize 19");
                output.newLine();
                output.append("komi " + komi);
                output.newLine();
                output.flush();
                if (noOfMove > 0) {
                    List<String> moveCommands = SgfParser.toMoveCommands(game);
                    MoveMetric initial = analyzeMove(output, "", noOfMove, new AnalyseKey(AnalyseTarget.CANDIDATE, 0, ""));
                    String aiMove = initial.getBestMove();
                    for (int moveNo = 1; moveNo <= noOfMove; moveNo++) {
                        String moveCommand = moveCommands.get(moveNo - 1);
                        MoveMetric candidate = analyzeMove(output, moveCommand, noOfMove, new AnalyseKey(AnalyseTarget.CANDIDATE, moveNo, moveCommand.split(" ")[1]));
                        MoveMetric ai = analyzeMove(output, moveCommand.split(" ")[0] + " " + aiMove, noOfMove, new AnalyseKey(AnalyseTarget.AI, moveNo, aiMove));
                        aiMove = candidate.getBestMove();
                        MoveMetric pass = analyzeMove(output, moveCommand.split(" ")[0] + " " + "pass", noOfMove, new AnalyseKey(AnalyseTarget.PASS, moveNo, "pass"));
                        MoveMetrics moveMetrics = MoveMetrics.builder().moveNo(moveNo).ai(ai).candidate(candidate).pass(pass).build();
                        moveMetrics.setMoveScore(moveMetricsScoreCalculator.calculateMoveScore(moveMetrics));
                        analyseProcessState.moveMetricsList.add(moveMetrics);
                        output.append("play " + moveCommand);
                        output.newLine();
                        output.flush();
                    }
                }
                output.append("quit").flush();
                analyseProcessState.end();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private MoveMetric analyzeMove(BufferedWriter output, String moveCommand, int noOfMove, AnalyseKey analyseKey) throws IOException {
        analyseProcessState.setCurrentAnalyseKey(analyseKey);
        log.info("analyze " + analyseKey.analyseTarget() + " move " + analyseKey.moveNo() + " with " + analyseKey.move());
//        int analyseTimeMs = RunKataGo.calculateAnalyseTimeMs(noOfMove, runTimeSec, analyseKey.moveNo());
        int analyseTimeMs = calculator.getMoveAnalyseTimeMs(analyseKey.moveNo());

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
        analyseProcessState.waitUntilLastMoveInfoSet();
        output.append("protocol_version");
        output.newLine();
        output.flush();
        if (!moveCommand.isEmpty()) {
            output.append("undo");
            output.newLine();
            output.flush();
        }
        analyseProcessState.waitUntilCompleteAnalyseThenReset();
        MoveInfo moveInfo = analyseProcessState.getAndResetLastMoveInfo();
        if (!moveInfo.analyseKey().equals(analyseProcessState.getCurrentAnalyseKey())) {
            throw new IllegalStateException("result analyse key:" + moveInfo.analyseKey() + " current: " + analyseProcessState.getCurrentAnalyseKey() + " not match");
        }
        analyseProcessState.moveInfoList.add(moveInfo);
        return moveMetricExtractor.extractMoveMetric(moveInfo);
    }

}
