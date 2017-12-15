package by.citech.handsfree.logic;

import by.citech.handsfree.gui.IUiToBtListener;
import by.citech.handsfree.settings.Settings;

/**
 * Created by tretyak on 15.12.2017.
 */

public class BluetoothUi implements IUiToBtListener {

    private static final String TAG = "WSD_BleUi";
    private static final boolean debug = Settings.debug;

    //--------------------- singleton

    private static volatile BluetoothUi instance = null;

    private BluetoothUi() {
    }

    public static BluetoothUi getInstance() {
        if (instance == null) {
            synchronized (BluetoothUi.class) {
                if (instance == null) {
                    instance = new BluetoothUi();
                }
            }
        }
        return instance;
    }


    @Override
    public void scanItemSelectedListener() {
        ConnectorBluetooth.getInstance().scanWork();
    }

    @Override
    public void stopItemSelectedListener() {
        ConnectorBluetooth.getInstance().stopScanBTDevice();
    }

    @Override
    public void clickItemListListener(int position) {
        ConnectorBluetooth.getInstance().clickItemList(position);
    }

    @Override
    public void clickBtnChangeDeviceListenerOne() {
        ConnectorBluetooth.getInstance().initListBTDevice();
    }

    @Override
    public void clickBtnChangeDeviceListenerTwo() {
        ConnectorBluetooth.getInstance().startScanBTDevices();
    }

    @Override
    public void swipeScanStartListener() {
        ConnectorBluetooth.getInstance().scanWork();
    }

    @Override
    public void swipeScanStopListener() {
        ConnectorBluetooth.getInstance().stopScanBTDevice();
    }
}
