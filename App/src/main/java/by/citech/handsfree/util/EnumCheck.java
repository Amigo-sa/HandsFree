package by.citech.handsfree.util;

import android.util.Log;

import java.util.Locale;

import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.ISettingEnum;
import by.citech.handsfree.settings.Settings;

public class EnumCheck {

    private static final String TAG = Tags.ENUM_CHECK;
    private static final boolean debug = Settings.debug;

    public static <T extends Enum<T> & ISettingEnum<T>> boolean isEnumOk(T t) {
        boolean isEnumOk = true;
        if (debug) Log.w(TAG, "ENUM CHECK START");

        if (debug) Log.w(TAG, "isEnumOk getSettingName " + t.getSettingName());
        if (debug) Log.w(TAG, "isEnumOk name " + t.name());
        if (debug) Log.w(TAG, "isEnumOk toString " + t.toString());
        if (debug) Log.w(TAG, "isEnumOk ordinal " + t.ordinal());
        if (debug) Log.w(TAG, "isEnumOk getTypeName " + t.getTypeName());
        if (debug) Log.w(TAG, "isEnumOk getSettingNumber " + t.getSettingNumber());

        for (T value : t.getValues()) {
            boolean isEnumOkTmp;
            if (debug) Log.w(TAG, String.format(Locale.US, "opMode " +
                            "getSettingName %s, " +
                            "name %s, " +
                            "toString %s, " +
                            "ordinal %d, " +
                            "getTypeName %s, " +
                            "getSettingNumber %s" +
                            "\n",
                    value.getSettingName(),
                    value.name(),
                    value.toString(),
                    value.ordinal(),
                    value.getTypeName(),
                    value.getSettingNumber()));

            isEnumOkTmp = value.getSettingName().matches(value.name()) && value.getSettingName().matches(value.toString());
            if (!isEnumOkTmp) isEnumOk = false;
            if (debug) Log.w(TAG, "onCreate opMode's getSettingName matches name matches toString is: " + isEnumOkTmp);

            isEnumOkTmp = value.getSettingName().equals(value.name()) && value.getSettingName().equals(value.toString());
            if (!isEnumOkTmp) isEnumOk = false;
            if (debug) Log.w(TAG, "onCreate opMode's getSettingName equals name equals toString is: " + isEnumOkTmp);
        }

        if (debug) Log.w(TAG, "ENUM CHECK END");
        return isEnumOk;
    }

}
