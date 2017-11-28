package by.citech.util;

import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

import by.citech.param.Colors;

public class Buttons {

    private static final boolean debug = true;
    private static final String TAG = "WSD_Buttons";

    private static Map<String, Pair<Button[], boolean[]>> pairMap;

    //--------------------- singleton

    private static volatile Buttons instance = null;

    private Buttons() {
        pairMap = new HashMap<>();
    }

    public static Buttons getInstance() {
        if (instance == null) {
            synchronized (Buttons.class) {
                if (instance == null) {
                    instance = new Buttons();
                }
            }
        }
        return instance;
    }

    public void freezeState(String key, Button... buttonArr) {
        if (pairMap.containsKey(key)) {
            pairMap.remove(key);
        }
        boolean[] isEnabledArr = new boolean[buttonArr.length];
        for (int i = 0; i < buttonArr.length; i++) {
            isEnabledArr[i] = buttonArr[i].isEnabled();
        }
        pairMap.put(key, new Pair<>(buttonArr, isEnabledArr));
        disable(buttonArr);
    }

    public void releaseState(String key) {
        Pair<Button[], boolean[]> pair = pairMap.get(key);
        for (int i = 0; i < pair.getX().length; i++) {
            if (pair.getY()[i]) {
                enable(pair.getX()[i]);
            } else {
                disable(pair.getX()[i]);
            }
        }
        pairMap.remove(key);
    }

    public static void disable(Button... buttons) {
        for (Button button : buttons) {
            button.setEnabled(false);
            button.setBackgroundColor(Colors.GRAY);
        }
    }

    public static void enable(Button... buttons) {
        for (Button button : buttons) {
            button.setEnabled(true);
            button.setBackgroundColor(Colors.GREEN);
        }
    }

}

