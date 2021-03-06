package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.core.AnalyseTarget;
import com.samabcde.analyse.engine.RunKataGo;
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
import java.util.List;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class RunMoveExecutor extends AbstractExecutor {
    private final BufferedWriter writer;
    private final MoveMetricExtractor moveMetricExtractor;
    private final MoveMetricsScoreCalculator moveMetricsScoreCalculator;
    private final Integer runTimeSec;
    private final Game game;

    public RunMoveExecutor(BufferedWriter writer, Game game, AnalyseProcessState analyseProcessState, Integer runTimeSec, MoveMetricExtractor moveMetricExtractor, ThreadFactory threadFactory, MoveMetricsScoreCalculator moveMetricsScoreCalculator) {
        super(analyseProcessState, threadFactory);
        this.writer = writer;
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
            try (BufferedWriter output = writer) {
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
                    MoveMetrics moveMetrics = MoveMetrics.builder().moveNo(moveNo).ai(ai).candidate(candidate).pass(pass).build();
                    moveMetrics.setMoveScore(moveMetricsScoreCalculator.calculateMoveScore(moveMetrics));
                    analyseProcessState.moveMetricsList.add(moveMetrics);
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
