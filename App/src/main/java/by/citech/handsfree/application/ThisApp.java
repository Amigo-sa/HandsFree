package by.citech.handsfree.application;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.res.Configuration;

import by.citech.handsfree.activity.fsm.ActivityFsm;
import by.citech.handsfree.bluetoothlegatt.ConnectAction;
import by.citech.handsfree.connection.ChosenDeviceControl;
import by.citech.handsfree.connection.ConnectionControl;
import by.citech.handsfree.connection.fsm.ConnectionFsm;
import by.citech.handsfree.connection.fsm.IConnectionFsmListenerRegister;
import by.citech.handsfree.bluetoothlegatt.ConnectorBluetooth;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.PreferencesProcessor;
import by.citech.handsfree.threading.ThreadingManager;
import timber.log.Timber;

public class ThisApp
        extends Application implements IConnectionFsmListenerRegister, ActivityFsm.IActivityFsmListenerRegister {

    private static BluetoothManager bluetoothManager;
    private static BluetoothAdapter bluetoothAdapter;
    private static ThreadingManager threadingManager;
    private static ConnectorBluetooth connectorBluetooth;
    private static ActivityFsm activityFsm;
    private static ConnectionFsm connectionFsm;
    private static ConnectionControl connectionControl;
    private static ChosenDeviceControl chosenDeviceControl;
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
        connectionFsm = ConnectionFsm.getInstance();
        connectorBluetooth = ConnectorBluetooth.getInstance();
        connectionControl = ConnectionControl.getInstance();
        chosenDeviceControl = ChosenDeviceControl.getInstance();
        broadcastReceiverWrapper = new BroadcastReceiverWrapper();
        registerActivityFsmListener(chosenDeviceControl, Tags.ChosenDeviceControl);
        registerActivityFsmListener(connectionControl, Tags.ConnectionControl);
        registerConnectionFsmListener(connectorBluetooth, Tags.ConnectorBluetooth);
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
    public static void setBtConnectedAddr(String btConnectedAddr) {
        ThisApp.btConnectedAddr = btConnectedAddr;}
    public static void registerBroadcastListener(ConnectAction connectAction) {broadcastReceiverWrapper.registerListener(connectAction);}

}
