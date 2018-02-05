package by.citech.handsfree.util;

import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.ISettingEnum;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public class EnumCheck {

    public static <T extends Enum<T> & ISettingEnum<T>> boolean isEnumOk(T t) {
        boolean isEnumOk = true;
        Timber.w("ENUM CHECK START");

        Timber.w("isEnumOk getSettingName %s", t.getSettingName());
        Timber.w("isEnumOk name %s", t.name());
        Timber.w("isEnumOk toString %s", t.toString());
        Timber.w("isEnumOk ordinal %s", t.ordinal());
        Timber.w("isEnumOk getTypeName %s", t.getTypeName());
        Timber.w("isEnumOk getSettingNumber %s", t.getSettingNumber());

        for (T value : t.getValues()) {
            boolean isEnumOkTmp;
            Timber.w("opMode " +
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
                    value.getSettingNumber());

            isEnumOkTmp = value.getSettingName().matches(value.name()) && value.getSettingName().matches(value.toString());
            if (!isEnumOkTmp) isEnumOk = false;
            Timber.w("onCreate opMode's getSettingName matches name matches toString is: %s", isEnumOkTmp);

            isEnumOkTmp = value.getSettingName().equals(value.name()) && value.getSettingName().equals(value.toString());
            if (!isEnumOkTmp) isEnumOk = false;
            Timber.w("onCreate opMode's getSettingName equals name equals toString is: %s", isEnumOkTmp);
        }

        Timber.w("ENUM CHECK END");
        return isEnumOk;
    }

}
