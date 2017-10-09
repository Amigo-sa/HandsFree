package by.citech.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;


import static android.content.Context.MODE_PRIVATE;

public class AppData {
    private static String bluetoothDeviceId;
    private static String remoteIP;
    private static int remotePort;


    private static SharedPreferences preferences;

    public static void init(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        preferences = context.getSharedPreferences(context.getString(stringId), MODE_PRIVATE);
        restore();
    }

    public static void save() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("bluetoothDeviceId", bluetoothDeviceId).
                putString("remoteIP", remoteIP).
                putInt("remotePort", remotePort);
        editor.apply();
    }

    private static void restore() {
        bluetoothDeviceId = preferences.getString("bluetoothDeviceId", "");
        remoteIP = preferences.getString("remoteIP", "");
        remotePort = preferences.getInt("remotePort", 80);
    }

    public static String getBluetoothDeviceId() {
        return bluetoothDeviceId;
    }

    public static void setBluetoothDeviceId(String bluetoothDeviceId) {
        AppData.bluetoothDeviceId = bluetoothDeviceId;
        save();
    }

    public static String getRemoteIP() {
        return remoteIP;
    }

    public static void setRemoteIP(String remoteIP) {
        AppData.remoteIP = remoteIP;
        save();
    }

    public static int getRemotePort() {
        return remotePort;
    }

    public static void setRemotePort(int remotePort) {
        AppData.remotePort = remotePort;
        save();
    }

    public static void reset() {
        bluetoothDeviceId = "";
        remoteIP = "";
        remotePort = 0;
        save();
    }
}
