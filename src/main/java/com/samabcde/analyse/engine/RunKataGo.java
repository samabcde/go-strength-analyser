package com.samabcde.analyse.engine;

import com.samabcde.analyse.core.AnalyseMetadata;
import com.samabcde.analyse.core.AnalyseTarget;
import com.samabcde.analyse.core.ApplicationConfig;
import com.samabcde.analyse.engine.execute.AnalyseProcessState;
import com.samabcde.analyse.engine.execute.CheckReadinessExecutor;
import com.samabcde.analyse.engine.execute.ExecutorExceptionHandler;
import com.samabcde.analyse.engine.execute.ExecutorThreadFactory;
import com.samabcde.analyse.engine.execute.ReadMetricExecutor;
import com.samabcde.analyse.engine.execute.RunMoveExecutor;
import com.samabcde.analyse.info.AnalyseInfo;
import com.samabcde.analyse.info.AnalyseInfoExporter;
import com.samabcde.analyse.info.AnalyseInfoImporter;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetricExtractor;
import com.samabcde.analyse.metric.MoveMetrics;
import com.samabcde.analyse.metric.MoveMetricsScoreCalculator;
import com.samabcde.analyse.result.AnalyseResult;
import com.samabcde.analyse.result.AnalyseResultExporter;
import com.samabcde.analyse.sgf.Rank;
import com.samabcde.analyse.sgf.SgfParser;
import com.samabcde.analyse.statistic.BigDecimalSummaryStatistics;
import com.toomasr.sgf4j.parser.Game;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class RunKataGo implements CommandLineRunner {

    private final MoveMetricExtractor moveMetricExtractor;
    private final MoveMetricsScoreCalculator moveMetricsScoreCalculator;
    private final AnalyseInfoExporter analyseInfoExporter;
    private final AnalyseInfoImporter analyseInfoImporter;
    private final AnalyseResultExporter analyseResultExporter;
    private final ApplicationConfig applicationConfig;
    private final KataGoFactory kataGoFactory;

    public RunKataGo(ApplicationConfig applicationConfig, MoveMetricExtractor moveMetricExtractor, AnalyseInfoExporter analyseInfoExporter, AnalyseInfoImporter analyseInfoImporter, AnalyseResultExporter analyseResultExporter, KataGoFactory kataGoFactory, MoveMetricsScoreCalculator moveMetricScoreCalculator) {
        this.applicationConfig = applicationConfig;
        this.moveMetricExtractor = moveMetricExtractor;
        this.analyseInfoExporter = analyseInfoExporter;
        this.analyseInfoImporter = analyseInfoImporter;
        this.analyseResultExporter = analyseResultExporter;
        this.kataGoFactory = kataGoFactory;
        this.moveMetricsScoreCalculator = moveMetricScoreCalculator;
    }

    @Override
    public void run(String... args) {
        String source = args[0];
        AnalyseSource analyseSource = AnalyseSource.valueOf(source.toUpperCase());
        switch (analyseSource) {
            case INFO -> analyseWithInfo(Arrays.copyOfRange(args, 1, args.length));
            case KATA_GO -> analyseWithKataGo(Arrays.copyOfRange(args, 1, args.length));
        }
    }

    private void analyseWithInfo(String[] args) {
        String infoNameStr = "";
        for (String arg : args) {
            if (arg.startsWith("-infoName=")) {
                infoNameStr = arg.replace("-infoName=", "");
            }
        }
        List<Path> infoPaths;
        if (infoNameStr.isEmpty()) {
            infoPaths = getAnalyseInfoPaths();
        } else {
            infoPaths = Stream.of(infoNameStr.split(",")).map(this::getAnalyseInfoPath).toList();
        }
        List<AnalyseResult> analyseResults = new ArrayList<>();
        for (Path infoPath : infoPaths) {
            Resource resource = new PathResource(infoPath);
            AnalyseInfo analyseInfo = analyseInfoImporter.importInfo(resource);
            List<MoveMetric> moveMetricList = analyseInfo.getMoveInfoList().stream().map(moveMetricExtractor::extractMoveMetric).toList();
            Map<Integer, List<MoveMetric>> moveNumberToMoveMetricListMap = moveMetricList.stream().filter(moveMetric -> moveMetric.getMoveNo() > 0)
                    .collect(Collectors.groupingBy(MoveMetric::getMoveNo));
            List<MoveMetrics> moveMetricsList = moveNumberToMoveMetricListMap.entrySet().stream()
                    .map((entry) ->
                            {
                                Map<AnalyseTarget, MoveMetric> analyseTargetToMoveMetricMap = entry.getValue().stream().collect(Collectors.toMap(moveMetric -> moveMetric.getAnalyseKey().analyseTarget(), Function.identity()));
                                MoveMetrics moveMetrics = MoveMetrics.builder().moveNo(entry.getKey())
                                        .ai(analyseTargetToMoveMetricMap.get(AnalyseTarget.AI))
                                        .candidate(analyseTargetToMoveMetricMap.get(AnalyseTarget.CANDIDATE))
                                        .pass(analyseTargetToMoveMetricMap.get(AnalyseTarget.PASS)).build();
                                moveMetrics.setMoveScore(moveMetricsScoreCalculator.calculateMoveScore(moveMetrics));
                                return moveMetrics;
                            }
                    ).toList();
            AnalyseResult analyseResult = AnalyseResult.builder().metadata(analyseInfo.getMetadata())
                    .moveMetricsList(moveMetricsList).build();
            analyseResultExporter.export(analyseResult);
            analyseResults.add(analyseResult);
        }
        Map<Rank, BigDecimalSummaryStatistics> rankToStatistic = new TreeMap<>();
        Map<Rank, List<BigDecimal>> rankToGssList = new TreeMap<>();
        for (AnalyseResult analyseResult : analyseResults) {
            rankToGssList.computeIfAbsent(analyseResult.getBlackRank(), (key) -> new ArrayList<>()).add(analyseResult.getBlackStrengthScore());
            rankToGssList.computeIfAbsent(analyseResult.getWhiteRank(), (key) -> new ArrayList<>()).add(analyseResult.getWhiteStrengthScore());
        }
        rankToGssList.forEach((key, value) -> rankToStatistic.put(key, value.stream().collect(BigDecimalSummaryStatistics.statistics())));
        DecimalFormat decimalFormat = new DecimalFormat("####0.00");
        NumberFormat countFormat = DecimalFormat.getIntegerInstance();
        System.out.println("Rank,Avg,Min,Max,Std,Count");
        rankToStatistic.forEach((rank, statistic) -> System.out.println(
                Stream.of(rank.code(),
                        decimalFormat.format(statistic.getAverage()),
                        decimalFormat.format(statistic.getMin()),
                        decimalFormat.format(statistic.getMax()),
                        decimalFormat.format(statistic.getStandardDeviation()),
                        countFormat.format(statistic.getCount())).collect(Collectors.joining(","))
        ));
    }

    private void analyseWithKataGo(String[] args) {
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
        String[] sgfNames = sgfNameStr.split(",");
        for (String sgfName : sgfNames) {
            Game game = SgfParser.parseGame(getSgfPath(sgfName));
            if (game.getProperty("KM") == null) {
                throw new IllegalArgumentException("no komi");
            }
            if (game.getProperty("RU") == null) {
                throw new IllegalArgumentException("no rule");
            }
            AnalyseProcessState analyseProcessState = new AnalyseProcessState();
            ExecutorExceptionHandler exceptionHandler = new ExecutorExceptionHandler(analyseProcessState);
            ExecutorThreadFactory executorThreadFactory = new ExecutorThreadFactory(exceptionHandler);
            Process kataGoProcess = kataGoFactory.createKataGoProcess();
            ReadMetricExecutor readWinrateExecutor = new ReadMetricExecutor(new BufferedReader(new InputStreamReader(kataGoProcess.getInputStream())), analyseProcessState, moveMetricExtractor, executorThreadFactory);
            readWinrateExecutor.start();
            CheckReadinessExecutor checkReadinessExecutor = new CheckReadinessExecutor(new BufferedReader(new InputStreamReader(kataGoProcess.getErrorStream())), analyseProcessState, executorThreadFactory);
            checkReadinessExecutor.start();
            RunMoveExecutor runMoveExecutor = new RunMoveExecutor(new BufferedWriter(new OutputStreamWriter(kataGoProcess.getOutputStream())), game, analyseProcessState, runTimeSec, moveMetricExtractor, executorThreadFactory, moveMetricsScoreCalculator);
            runMoveExecutor.start();
            analyseProcessState.waitUntilEndOrErrorOccur();
            if (analyseProcessState.isEnd()) {
                AnalyseMetadata metadata = AnalyseMetadata.builder().runTimeSec(runTimeSec).sgfName(sgfName).sgf(game.getGeneratedSgf()).build();
                analyseInfoExporter.export(AnalyseInfo.builder().metadata(metadata).moveInfoList(analyseProcessState.moveInfoList).build());
                analyseResultExporter.export(AnalyseResult.builder().metadata(metadata)
                        .moveMetricsList(analyseProcessState.moveMetricsList)
                        .gameScore(moveMetricsScoreCalculator.calculateGameScore(analyseProcessState.moveMetricsList)).build());
            }
            readWinrateExecutor.stop();
            checkReadinessExecutor.stop();
            runMoveExecutor.stop();
            kataGoProcess.destroy();
            log.debug("all metrics: {}", analyseProcessState.moveMetricsList);
        }
    }

    private Path getSgfPath(final String sgfName) {
        return applicationConfig.getSgfFolderPath().resolve(sgfName + ".sgf");
    }

    private Path getAnalyseInfoPath(String analyseInfoName) {
        return applicationConfig.getAnalyseInfoFolderPath().resolve(analyseInfoName + ".json");
    }

    private List<Path> getAnalyseInfoPaths() {
        try {
            return Files.list(applicationConfig.getAnalyseInfoFolderPath()).filter(p -> p.getFileName().toString().endsWith(".json")).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
