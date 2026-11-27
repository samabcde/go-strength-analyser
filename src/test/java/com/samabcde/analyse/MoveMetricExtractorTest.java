package com.samabcde.analyse;

import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.core.AnalyseTarget;
import com.samabcde.analyse.engine.execute.AnalyseProcessState;
import com.samabcde.analyse.info.MoveInfo;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetricExtractor;
import com.samabcde.analyse.metric.MoveVariation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoveMetricExtractorTest {

    private final MoveMetricExtractor moveMetricExtractor = new MoveMetricExtractor();

    @Test
    public void Given_validInput_Should_return_correctResult() {
        String info = "info move L15 visits 121 edgeVisits 12 utility -1.07502 winrate 0.00955206 scoreMean -28.424 scoreStdev 19.5573 scoreLead -28.424 scoreSelfplay -28.0574 prior 0.209432 lcb -0.0022216 utilityLcb -1.10799 weight 10 order 0 pv L15 E5 M15 C13 C17 C18 D16 " +
                "info move H2 visits 153 edgeVisits 12 utility -1.08721 winrate 0.0115132 scoreMean -29.8217 scoreStdev 20.0817 scoreLead -29.8217 scoreSelfplay -29.389 prior 0.373352 lcb -0.00294137 utilityLcb -1.12768 weight 10 order 1 pv H2 H8 L15 L10 N12 G9 M15 M10 M11 C13 " +
                "info move G2 visits 130 edgeVisits 12 utility -1.08636 winrate 0.0108509 scoreMean -29.2966 scoreStdev 20.1815 scoreLead -29.2966 scoreSelfplay -29.0387 prior 0.299997 lcb -0.000797726 utilityLcb -1.11897 weight 10 order 2 pv G2 H8 L15 G9 M15 L10 M11 M10 N11 N15 " +
                "info move H8 visits 49 edgeVisits 12 utility -1.07285 winrate 0.0129406 scoreMean -27.5248 scoreStdev 17.6893 scoreLead -27.5248 scoreSelfplay -27.4359 prior 0.0198256 lcb -0.0161538 utilityLcb -1.15431 weight 10 order 3 pv H8 D5 L15 L16 M15 N15 M16 " +
                "info move L10 visits 25 edgeVisits 12 utility -1.10385 winrate 0.00399732 scoreMean -28.1726 scoreStdev 15.6698 scoreLead -28.1726 scoreSelfplay -28.2602 prior 0.0379175 lcb -0.00740477 utilityLcb -1.13578 weight 10 order 4 pv L10 H8 G2 G9 C13 " +
                "info move B4 visits 5 edgeVisits 12 utility -1.16511 winrate 0.00396565 scoreMean -40.3397 scoreStdev 21.6219 scoreLead -40.3397 scoreSelfplay -39.5244 prior 0.0176333 lcb -0.0933563 utilityLcb -1.43761 weight 10 order 5 pv B4 D5 L15 M15 " +
                "info move L9 visits 3 edgeVisits 12 utility -1.17294 winrate 0.00463912 scoreMean -37.4653 scoreStdev 19.8482 scoreLead -37.4653 scoreSelfplay -36.7008 prior 0.00232647 lcb -1.17633 utilityLcb -4.47965 weight 10 order 6 pv L9 H8 Q15 " +
                "info move Q15 visits 1 edgeVisits 12 utility -1.15063 winrate 0.00702433 scoreMean -37.7183 scoreStdev 18.1302 scoreLead -37.7183 scoreSelfplay -36.0593 prior 0.00409325 lcb -0.992976 utilityLcb -2.8 weight 10 order 7 pv Q15 " +
                "info move C5 visits 1 edgeVisits 12 utility -1.20293 winrate 0.00577182 scoreMean -48.6297 scoreStdev 20.2033 scoreLead -48.6297 scoreSelfplay -45.184 prior 0.00357019 lcb -0.994228 utilityLcb -2.8 weight 10 order 8 pv C5";
        AnalyseKey analyseKey = new AnalyseKey(AnalyseTarget.CANDIDATE, 1, "A1");
        MoveInfo moveInfo = new MoveInfo(analyseKey, info);
        MoveMetric actual = moveMetricExtractor.extractMoveMetric(moveInfo);
        assertEquals(analyseKey, actual.getAnalyseKey());
        assertEquals("L15", actual.getBestMove());
        assertEquals(BigDecimal.valueOf(0.00955206), actual.getWinrate());
        assertEquals(BigDecimal.valueOf(-28.424), actual.getScoreLead());
        assertEquals(List.of(
                new MoveVariation(0, List.of("L15", "E5", "M15", "C13", "C17", "C18", "D16")),
                new MoveVariation(1, List.of("H2", "H8", "L15", "L10", "N12", "G9", "M15", "M10", "M11", "C13")),
                new MoveVariation(2, List.of("G2", "H8", "L15", "G9", "M15", "L10", "M11", "M10", "N11", "N15"))
        ), actual.getBestVariations().subList(0, 3));
        assertEquals(9, actual.getBestVariations().size());
    }

    @Test
    public void Given_InputMoveHasPass_Should_return_correctResult() {
        String info = "info move pass visits 121 edgeVisits 8 utility -1.07502 winrate 0.00955206 scoreMean -28.424 scoreStdev 19.5573 scoreLead -28.424 scoreSelfplay -28.0574 prior 0.209432 lcb -0.0022216 utilityLcb -1.10799 weight 9 order 0 pv L15 pass M15 C13 C17 C18 D16 " +
                "info move H2 visits 153 edgeVisits 8 utility -1.08721 winrate 0.0115132 scoreMean -29.8217 scoreStdev 20.0817 scoreLead -29.8217 scoreSelfplay -29.389 prior 0.373352 lcb -0.00294137 utilityLcb -1.12768 weight 9 order 1 pv H2 H8 L15 L10 N12 G9 M15 M10 M11 C13 " +
                "info move G2 visits 130 edgeVisits 8 utility -1.08636 winrate 0.0108509 scoreMean -29.2966 scoreStdev 20.1815 scoreLead -29.2966 scoreSelfplay -29.0387 prior 0.299997 lcb -0.000797726 utilityLcb -1.11897 weight 9 order 2 pv G2 H8 L15 G9 M15 L10 M11 M10 N11 N15 " +
                "info move H8 visits 49 edgeVisits 8 utility -1.07285 winrate 0.0129406 scoreMean -27.5248 scoreStdev 17.6893 scoreLead -27.5248 scoreSelfplay -27.4359 prior 0.0198256 lcb -0.0161538 utilityLcb -1.15431 weight 9 order 3 pv H8 D5 L15 L16 M15 N15 M16 " +
                "info move L10 visits 25 edgeVisits 8 utility -1.10385 winrate 0.00399732 scoreMean -28.1726 scoreStdev 15.6698 scoreLead -28.1726 scoreSelfplay -28.2602 prior 0.0379175 lcb -0.00740477 utilityLcb -1.13578 weight 9 order 4 pv L10 H8 G2 G9 C13 " +
                "info move B4 visits 5 edgeVisits 8 utility -1.16511 winrate 0.00396565 scoreMean -40.3397 scoreStdev 21.6219 scoreLead -40.3397 scoreSelfplay -39.5244 prior 0.0176333 lcb -0.0933563 utilityLcb -1.43761 weight 9 order 5 pv B4 D5 L15 M15 " +
                "info move L9 visits 3 edgeVisits 8 utility -1.17294 winrate 0.00463912 scoreMean -37.4653 scoreStdev 19.8482 scoreLead -37.4653 scoreSelfplay -36.7008 prior 0.00232647 lcb -1.17633 utilityLcb -4.47965 weight 9 order 6 pv L9 H8 Q15 " +
                "info move Q15 visits 1 edgeVisits 8 utility -1.15063 winrate 0.00702433 scoreMean -37.7183 scoreStdev 18.1302 scoreLead -37.7183 scoreSelfplay -36.0593 prior 0.00409325 lcb -0.992976 utilityLcb -2.8 weight 9 order 7 pv Q15 " +
                "info move C5 visits 1 edgeVisits 8 utility -1.20293 winrate 0.00577182 scoreMean -48.6297 scoreStdev 20.2033 scoreLead -48.6297 scoreSelfplay -45.184 prior 0.00357019 lcb -0.994228 utilityLcb -2.8 weight 9 order 8 pv C5";
        AnalyseKey analyseKey = new AnalyseKey(AnalyseTarget.CANDIDATE, 1, "A1");
        MoveInfo moveInfo = new MoveInfo(analyseKey, info);
        MoveMetric actual = moveMetricExtractor.extractMoveMetric(moveInfo);
        assertEquals("pass", actual.getBestMove());
        assertEquals(BigDecimal.valueOf(0.00955206), actual.getWinrate());
        assertEquals(BigDecimal.valueOf(-28.424), actual.getScoreLead());
        assertEquals("L15", actual.getBestVariations().getFirst().firstMove());
    }

    @Test
    public void Given_invalidInput_Should_throw_IllegalArgumentException() {
        String info = "random";
        MoveInfo moveInfo = new MoveInfo(new AnalyseKey(AnalyseTarget.CANDIDATE, 1, "A1"), info);
        assertThrows(IllegalArgumentException.class, () -> moveMetricExtractor.extractMoveMetric(moveInfo));
    }

    @Test
    void Given_updatesAcrossLines_ShouldKeepLatestInfoPerVariationOrder() {
        AnalyseKey analyseKey = new AnalyseKey(AnalyseTarget.CANDIDATE, 1, "A1");
        AnalyseProcessState state = new AnalyseProcessState();
        state.setLastMoveInfo(new MoveInfo(analyseKey, infoLine("L15", 0, "L15 E5", "0.4", "-2")));
        state.setLastMoveInfo(new MoveInfo(analyseKey, infoLine("G4", 0, "G4 D16 Q4", "0.5", "-1")));
        state.setLastMoveInfo(new MoveInfo(analyseKey, infoLine("H2", 1, "H2 H8", "0.3", "-3")));

        MoveMetric actual = moveMetricExtractor.extractMoveMetric(state.getLastMoveInfo());

        assertEquals("G4", actual.getBestMove());
        assertEquals(2, actual.getBestVariations().size());
        assertEquals(List.of("G4", "D16", "Q4"), actual.getBestVariations().getFirst().moves());
        assertEquals("H2", actual.getBestVariations().get(1).firstMove());
    }

    @Test
    void Given_kataGoSymmetryInfo_ShouldExtractVariation() {
        String info = "info move Q4 visits 1 edgeVisits 1 utility 0.147465 winrate 0.572906 "
                + "scoreMean 0.00713979 scoreStdev 14.2516 scoreLead 0.00713979 "
                + "scoreSelfplay 0.44244 prior 0.178198 lcb -0.677094 utilityLcb -3.35253 "
                + "weight 3.247 isSymmetryOf Q16 order 1 pv Q4 D16";
        MoveInfo moveInfo = new MoveInfo(new AnalyseKey(AnalyseTarget.CANDIDATE, 1, "Q4"), info);

        MoveMetric actual = moveMetricExtractor.extractMoveMetric(moveInfo);

        assertEquals("Q4", actual.getBestMove());
        assertEquals(new BigDecimal("0.572906"), actual.getWinrate());
        assertEquals(new BigDecimal("0.00713979"), actual.getScoreLead());
        assertEquals(List.of("Q4", "D16"), actual.getBestVariations().getFirst().moves());
    }

    private static String infoLine(String move, int order, String pv, String winrate, String scoreLead) {
        return "info move " + move + " visits 100 edgeVisits 10 utility -1.0 winrate " + winrate
                + " scoreMean " + scoreLead + " scoreStdev 1.0 scoreLead " + scoreLead
                + " scoreSelfplay 0.0 prior 0.1 lcb -0.1 utilityLcb -1.1 weight 10 order " + order + " pv " + pv;
    }
}