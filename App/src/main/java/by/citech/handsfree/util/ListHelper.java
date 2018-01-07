package by.citech.handsfree.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ListHelper {

    public static <T> List<T> getListInitiatedWithNulls(int from, int amount) {
        List<T> list = new ArrayList<>();
        int to = from + amount;
        for (int i = 0; i < to ; i++) {
            list.add(i, null);
        }
        return list;
    }

}
