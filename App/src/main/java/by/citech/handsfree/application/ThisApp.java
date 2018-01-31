package by.citech.handsfree.application;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.res.Configuration;

import by.citech.handsfree.activity.fsm.ActivityFsm;
import by.citech.handsfree.bluetoothlegatt.ConnectAction;
import by.citech.handsfree.call.CallControl;
import by.citech.handsfree.bluetoothlegatt.ConnectorBluetooth;
import by.citech.handsfree.settings.PreferencesProcessor;
import by.citech.handsfree.threading.ThreadingManager;
import timber.log.Timber;

public class ThisApp
        extends Application
        implements ActivityFsm.IActivityFsmListenerRegister {

    private static BluetoothManager bluetoothManager;
    private static BluetoothAdapter bluetoothAdapter;
    private static ThreadingManager threadingManager;
    private static ConnectorBluetooth connectorBluetooth;
    private static ActivityFsm activityFsm;
    private static CallControl callControl;
    private static BluetoothDevice btConnectedDevice;
    private static String btConnectedAddr;
    private static Context appContext;
    private static BroadcastReceiverWrapper broadcastReceiverWrapper;

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) bluetoothAdapter = bluetoothManager.getAdapter();
        threadingManager = ThreadingManager.getInstance();
        threadingManager.activate();
        appContext = getApplicationContext();
        activityFsm = ActivityFsm.getInstance();
        connectorBluetooth = ConnectorBluetooth.getInstance();
        callControl = CallControl.getInstance();
        broadcastReceiverWrapper = new BroadcastReceiverWrapper();
        PreferencesProcessor.init(this);
        appContext = getApplicationContext();
        registerReceiver(broadcastReceiverWrapper.getGattUpdateReceiver(), BroadcastReceiverWrapper.makeGattUpdateIntentFilter());
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
    public static void setBtConnectedAddr(String btConnectedAddr) {ThisApp.btConnectedAddr = btConnectedAddr;}
    public static void registerBroadcastListener(ConnectAction connectAction) {broadcastReceiverWrapper.registerListener(connectAction);}

}
