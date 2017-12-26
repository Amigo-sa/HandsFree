package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import by.citech.handsfree.gui.IBtToUiListener;
import by.citech.handsfree.logic.IBluetoothListener;
import by.citech.handsfree.settings.Settings;

public class LeScanner implements IBtToUiListener {

    private final static String STAG = "WSD_LeScanner";
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private boolean mScanning;
    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private IBluetoothListener mIBluetoothListener;
    private IScannListener iScannListener;
    private BluetoothAdapter bluetoothAdapter;

    public LeScanner(IScannListener iScannListener) {
        this.iScannListener = iScannListener;
    }

    //--------------------- getters and setters

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void setIBluetoothListener(IBluetoothListener mIBluetoothListener) {
        this.mIBluetoothListener = mIBluetoothListener;
    }
    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            if (Settings.debug) Log.w(TAG, "getBluetoothAdapter bluetoothAdapter is null, get");
            bluetoothAdapter = mIBluetoothListener.getBluetoothManager().getAdapter();
        }
        return bluetoothAdapter;
    }

    public void startScanBluetoothDevice() {
        scanLeDevice(true);
    }

    public void stopScanBluetoothDevice() {
        scanLeDevice(false);
    }

    // процедура сканирования устройства
    private void scanLeDevice(final boolean enable) {
        final BluetoothLeScanner leScanner = getBluetoothAdapter().getBluetoothLeScanner();
        if (enable) {
            if (Settings.debug) Log.i(TAG, "start scanLeDevice()");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                if (Settings.debug) Log.i(TAG, "stop scanLeDevice()");
                mScanning = false;
                leScanner.stopScan(mScanCallback);

                mIBluetoothListener.changeOptionMenu();

            }, SCAN_PERIOD);

            mScanning = true;
            leScanner.startScan(mScanCallback);
        } else {
            if (Settings.debug) Log.i(TAG, "stop scanLeDevice()");
            mScanning = false;
            leScanner.stopScan(mScanCallback);
        }
        mIBluetoothListener.changeOptionMenu();
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //if (Settings.debug) Log.i(TAG, "onScanResult() ");
            iScannListener.scanCallback(result.getDevice(), result.getRssi());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            if (Settings.debug) Log.i(TAG, "onBatchScanResults() ");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (Settings.debug) Log.i(TAG, "onScanFailed() " + errorCode);
        }
    };

    @Override
    public boolean menuChangeCondition() {
        return mScanning;
    }
}
