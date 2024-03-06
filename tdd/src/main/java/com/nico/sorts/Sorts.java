package com.nico.sorts;

import java.util.ArrayList;
import java.util.List;

public class Sorts {

    private Sorts() {
        // Does nothing
    }

    public static List<Integer> sort(List<Integer> list) {
        if(list.size() <= 1) {
            return List.copyOf(list);
        }
        List<Integer> middles = list.stream()
                .filter(value -> value.equals(list.getFirst()))
                .toList();
        List<Integer> lessers = list.stream()
                .filter(value -> value.compareTo(list.getFirst()) < 0)
                .toList();
        List<Integer> greaters = list.stream()
                .filter(value -> value.compareTo(list.getFirst()) > 0)
                .toList();

        List<Integer> result = new ArrayList<>(sort(lessers));
        result.addAll(middles);
        result.addAll(sort(greaters));
        return List.copyOf(result);
    }
}
