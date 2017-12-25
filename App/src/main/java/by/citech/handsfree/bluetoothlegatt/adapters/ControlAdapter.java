package by.citech.handsfree.bluetoothlegatt.adapters;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.IBtList;
import by.citech.handsfree.bluetoothlegatt.IScannListener;
import by.citech.handsfree.logic.ConnectorBluetooth;
import by.citech.handsfree.settings.Settings;

/**
 * Created by tretyak on 21.11.2017.
 */
// implements IElement
public class ControlAdapter implements IScannListener {

//    private final static String TAG = "WSD_ControlAdapter";
    private final String TAG;

    private boolean Connected;
    private ConnectorBluetooth connectorBluetooth;
    private IBtList iBtList;

//    private  IElement iElement;

    //--------------------- TEST

    private static int objCount;
    private final int objNumber;

    static {
        objCount = 0;
    }

    {
        objCount++;
        objNumber = objCount;
        TAG = "WSD_ControlAdapter " + objNumber;
    }

    //--------------------- TEST
    public ControlAdapter(ConnectorBluetooth connectorBluetooth) {
        this.connectorBluetooth = connectorBluetooth;
    }

    public void setConnected(boolean connected) {
        if (Settings.debug) Log.i(TAG, "setConnected = " + connected);
        this.Connected = connected;
    }

    public void initializeListBluetoothDevice(BluetoothDevice device) {
        if (Settings.debug) Log.i(TAG, "initializeListBluetoothDevice()");
        iBtList = connectorBluetooth.getiBtList();
        clearAllDevicesFromList();
        if (Connected) {
            addConnectDeviceToList(device);
        }
    }

    public void addConnectDeviceToList(BluetoothDevice device){
            if (Settings.debug) Log.i(TAG, "ADD DEVICE TO LIST " + device + "\n");
            if ((device != null) && iBtList != null)
                iBtList.addDevice(device, 200);
    }

    public void clearAllDevicesFromList(){
        if (Settings.debug) Log.e(TAG, "clearAllDevicesFromList()");
        if (iBtList != null){
            iBtList.clear();
        }
    }

    @Override
    public void scanCallback(BluetoothDevice device, int rssi) {
        if (iBtList != null)
            iBtList.addDevice(device, rssi);
    }
}
