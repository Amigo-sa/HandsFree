package by.citech.handsfree.connection;

import by.citech.vincentwillemvangogh.messaging.AutBMsgSendData;
import by.citech.vincentwillemvangogh.settings.Settings;
import timber.log.Timber;

import static by.citech.vincentwillemvangogh.connection.LightConfig.EArea;
import static by.citech.vincentwillemvangogh.connection.LightConfig.ELight;
import static by.citech.vincentwillemvangogh.connection.LightConfig.ELight.getValue;
import static by.citech.vincentwillemvangogh.connection.LightConfig.ELight.off;
import static by.citech.vincentwillemvangogh.connection.LightConfig.ELight.on;
import static by.citech.vincentwillemvangogh.messaging.AutBMsgAdaptor.getMsgSendData;

class LightHelper {

    private static final boolean debug = Settings.debug;

    //--------------------- conversion

    static void convert(EConvertion toWhat, byte[] data, LightConfig cfg) {
        LightConfig.EArea[] areas = LightConfig.EArea.values();
        LightConfig.ELight[] lights = LightConfig.ELight.values();
        LightConfig.EArea area;
        LightConfig.ELight light;
        int bytePosition, areaPosition, lightPosition;
        for (areaPosition = 0; areaPosition < areas.length; areaPosition++) {
            area = areas[areaPosition];
            for (lightPosition = 0; lightPosition < lights.length; lightPosition++) {
                light = lights[lightPosition];
                bytePosition = area.getDataPosition(light);
                if (bytePosition != -1) {
                    switch (toWhat) {
                        case ToData:     toData(cfg, data, areaPosition, lightPosition, bytePosition); break;
                        case ToConfig: toConfig(cfg, data, areaPosition, lightPosition, bytePosition); break;
                        default: break;
                    }
                }
            }
        }
    }

    private static void toData(LightConfig cfg, byte[] data, int area, int light, int bytePosition) {
        if (cfg.getCfg()[area][light]) data[bytePosition] = on;
        else                           data[bytePosition] = off;
    }

    private static void toConfig(LightConfig cfg, byte[] data, int area, int light, int bytePosition) {
        byte value = data[bytePosition];
        switch (value) {
            case on:  cfg.getCfg()[area][light] =  true; break;
            case off: cfg.getCfg()[area][light] = false; break;
            default:  cfg.getCfg()[area][light] = false; if(debug) Timber.e("bad value on light data: %s", value); break;
        }
    }

    //--------------------- updating

    static AutBMsgSendData update(AutBMsgSendData msg, boolean isOn, EArea area, ELight light) {
        return getMsgSendData(update(msg.getDataField(), isOn, area, light));
    }

    static byte[] update(byte[] toUpdate, boolean isOn, EArea area, ELight light) {
        toUpdate[area.getDataPosition(light)] = getValue(isOn);
        return toUpdate;
    }

    static LightConfig update(LightConfig toUpdate, boolean isOn, EArea area, ELight light) {
        toUpdate.getCfg()[area.ordinal()][light.ordinal()] = isOn;
        return toUpdate;
    }

    static LightConfig getInitConfig() {
        return new LightConfig();
    }

    static byte[] getInitData() {
        return new byte[AutBMsgSendData.EField.Data.getLength()];
    }

    //--------------------- additional

    enum EConvertion {
        ToConfig,
        ToData
    }

}
