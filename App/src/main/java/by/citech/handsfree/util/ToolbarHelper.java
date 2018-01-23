package by.citech.handsfree.util;

import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

import java.util.Locale;

import by.citech.handsfree.parameters.Colors;
import by.citech.handsfree.settings.Settings;

public class ToolbarHelper {

    public static void updateToolbar(String prefix, String placeholder, String message, int color, ActionBar toolbar) {
        if (toolbar == null) return;
        if (placeholder == null) placeholder = "";
        if (prefix == null) prefix = "";
        int toolbarMessageOffset = prefix.length() + placeholder.length();
        String title = String.format(Locale.US, "%s%s%s", prefix, placeholder, message);
        int titleLength = title.length();
        SpannableString s = new SpannableString(title);
        s.setSpan(new AbsoluteSizeSpan(Settings.toolbarBaseSize), 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Colors.WHITE), 0, toolbarMessageOffset, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(color), toolbarMessageOffset, titleLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new RelativeSizeSpan(Settings.toolbarMessageToPrefix), toolbarMessageOffset, titleLength, 0);
        toolbar.setTitle(s);
    }

}
