package com.samabcde.analyse.formula;

import com.samabcde.analyse.calculate.MoveScore;
import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.core.AnalyseTarget;
import com.samabcde.analyse.formula.FormulaV1;
import com.samabcde.analyse.formula.Version;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FormulaV1Test {
    FormulaV1 formulaV1 = new FormulaV1();

    @Test
    void version() {
        assertEquals(Version.V1, formulaV1.version());
    }

    @ParameterizedTest(name="{0}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
                        description | aiScoreLead | aiWinrate | candidateScoreLead | candidateWinrate | passScoreLead | passWinrate | expectedScoreLeadScore | expectedWinrateScore | expectedIntegrated
    candidate is mid of ai and pass |          -1 |         0 |                  0 |              0.5 |             1 |           1 |                      0 |                    0 |                  0
            candidate is same as ai |          -1 |         0 |                 -1 |                0 |             1 |           1 |                      1 |                    1 |                  1
          candidate is same as pass |          -1 |         0 |                  1 |                1 |             1 |           1 |                     -1 |                   -1 |                 -1
           candidate better than ai |          -1 |         0 |                 -2 |                0 |             1 |           1 |                      1 |                    1 |                  1
          candidate worse than pass |          -1 |         0 |                  2 |                1 |             1 |         0.9 |                     -1 |                   -1 |                 -1
              integrated is average |          -1 |         0 |                0.5 |             0.25 |             1 |           1 |                   -0.5 |                  0.5 |                  0
            """)
    void calculateMove(String description, BigDecimal aiScoreLead, BigDecimal aiWinrate, BigDecimal candidateScoreLead, BigDecimal candidateWinrate, BigDecimal passScoreLead, BigDecimal passWinrate,
                       BigDecimal expectedScoreLeadScore, BigDecimal expectedWinrateScore, BigDecimal expectedIntegrated) {
        MoveMetrics moveMetrics = MoveMetrics.builder()
                .moveNo(1)
                .ai(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.AI, 1, "C3"))
                        .bestMove("A2")
                        .scoreLead(aiScoreLead)
                        .winrate(aiWinrate)
                        .build())
                .candidate(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.CANDIDATE, 1, "C2"))
                        .bestMove("A3")
                        .scoreLead(candidateScoreLead)
                        .winrate(candidateWinrate)
                        .build())
                .pass(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.PASS, 1, "pass"))
                        .bestMove("A19")
                        .scoreLead(passScoreLead)
                        .winrate(passWinrate)
                        .build())
                .build();
        MoveScore moveScore = formulaV1.calculateMove(moveMetrics);
        assertThat(moveScore.scoreLeadScore()).isEqualByComparingTo(expectedScoreLeadScore);
        assertThat(moveScore.winrateScore()).isEqualByComparingTo(expectedWinrateScore);
        assertThat(moveScore.integrated()).isEqualByComparingTo(expectedIntegrated);
    }

}