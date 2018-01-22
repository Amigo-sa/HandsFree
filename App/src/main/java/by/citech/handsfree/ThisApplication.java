package by.citech.handsfree;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.res.Configuration;


import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.PreferencesProcessor;


public class ThisApplication
        extends Application {

    private static BluetoothManager bluetoothManager;
    private static BluetoothAdapter bluetoothAdapter;

    private static String btConnectedAddr;
    private static Context appContext;

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();

//        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        if (bluetoothManager != null) bluetoothAdapter = bluetoothManager.getAdapter();
        appContext = getApplicationContext();
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public static Context getAppContext() {return appContext;}
    public static BluetoothManager getBluetoothManager() {return bluetoothManager;}
    public static BluetoothAdapter getBluetoothAdapter() {return bluetoothAdapter;}
    public static String getBtConnectedAddr() {return btConnectedAddr;}
    public static void setBtConnectedAddr(String btConnectedAddr) {ThisApplication.btConnectedAddr = btConnectedAddr;}

}
