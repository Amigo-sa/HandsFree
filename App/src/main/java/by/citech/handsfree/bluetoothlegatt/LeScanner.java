package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import by.citech.handsfree.application.ThisApplication;
import by.citech.handsfree.ui.IScanListener;
import by.citech.handsfree.settings.Settings;

public class LeScanner {

    private final static String STAG = "WSD_LeScanner";
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private boolean mScanning;

    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private IScannListener iScannListener;
    private BluetoothAdapter bluetoothAdapter;
    private IScanListener iScanListener;

    public LeScanner(IScannListener iScannListener) {
        this.iScannListener = iScannListener;
    }

    //--------------------- getters and setters

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void setiScanListener(IScanListener iScanListener) {
        this.iScanListener = iScanListener;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            if (Settings.debug) Log.w(TAG, "getBluetoothAdapter bluetoothAdapter is null, get");
            bluetoothAdapter = ThisApplication.getBluetoothManager().getAdapter();
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:11:22:33:AA:BB");
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

        //scan specified devices only with ScanFilter  // С использованием фильтрации

//        ScanFilter scanFilter = new ScanFilter.Builder()
//                        .setManufacturerData()
//                        .setDeviceAddress("")
//                        .build();
//        List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
//        scanFilters.add(scanFilter);
//        ScanSettings scanSettings = new ScanSettings.Builder().build();

        if (enable) {
            if (Settings.debug) Log.i(TAG, "start scanLeDevice()");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                if (Settings.debug) Log.i(TAG, "stop scanLeDevice()");
                mScanning = false;
                leScanner.stopScan(mScanCallback);
                mHandler.post(() -> iScanListener.onStopScan());
            }, SCAN_PERIOD);

            mScanning = true;

            //-------------- TEST START
            //leScanner.startScan(scanFilters, scanSettings, mScanCallback // с использованием фильтрации
            //leScanner.startScan(mScanCallback);
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .build();
            leScanner.startScan(null, settings, mScanCallback);
            mHandler.post(() -> iScanListener.onStartScan());
            //-------------- TEST STOP

        } else {
            if (Settings.debug) Log.i(TAG, "stop scanLeDevice()");
            mScanning = false;
            leScanner.stopScan(mScanCallback);
            mHandler.post(() -> iScanListener.onStopScan());
        }
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

    public boolean isScanning() {
        return mScanning;
    }
}
