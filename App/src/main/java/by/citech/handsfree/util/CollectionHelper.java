package by.citech.handsfree.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CollectionHelper {

    public static <T> List<T> getArrayListInitiatedWithNulls(int from, int amount) {
        List<T> list = new ArrayList<>();
        int to = from + amount;
        for (int i = 0; i < to ; i++) {
            list.add(i, null);
        }
        return list;
    }

    @SafeVarargs
    public static <T> HashSet<T> s(T... states) {
        if (states == null || states.length == 0) {
            return new HashSet<>(Collections.emptyList());
        } else if (states.length == 1) {
            return new HashSet<>(Collections.singletonList(states[0]));
        } else {
            return new HashSet<>(Arrays.asList(states));
        }
    }

}
