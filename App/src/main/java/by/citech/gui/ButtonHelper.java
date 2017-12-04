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
        disableGray(buttonArr);
    }

    public void releaseState(String key) {
        Pair<Button[], boolean[]> pair = pairMap.get(key);
        for (int i = 0; i < pair.getX().length; i++) {
            if (pair.getY()[i]) {
                enableGreen(pair.getX()[i]);
            } else {
                disableGray(pair.getX()[i]);
            }
        }
        pairMap.remove(key);
    }

    //--------------------- set

    public static void setColor(Button button, int color) {
        button.setBackgroundColor(color);
    }

    public static void setColorLabel(Button button, String label, int color) {
        button.setText(label);
        button.setBackgroundColor(color);
    }

    //--------------------- enableGreen

    public static void enable(Button button, int color) {
        button.setEnabled(true);
        setColor(button, color);
    }

    public static void enable(Button button, int color, String label) {
        button.setEnabled(true);
        setColorLabel(button, label, color);
    }

    public static void enableGreen(Button... buttons) {
        for (Button button : buttons) {
            button.setEnabled(true);
            button.setBackgroundColor(Colors.GREEN);
        }
    }

    //--------------------- disableGray

    public static void disableGray(Button button, String label) {
        button.setEnabled(false);
        ButtonHelper.setColorLabel(button, label, Colors.GRAY);
    }

    public static void disableGray(Button... buttons) {
        for (Button button : buttons) {
            button.setEnabled(false);
            button.setBackgroundColor(Colors.GRAY);
        }
    }

}

