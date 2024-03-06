package com.nico.wraps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Wrapper {

    private Wrapper() {
        // Does nothing
    }

    public static List<String> wrap(String line, int columnWidth) {
        if(columnWidth <= 0 || line.isEmpty()) {
            return List.of();
        }
        int maxWordLength = getMaxWordLength(line);
        if(columnWidth < maxWordLength) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        List<String> words = getWords(line);
        StringBuilder lineOK = new StringBuilder();
        for (int wordNumber = 0 ; wordNumber < words.size() ; wordNumber++) {
            boolean isLastWord = (wordNumber == (words.size() - 1));
            String word = words.get(wordNumber);

            lineOK.append(word);
            insertWordSeparatorIfPossible(columnWidth, isLastWord, lineOK);
            if(isLastWord || cannotInsertMore(columnWidth, lineOK)) {
                result.add(lineOK.toString());
                lineOK = new StringBuilder();
            }
        }

        return result;
    }

    private static void insertWordSeparatorIfPossible(int columnWidth, boolean isLastWord, StringBuilder lineOK) {
        if(!isLastWord &&
                canInsertWordSeparator(columnWidth, lineOK)) {
            lineOK.append(" ");
        }
    }

    private static boolean cannotInsertMore(int columnWidth, StringBuilder line) {
        return line.length() >= columnWidth;
    }

    private static boolean canInsertWordSeparator(int columnWidth, StringBuilder line) {
        return line.length() + 1 <= columnWidth;
    }

    private static int getMaxWordLength(String line) {
        return getWords(line).stream()
                .mapToInt(String::length)
                .max()
                .orElse(line.length());
    }

    private static List<String> getWords(String line) {
        return Arrays.stream(line.split("\\s")).toList();
    }
}
