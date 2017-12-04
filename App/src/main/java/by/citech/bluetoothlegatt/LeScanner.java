package by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;

import by.citech.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.bluetoothlegatt.adapters.LeDeviceListAdapter;
import by.citech.logic.IBluetoothListener;
import by.citech.param.Settings;

/**
 * Created by tretyak on 21.11.2017.
 */

public class LeScanner {

    private final static String TAG = "WSD_LeScanner";
    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private boolean mScanning;
    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private BluetoothAdapter mBluetoothAdapter;
    private IBluetoothListener mIBluetoothListener;
    private ControlAdapter controlAdapter;

    public LeScanner(Handler mHandler,
                     BluetoothAdapter mBluetoothAdapter,
                     IBluetoothListener mIBluetoothListener,
                     ControlAdapter controlAdapter) {
        this.mHandler = mHandler;
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.mIBluetoothListener = mIBluetoothListener;
        this.controlAdapter = controlAdapter;
    }

    public boolean isScanning() {
        return mScanning;
    }

    public void startScanBluetoothDevice(){
        scanLeDevice(true);
    }

    public void stopScanBluetoothDevice(){
        scanLeDevice(false);
    }

    // процедура сканирования устройства
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if (Settings.debug) Log.i(TAG, "start scanLeDevice()");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mIBluetoothListener.changeOptionMenu();

                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            if (Settings.debug) Log.i(TAG, "stop scanLeDevice()");
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        mIBluetoothListener.changeOptionMenu();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    if (Settings.debug) Log.i(TAG, "onLeScan()");
                    final LeDeviceListAdapter leDeviceListAdapter = controlAdapter.getLeDeviceListAdapter();
                    if (leDeviceListAdapter != null && mIBluetoothListener != null)
                        mIBluetoothListener.addDeviceToList(leDeviceListAdapter, device, rssi);

                }
            };
}
