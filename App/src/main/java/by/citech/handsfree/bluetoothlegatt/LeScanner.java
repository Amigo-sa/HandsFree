package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import by.citech.handsfree.application.ThisApp;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public class LeScanner {

    private final static String STAG = "WSD_LeScanner";
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private boolean mScanning;
    private boolean scanWithFilter;

    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private BluetoothAdapter bluetoothAdapter;
    private IScanListener iScanListener;

    private List<ScanFilter> scanFilters;
    private ScanSettings scanSettings;
    private String deviceAddress;

    public LeScanner() {

    }
    //--------------------- getters and setters

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
        if (deviceAddress != null)
            this.scanWithFilter = true;
    }

    public void setScanWithFilter(boolean scanWithFilter) {
        this.scanWithFilter = scanWithFilter;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void setiScanListener(IScanListener iScanListener) {
        this.iScanListener = iScanListener;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            Timber.w("getBluetoothAdapter bluetoothAdapter is null, get");
            bluetoothAdapter = BluetoothLeCore.getBluetoothAdapter();
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


    private void startInnerScan(BluetoothLeScanner leScanner, ScanSettings scanSettings){
        Timber.i("start scanLeDevice()");
        leScanner.startScan((scanWithFilter) ? scanFilters : null, scanSettings, mScanCallback);
        if (!scanWithFilter) mHandler.post(() -> iScanListener.onStartScan());
        mScanning = true;
    }

    private void stopInnerScan(BluetoothLeScanner leScanner){
        Timber.i("stop scanLeDevice()");
        mScanning = false;
        leScanner.stopScan(mScanCallback);
        mHandler.post(() -> iScanListener.onStopScan());
    }


    private void stopScanThroughPeriod(BluetoothLeScanner leScanner){
        mHandler.postDelayed(() -> {
            stopInnerScan(leScanner);
        }, SCAN_PERIOD);
    }



    // процедура сканирования устройства
    private void scanLeDevice(final boolean enable) {

        final BluetoothLeScanner leScanner = getBluetoothAdapter().getBluetoothLeScanner();

        if (enable) {

            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .build();
            //scan specified devices only with ScanFilter  // С использованием фильтрации
            if (scanWithFilter) {
                ScanFilter scanFilter = new ScanFilter.Builder()
                        .setDeviceAddress(deviceAddress)
                        .build();
                scanFilters = new ArrayList<ScanFilter>();
                scanFilters.add(scanFilter);
            }

            // Stops scanning after a pre-defined scan period.
            if (!scanWithFilter) stopScanThroughPeriod(leScanner);
            startInnerScan(leScanner, scanSettings);

        } else
            stopInnerScan(leScanner);
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //if (Settings.debug) Log.i(TAG, "onScanResult() ");
            iScanListener.scanCallback(result.getDevice(), result.getRssi());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Timber.i("onBatchScanResults() ");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Timber.i("onScanFailed() %s", errorCode);
        }
    };

    public boolean isScanning() {
        return mScanning;
    }
}
