package by.citech.handsfree.ui.helpers;

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

public class ViewHelper {

    private static final boolean debug = Settings.debug;
    private static final String STAG = Tags.ViewHelper + " ST";

    //--------------------- animation

    public static void startAnimation(View view, Animation animation) {
        if (view == null || animation == null) {
            Log.e(STAG, "startAnimation" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        view.startAnimation(animation);
    }

    public static void clearAnimation(View view) {
        if (view == null) {
            Log.e(STAG, "clearAnimation" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        view.clearAnimation();
    }

    //--------------------- get visibility

    public static boolean getVisibility(View view) {
        if (view == null) {
            Log.e(STAG, "getVisibility" + StatusMessages.ERR_PARAMETERS);
            return false;
        }
        return (view.getVisibility() != View.VISIBLE);
    }

    //--------------------- get text

    public static String getText(EditText editText) {
        if (editText == null) {
            Log.e(STAG, "getText" + StatusMessages.ERR_PARAMETERS);
            return "";
        }
        Editable editable = editText.getText();
        if (editable != null) {
            return editable.toString();
        } else {
            Log.e(STAG, "getText editable is null");
            return "";
        }
    }

    //--------------------- color/label

    public static void setColor(View view, int color) {
        if (view == null) {
            Log.e(STAG, "setColor" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        view.setBackgroundColor(color);
    }

    public static void setText(TextView textView, String text) {
        if (textView == null) {
            Log.e(STAG, "setText" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setText(text);
    }

    public static void setColorAndText(TextView textView, int stringResId, int color) {
        if (textView == null) {
            Log.e(STAG, "setColorAndText" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setText(stringResId);
        textView.setBackgroundColor(color);
    }

    public static void setColorAndText(TextView textView, String label, int color) {
        if (textView == null) {
            Log.e(STAG, "setColorAndText" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setText(label);
        textView.setBackgroundColor(color);
    }

    //--------------------- visibility

    public static void setVisibility(View view, int visibility) {
        if (view == null) {
            Log.e(STAG, "setVisibility" + StatusMessages.ERR_PARAMETERS);
        } else {
            view.setVisibility(visibility);
        }
    }

    //--------------------- enableGreen

    public static void enable(TextView textView, int color) {
        if (textView == null) {
            Log.e(STAG, "enable" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setEnabled(true);
        setColor(textView, color);
    }

    public static void enable(TextView TextView, int color, String label) {
        if (TextView == null) {
            Log.e(STAG, "enable" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        TextView.setEnabled(true);
        setColorAndText(TextView, label, color);
    }

    public static void enableGreen(TextView... textViews) {
        if (textViews == null) {
            Log.e(STAG, "enableGreen" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        for (TextView textView : textViews) {
            if (textView == null) {
                Log.e(STAG, "enableGreen button" + StatusMessages.ERR_PARAMETERS);
                continue;
            }
            textView.setEnabled(true);
            textView.setBackgroundColor(Colors.GREEN);
        }
    }

    //--------------------- disableGray

    public static void disableGray(TextView textView, String label) {
        if (textView == null) {
            Log.e(STAG, "disableGray" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        textView.setEnabled(false);
        setColorAndText(textView, label, Colors.GRAY);
    }

    public static void disableGray(TextView... textViews) {
        if (textViews == null) {
            Log.e(STAG, "disableGray textViews" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        for (TextView textView : textViews) {
            if (textView == null) {
                Log.e(STAG, "disableGray textView" + StatusMessages.ERR_PARAMETERS);
                continue;
            }
            textView.setEnabled(false);
            textView.setBackgroundColor(Colors.GRAY);
        }
    }

}

