package com.samabcde.analyse.sgf;

import com.toomasr.sgf4j.parser.Game;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SgfParserTest {
    @Test
    void convertsSgfCoordinatesToKataGoCoordinatesWithInvertedRows() {
        Game game = SgfParser.parseGame("(;SZ[19];B[pd];W[dp];B[aa];W[ss])");

        assertEquals(List.of("B Q16", "W D4", "B A19", "W T1"), SgfParser.toMoveCommands(game));
    }

}
