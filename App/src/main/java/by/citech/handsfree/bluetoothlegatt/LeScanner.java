package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;

import by.citech.handsfree.gui.IbtToUiListener;
import by.citech.handsfree.logic.IBluetoothListener;
import by.citech.handsfree.param.Settings;

/**
 * Created by tretyak on 21.11.2017.
 */

public class LeScanner implements IbtToUiListener {

    private final static String TAG = "WSD_LeScanner";
    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private boolean mScanning;
    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private IBluetoothListener mIBluetoothListener;
    private IScannListener iScannListener;
    private BluetoothAdapter bluetoothAdapter;

    public LeScanner(Handler mHandler,
                     IBluetoothListener mIBluetoothListener,
                     IScannListener iScannListener) {
        this.mHandler = mHandler;
        this.mIBluetoothListener = mIBluetoothListener;
        this.iScannListener = iScannListener;
    }

    //--------------------- getters and setters

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            if (Settings.debug) Log.w(TAG, "getBluetoothAdapter bluetoothAdapter is null, get");
            bluetoothAdapter = mIBluetoothListener.getBluetoothManager().getAdapter();
        }
        return bluetoothAdapter;
    }

    public boolean isScanning() {
        return mScanning;
    }

    public void startScanBluetoothDevice() {
        scanLeDevice(true);
    }

    public void stopScanBluetoothDevice() {
        scanLeDevice(false);
    }

    // процедура сканирования устройства
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if (Settings.debug) Log.i(TAG, "start scanLeDevice()");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                if (Settings.debug) Log.i(TAG, "stop scanLeDevice()");
                mScanning = false;
                getBluetoothAdapter().stopLeScan(mLeScanCallback);
                mIBluetoothListener.changeOptionMenu();

            }, SCAN_PERIOD);

            mScanning = true;
            getBluetoothAdapter().startLeScan(mLeScanCallback);
            //mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback); TODO: заменить deprecated  сканирование
        } else {
            if (Settings.debug) Log.i(TAG, "stop scanLeDevice()");
            mScanning = false;
            getBluetoothAdapter().stopLeScan(mLeScanCallback);
        }
        mIBluetoothListener.changeOptionMenu();
    }

// TODO: заменить deprecated  сканирование
//    private ScanCallback mScanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//            if (Settings.debug) Log.i(TAG, "onScanResult()");
//        }
//
//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
//            if (Settings.debug) Log.i(TAG, "onBatchScanResults()");
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//            if (Settings.debug) Log.i(TAG, "onScanFailed()");
//        }
//    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    //if (Settings.debug) Log.i(TAG, "onLeScan()");
                    iScannListener.scanCallback(device, rssi);
                }
            };

    @Override
    public boolean menuChangeCondition() {
        return mScanning;
    }
}
