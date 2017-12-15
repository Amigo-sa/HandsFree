package by.citech.handsfree.logic;

import by.citech.handsfree.gui.UiBtListener;
import by.citech.handsfree.param.Settings;

/**
 * Created by tretyak on 15.12.2017.
 */

public class BleUi implements UiBtListener{

    private static final String TAG = "WSD_BleUi";
    private static final boolean debug = Settings.debug;

    //--------------------- singleton

    private static volatile BleUi instance = null;

    private BleUi() {
    }

    public static BleUi getInstance() {
        if (instance == null) {
            synchronized (BleUi.class) {
                if (instance == null) {
                    instance = new BleUi();
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
