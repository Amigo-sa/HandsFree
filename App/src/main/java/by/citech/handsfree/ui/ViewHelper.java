package by.citech.handsfree.ui;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import by.citech.handsfree.parameters.Colors;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class ViewHelper {

    private static final boolean debug = Settings.debug;

    //--------------------- animation

    public static void startAnimation(View view, Animation animation) {
        if (view == null || animation == null) {
            if (debug) Timber.e("startAnimation%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        view.startAnimation(animation);
    }

    public static void clearAnimation(View view) {
        if (view == null) {
            if (debug) Timber.e("clearAnimation%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        view.clearAnimation();
    }

    //--------------------- get visibility

    public static boolean getVisibility(View view) {
        if (view == null) {
            if (debug) Timber.e("getVisibility%s", StatusMessages.ERR_PARAMETERS);
            return false;
        }
        return (view.getVisibility() != View.VISIBLE);
    }

    //--------------------- get text

    public static String getText(EditText editText) {
        if (editText == null) {
            if (debug) Timber.e("getText%s", StatusMessages.ERR_PARAMETERS);
            return "";
        }
        Editable editable = editText.getText();
        if (editable != null) {
            return editable.toString();
        } else {
            if (debug) Timber.e("getText editable is null");
            return "";
        }
    }

    //--------------------- color/label

    public static void setColor(View view, int color) {
        if (view == null) {
            if (debug) Timber.e("setColor%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        view.setBackgroundColor(color);
    }

    public static void setText(TextView textView, String text) {
        if (textView == null) {
            if (debug) Timber.e("startAnimation%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setText(text);
    }

    public static void setColorAndText(TextView textView, int stringResId, int color) {
        if (textView == null) {
            if (debug) Timber.e("setColorAndText%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setText(stringResId);
        textView.setBackgroundColor(color);
    }

    public static void setColorAndText(TextView textView, String label, int color) {
        if (textView == null) {
            if (debug) Timber.e("setColorAndText%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setText(label);
        textView.setBackgroundColor(color);
    }

    //--------------------- visibility

    public static void setVisibility(View view, int visibility) {
        if (view == null) {
            if (debug) Timber.e("setVisibility%s", StatusMessages.ERR_PARAMETERS);
        } else {
            view.setVisibility(visibility);
        }
    }

    //--------------------- enableGreen

    public static void enable(TextView textView, int color) {
        if (textView == null) {
            if (debug) Timber.e("enable%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setEnabled(true);
        setColor(textView, color);
    }

    public static void enable(TextView TextView, int color, String label) {
        if (TextView == null) {
            if (debug) Timber.e("enable%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        TextView.setEnabled(true);
        setColorAndText(TextView, label, color);
    }

    public static void enableGreen(TextView... textViews) {
        if (textViews == null) {
            if (debug) Timber.e("enableGreen%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        for (TextView textView : textViews) {
            if (textView == null) {
                if (debug) Timber.e("enableGreen button%s", StatusMessages.ERR_PARAMETERS);
                continue;
            }
            textView.setEnabled(true);
            textView.setBackgroundColor(Colors.GREEN);
        }
    }

    //--------------------- disableGray

    public static void disableGray(TextView textView, String label) {
        if (textView == null) {
            if (debug) Timber.e("disableGray%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setEnabled(false);
        setColorAndText(textView, label, Colors.GRAY);
    }

    public static void disableGray(TextView... textViews) {
        if (textViews == null) {
            if (debug) Timber.e("disableGray textViews%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        for (TextView textView : textViews) {
            if (textView == null) {
                if (debug) Timber.e("disableGray textView%s", StatusMessages.ERR_PARAMETERS);
                continue;
            }
            textView.setEnabled(false);
            textView.setBackgroundColor(Colors.GRAY);
        }
    }

}

