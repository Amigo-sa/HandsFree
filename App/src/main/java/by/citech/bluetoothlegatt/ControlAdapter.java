package by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.logic.IBluetoothListener;
import by.citech.param.Settings;

/**
 * Created by tretyak on 21.11.2017.
 */

public class ControlAdapter {

    private final static String TAG = "WSD_ControlAdapter";

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean Connected = false;
    private BluetoothDevice mBTDevice;
    private IBluetoothListener mIBluetoothListener;

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
    }

    public void initializeListBluetoothDevice() {
        if (Settings.debug) Log.i(TAG, "initializeListBluetoothDevice()");
        if (mLeDeviceListAdapter == null) {
            mLeDeviceListAdapter = mIBluetoothListener.addLeDeviceListAdapter();
            //if (Settings.debug) Log.i(TAG, "mLeDeviceListAdapter() = " + mLeDeviceListAdapter);
        } else
            clearAllDevicesFromList();
        if (Connected)
            addConnectDeviceToList();
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
}
