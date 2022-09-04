package com.samabcde.analyse.sgf;

import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SgfParser {
    private static final String sgf = "(;GM[1]FF[4]CA[UTF-8]AP[Sabaki:0.52.0]RU[japanese]KM[6.5]SZ[19]DT[2022-02-20];B[pd];W[dp];B[pq];W[dd];B[qk];W[gq];B[sa];W[gc];B[sb];W[dj];B[sc];W[jj];B[sd];W[kd];B[se];W[kp];B[sf];W[nc];B[sg];W[nq];B[sh];W[nj];B[si];W[ne];B[sj];W[ng];B[sk];W[nm];B[sl];W[no];B[sm];W[cp];B[sn];W[cd];B[so];W[ob];B[sp];W[or];B[sq];W[od];B[sr];W[op];B[ss];W[])";

    public static void main(String[] args) {
        int totalTimeMs = 0;
        System.out.println(totalTimeMs);

        Game game = Sgf.createFromString(sgf);
        System.out.println(game.getNoMoves());
        System.out.println(game.getProperties());
        GameNode node = game.getRootNode();
        do {
            System.out.println(node.getColor() + " " + node.getMoveString() + " " + (node.getMoveString() == null || node.getMoveString().equals("") ? "" : Arrays.toString(node.getCoords())));
        } while ((node = node.getNextNode()) != null);
        List<String> moveCommands = toMoveCommands(game);
        moveCommands.forEach(System.out::println);
    }

    public static Game parseGame(Path sgfPath) {
        return Sgf.createFromPath(sgfPath);
    }

    public static Game parseGame(String sgfString) {
        return Sgf.createFromString(sgfString);
    }

    public static List<String> toMoveCommands(Game game) {
        List<String> moveCommands = new ArrayList<>();
        GameNode node = game.getFirstMove();
        if (node == null) {
            return moveCommands;
        }
        do {
            if (!node.getProperties().containsKey("B") && !node.getProperties().containsKey("W")) {
                continue;
            }
            moveCommands.add(node.getColor() + " " + (node.getMoveString().equals("") ? "pass" :
                    Util.alphabet[node.getCoords()[0]] + (node.getCoords()[1] + 1)));
            System.out.println(node.getColor() + " " + node.getMoveString() + " " + (node.getMoveString() == null || node.getMoveString().equals("") ? "" : Arrays.toString(node.getCoords())));
        } while ((node = node.getNextNode()) != null);
        return moveCommands;
    }
}
