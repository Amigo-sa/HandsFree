package by.citech.gui;

import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

import by.citech.param.Colors;
import by.citech.param.Settings;
import by.citech.param.Tags;
import by.citech.util.Pair;

public class ButtonHelper {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.BUTTON_HELPER;

    private static Map<String, Pair<Button[], boolean[]>> pairMap;

    //--------------------- singleton

    private static volatile ButtonHelper instance = null;

    private ButtonHelper() {
        pairMap = new HashMap<>();
    }

    public static ButtonHelper getInstance() {
        if (instance == null) {
            synchronized (ButtonHelper.class) {
                if (instance == null) {
                    instance = new ButtonHelper();
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

    public static void setColorLabel(Button button, String label, int color) {
        button.setText(label);
        button.setBackgroundColor(color);
    }

    public static void enable(Button button, String label, int color) {
        button.setEnabled(true);
        setColorLabel(button, label, color);
    }

    public static void enable(Button... buttons) {
        for (Button button : buttons) {
            button.setEnabled(true);
            button.setBackgroundColor(Colors.GREEN);
        }
    }

    public static void disable(Button button, String label) {
        button.setEnabled(false);
        ButtonHelper.setColorLabel(button, label, Colors.GRAY);
    }

    public static void disable(Button... buttons) {
        for (Button button : buttons) {
            button.setEnabled(false);
            button.setBackgroundColor(Colors.GRAY);
        }
    }

}

