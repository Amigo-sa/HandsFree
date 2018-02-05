package by.citech.handsfree.application;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Vibrator;

import by.citech.handsfree.activity.fsm.ActivityFsm;
import by.citech.handsfree.bluetoothlegatt.ConnectAction;
import by.citech.handsfree.bluetoothlegatt.ConnectorBluetooth;
import by.citech.handsfree.bluetoothlegatt.ui.BluetoothUi;
import by.citech.handsfree.call.CallControl;
import by.citech.handsfree.call.CallHandshake;
import by.citech.handsfree.network.ConnectorNet;
import by.citech.handsfree.proximity.ProximityLocker;
import by.citech.handsfree.proximity.ProximitySensorListener;
import by.citech.handsfree.settings.PreferencesProcessor;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.threading.ThreadingManager;
import by.citech.handsfree.vibration.VibrationSwitch;
import timber.log.Timber;

public class ThisApp
        extends Application
        implements ActivityFsm.IActivityFsmListenerRegister {

    private static ThisAppBuilder thisAppBuilder;
    private static ProximitySensorListener proximitySensorListener;
    private static ProximityLocker proximityLocker;
    private static VibrationSwitch vibrationSwitch;
//  private static BluetoothManager bluetoothManager;
//  private static BluetoothAdapter bluetoothAdapter;
    private static ThreadingManager threadingManager;
    private static ConnectorBluetooth connectorBluetooth;
    private static BluetoothUi bluetoothUi;
    private static ConnectorNet connectorNet;
    private static ActivityFsm activityFsm;
    private static CallControl callControl;
    private static CallHandshake callHandshake;
    private static Context appContext;
    private static BroadcastReceiverWrapper broadcastReceiverWrapper;
    private static Handler commonHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Settings.debug) Timber.plant(new Timber.DebugTree());
        appContext = getApplicationContext();
        commonHandler = new Handler();
        PreferencesProcessor.init(this);

        thisAppBuilder = new ThisAppBuilder(PreferencesProcessor.getOpModePref());

//      bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//      if (bluetoothManager != null) bluetoothAdapter = bluetoothManager.getAdapter();

        threadingManager = ThreadingManager.getInstance();
        threadingManager.activate();

        activityFsm = ActivityFsm.getInstance();
        connectorBluetooth = ConnectorBluetooth.getInstance();
        bluetoothUi = BluetoothUi.getInstance();
        connectorNet = ConnectorNet.getInstance();
        callControl = CallControl.getInstance();
        callHandshake = CallHandshake.getInstance();

        broadcastReceiverWrapper = new BroadcastReceiverWrapper();
        registerReceiver(
                broadcastReceiverWrapper.getGattUpdateReceiver(),
                BroadcastReceiverWrapper.makeGattUpdateIntentFilter());

        proximitySensorListener = new ProximitySensorListener();
        proximityLocker = new ProximityLocker();
        vibrationSwitch = new VibrationSwitch(commonHandler);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public static VibrationSwitch getVibrationSwitch() {return vibrationSwitch;}
    public static Handler getCommonHandler() {return commonHandler;}
    public static ProximityLocker getProximityLocker() {return proximityLocker;}
    public static ProximitySensorListener getProximitySensorListener() {return proximitySensorListener;}
    public static BluetoothUi getBluetoothUi() {return bluetoothUi;}
    public static BroadcastReceiverWrapper getBroadcastReceiverWrapper() {return broadcastReceiverWrapper;}
    public static ThisAppBuilder getThisAppBuilder() {return thisAppBuilder;}
    public static CallHandshake getCallHandshake() {return callHandshake;}
    public static ActivityFsm getActivityFsm() {return activityFsm;}
    public static CallControl getCallControl() {return callControl;}
    public static ThreadingManager getThreadingManager() {return threadingManager;}
    public static ConnectorNet getConnectorNet() {return connectorNet;}
    public static ConnectorBluetooth getConnectorBluetooth() {return connectorBluetooth;}
    public static Context getAppContext() {return appContext;}
//  public static BluetoothManager getBluetoothManager() {return bluetoothManager;}
//  public static BluetoothAdapter getBluetoothAdapter() {return bluetoothAdapter;}

}
