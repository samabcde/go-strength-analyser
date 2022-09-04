package com.samabcde.analyse.sgf;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.samabcde.analyse.sgf.Rank.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RankTest {

    @ParameterizedTest
    @MethodSource("rankToExpectedCode")
    public void code(Rank rank, String expectedCode) {
        assertEquals(expectedCode, rank.code());
    }

    public static Stream<Arguments> rankToExpectedCode() {
        return Stream.of(
                Arguments.of(_35K_, "35K"),
                Arguments.of(_34K_, "34K"),
                Arguments.of(_33K_, "33K"),
                Arguments.of(_32K_, "32K"),
                Arguments.of(_31K_, "31K"),
                Arguments.of(_30K_, "30K"),
                Arguments.of(_29K_, "29K"),
                Arguments.of(_28K_, "28K"),
                Arguments.of(_27K_, "27K"),
                Arguments.of(_26K_, "26K"),
                Arguments.of(_25K_, "25K"),
                Arguments.of(_24K_, "24K"),
                Arguments.of(_23K_, "23K"),
                Arguments.of(_22K_, "22K"),
                Arguments.of(_21K_, "21K"),
                Arguments.of(_20K_, "20K"),
                Arguments.of(_19K_, "19K"),
                Arguments.of(_18K_, "18K"),
                Arguments.of(_17K_, "17K"),
                Arguments.of(_16K_, "16K"),
                Arguments.of(_15K_, "15K"),
                Arguments.of(_14K_, "14K"),
                Arguments.of(_13K_, "13K"),
                Arguments.of(_12K_, "12K"),
                Arguments.of(_11K_, "11K"),
                Arguments.of(_10K_, "10K"),
                Arguments.of(_9K_, "9K"),
                Arguments.of(_8K_, "8K"),
                Arguments.of(_7K_, "7K"),
                Arguments.of(_6K_, "6K"),
                Arguments.of(_5K_, "5K"),
                Arguments.of(_4K_, "4K"),
                Arguments.of(_3K_, "3K"),
                Arguments.of(_2K_, "2K"),
                Arguments.of(_1K_, "1K"),
                Arguments.of(_1D_, "1D"),
                Arguments.of(_2D_, "2D"),
                Arguments.of(_3D_, "3D"),
                Arguments.of(_4D_, "4D"),
                Arguments.of(_5D_, "5D"),
                Arguments.of(_6D_, "6D"),
                Arguments.of(_7D_, "7D"),
                Arguments.of(_8D_, "8D"),
                Arguments.of(_9D_, "9D"),
                Arguments.of(_1P_, "1P"),
                Arguments.of(_2P_, "2P"),
                Arguments.of(_3P_, "3P"),
                Arguments.of(_4P_, "4P"),
                Arguments.of(_5P_, "5P"),
                Arguments.of(_6P_, "6P"),
                Arguments.of(_7P_, "7P"),
                Arguments.of(_8P_, "8P"),
                Arguments.of(_9P_, "9P"),
                Arguments.of(_NR_, "NR")
        );
    }
}