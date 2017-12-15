package by.citech.handsfree.bluetoothlegatt.adapters;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.IScannListener;
import by.citech.handsfree.logic.IBluetoothListener;
import by.citech.handsfree.settings.Settings;

/**
 * Created by tretyak on 21.11.2017.
 */

public class ControlAdapter implements IScannListener {

//    private final static String TAG = "WSD_ControlAdapter";
    private final String TAG;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean Connected;
    private volatile BluetoothDevice mBTDevice;
    private IBluetoothListener mIBluetoothListener;

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

    public ControlAdapter(IBluetoothListener mIBluetoothListener) {
        this.mIBluetoothListener = mIBluetoothListener;
    }

    public LeDeviceListAdapter getLeDeviceListAdapter() {
        return mLeDeviceListAdapter;
    }

    public void setConnected(boolean connected) {
        Connected = connected;
    }

    public void setBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
        if (Settings.debug) Log.w(TAG, "set mBTDevice 1 to " + this.mBTDevice );
        if (mBTDevice == null) {
            if (Settings.debug) Log.w(TAG, "set mBTDevice 2 to " + mBTDevice );
            this.mBTDevice = null;
            if (this.mBTDevice != null) {
                this.mBTDevice = null;
                if (Settings.debug) Log.w(TAG, "set mBTDevice 3 is " + this.mBTDevice );
            }
        }
    }

    public void initializeListBluetoothDevice() {
        if (Settings.debug) Log.i(TAG, "initializeListBluetoothDevice()");
        if (mLeDeviceListAdapter == null) {
            mLeDeviceListAdapter = mIBluetoothListener.addLeDeviceListAdapter();
            //if (Settings.debug) Log.i(TAG, "mLeDeviceListAdapter() = " + mLeDeviceListAdapter);
        } else {
            Log.e(TAG, "initializeListBluetoothDevice adapter is null");
            clearAllDevicesFromList();
        }
        if (Connected) {
            addConnectDeviceToList();
        }
    }

    public void addConnectDeviceToList(){
        if (Connected) {
            if (Settings.debug) Log.i(TAG, "ADD DEVICE TO LIST " + mBTDevice + "\n");
            if (Settings.debug) Log.i(TAG, "mLeDeviceListAdapter = " + mLeDeviceListAdapter + "\n");
            if ((mBTDevice != null) && mLeDeviceListAdapter != null) {
                mLeDeviceListAdapter.addDevice(mBTDevice, 200);
                // чтобы не сыпался в исклюение adapter
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void clearAllDevicesFromList(){
        if (Settings.debug) Log.e(TAG, "clearAllDevicesFromList()");
        if (mLeDeviceListAdapter != null){
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void scanCallback(BluetoothDevice device, int rssi) {
        if (mLeDeviceListAdapter != null)
            mLeDeviceListAdapter.addDevice(device, rssi);
    }
}
