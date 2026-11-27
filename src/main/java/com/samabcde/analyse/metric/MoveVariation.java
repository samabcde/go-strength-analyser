package com.samabcde.analyse.metric;

import java.util.List;

public record MoveVariation(int order, List<String> moves) {
    public MoveVariation {
        moves = List.copyOf(moves);
    }

    public String firstMove() {
        return moves.isEmpty() ? null : moves.getFirst();
    }
}
