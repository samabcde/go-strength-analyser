package com.samabcde.analyse.util;

public final class TestUtils {

    private static final String TEXT_BLOCK_LINE_SEPARATOR = "\n";

    private TestUtils() {
    }

    /**
     * For testing using text block, so
     * we can compare with multiline string with system line separator
     *
     * @see <a href="https://docs.oracle.com/en/java/javase/15/text-blocks/index.html#normalization-of-line-terminators">
     * Normalization Of Line Terminators</a>
     */
    public static String normalizeTextBlockLineSeparator(String textBlock) {
        return textBlock.replace(TEXT_BLOCK_LINE_SEPARATOR, System.lineSeparator());
    }

}
