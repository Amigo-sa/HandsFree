package by.citech.handsfree.bluetoothlegatt.ui;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.handsfree.application.ThisApp;
import by.citech.handsfree.bluetoothlegatt.ConnectAction;
import by.citech.handsfree.bluetoothlegatt.ConnectorBluetooth;
import by.citech.handsfree.bluetoothlegatt.IBluetoothListener;
import by.citech.handsfree.bluetoothlegatt.IBtList;
import by.citech.handsfree.bluetoothlegatt.ui.uicommands.UiController;
import by.citech.handsfree.bluetoothlegatt.ui.uicommands.button.ButtonChangeViewOffCommand;
import by.citech.handsfree.bluetoothlegatt.ui.uicommands.button.ButtonChangeViewOnCommand;
import by.citech.handsfree.bluetoothlegatt.ui.uicommands.dialogs.ChoseDialogCommand;
import by.citech.handsfree.bluetoothlegatt.ui.uicommands.dialogs.ConnInfoDialogCommand;
import by.citech.handsfree.bluetoothlegatt.ui.uicommands.dialogs.ConnectDialogCommand;
import by.citech.handsfree.bluetoothlegatt.ui.uicommands.dialogs.DisconnInfoDialogCommand;
import by.citech.handsfree.bluetoothlegatt.ui.uicommands.dialogs.DisconnectDialogCommand;
import by.citech.handsfree.bluetoothlegatt.ui.uicommands.dialogs.ReconnectDialogCommand;
import by.citech.handsfree.ui.IBtToUiCtrl;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.ui.ISwipeListener;

public class BluetoothUi implements IUiToBtListener,
                                    ConnectAction,
                                    ISwipeListener,
                                    IMenuListener {

    private static final String TAG = "WSD_BleUi";
    private static final boolean debug = Settings.debug;

    private UiController uiController;
    private ConnectDialogCommand connDialogOn;
    private DisconnectDialogCommand discDialogOn;
    private ReconnectDialogCommand reconnDiaologOn;
    private ConnInfoDialogCommand connDialogInfoOn;
    private DisconnInfoDialogCommand disconnDialogInfoOn;
    private ChoseDialogCommand choseDialogCommand;

    private ButtonChangeViewOnCommand buttonViewColorChangeOn;
    private ButtonChangeViewOffCommand buttonViewColorChangeOff;
    private IMsgToUi iMsgToUi;
    private IBtToUiCtrl iBtToUiCtrl;
    private IBluetoothListener mIBluetoothListener;
    private IBtList iBtList;
    private IBtList iBtConnectList;
    private BluetoothDevice mBtDevice;

    //--------------------- singleton

    private static volatile BluetoothUi instance = null;

    private BluetoothUi() {
        uiController = new UiController();

        buttonViewColorChangeOn = new ButtonChangeViewOnCommand();
        buttonViewColorChangeOff = new ButtonChangeViewOffCommand();

        choseDialogCommand = new ChoseDialogCommand(this);
        discDialogOn = new DisconnectDialogCommand(ConnectorBluetooth.getInstance());
        reconnDiaologOn = new ReconnectDialogCommand(ConnectorBluetooth.getInstance());
        connDialogOn = new ConnectDialogCommand(ConnectorBluetooth.getInstance());
        disconnDialogInfoOn = new DisconnInfoDialogCommand();
        connDialogInfoOn = new ConnInfoDialogCommand();

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

    //--------------- setters ------------------

    public BluetoothUi setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
        return this;
    }

    public BluetoothUi setiBtToUiCtrl(IBtToUiCtrl iBtToUiCtrl) {
        this.iBtToUiCtrl = iBtToUiCtrl;
        return this;
    }

    public BluetoothUi setmIBluetoothListener(IBluetoothListener mIBluetoothListener) {
        this.mIBluetoothListener = mIBluetoothListener;
        return this;
    }

    public BluetoothUi setiBtList(IBtList iBtList) {
        this.iBtList = iBtList;
        return this;
    }

    public BluetoothUi setiBtConnectList(IBtList iBtConnectList) {
        this.iBtConnectList = iBtConnectList;
        return this;
    }

    public BluetoothUi registerListenerBroadcast() {
        ThisApp.getBroadcastReceiverWrapper().registerListener(this);
        return this;
    }

    public BluetoothUi build() {
        buttonViewColorChangeOn.setiBluetoothListener(mIBluetoothListener);
        buttonViewColorChangeOff.setBluetoothListener(mIBluetoothListener);
        choseDialogCommand.setiMsgToUi(iMsgToUi);
        connDialogOn.setiMsgToUi(iMsgToUi);
        discDialogOn.setiMsgToUi(iMsgToUi);
        reconnDiaologOn.setiMsgToUi(iMsgToUi);
        disconnDialogInfoOn.setiBtToUiCtrl(iBtToUiCtrl);
        disconnDialogInfoOn.setiMsgToUi(iMsgToUi);
        connDialogInfoOn.setiBtToUiCtrl(iBtToUiCtrl);
        connDialogInfoOn.setiMsgToUi(iMsgToUi);
        return this;
    }

    @Override
    public void clickItemList(BluetoothDevice device) {
        mBtDevice = device;
        choseDialogCommand.setDevice(device);
        connDialogOn.setDevice(device);
        discDialogOn.setDevice(device);
        reconnDiaologOn.setDevice(device);
        disconnDialogInfoOn.setDevice(device);
        connDialogInfoOn.setDevice(device);

        if (device.equals(getConnectedDevice())) {
            uiController.setCommand(discDialogOn).execute();
        } else if (getConnectedDevice() != null) {
            uiController.setCommand(reconnDiaologOn).execute();
        } else {
            uiController.setCommand(choseDialogCommand).execute();
        }
    }

    @Override
    public void clickBtnListener() {
        ConnectorBluetooth.getInstance().setScanFilter(false);
        ConnectorBluetooth.getInstance().startScan();
    }

    public void clickBtnChoseProceed() {
        ConnectorBluetooth.getInstance().getDeviceForConnecting(mBtDevice);
        iBtConnectList.addDevice(mBtDevice, true, false);
        iBtList.removeDevice(mBtDevice);
    }

    @Override
    public boolean isScanning() {
        return ConnectorBluetooth.getInstance().isScanning();
    }

    @Override
    public boolean isConnecting() {
        return ConnectorBluetooth.getInstance().isConnecting();
    }

    @Override
    public boolean isConnected() {
        return ConnectorBluetooth.getInstance().isConnected();
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
        return ConnectorBluetooth.getInstance().getConnectedDevice();
    }

    //------------------ ConnectAction --------------------

    @Override
    public void actionConnected() {
//        uiController.setCommand(connDialogOn).undo();
//        uiController.setCommand(connDialogInfoOn).execute();
//        uiController.setCommand(connDialogInfoOn).undo();
        uiController.setCommand(buttonViewColorChangeOn).execute();
        iBtConnectList.clear();
        iBtConnectList.addDevice(mBtDevice, false, true);
        iBtList.removeDevice(mBtDevice);

    }

    @Override
    public void actionDisconnected() {
        //uiController.setCommand(connDialogOn).undo();
        uiController.setCommand(disconnDialogInfoOn).execute();
        uiController.setCommand(disconnDialogInfoOn).undo();
        uiController.setCommand(buttonViewColorChangeOff).execute();
        iBtConnectList.clear();
    }

    @Override
    public void actionServiceDiscovered() {

    }

    @Override
    public void actionDescriptorWrite() {

    }

    //------------------ ISwipeListener --------------------

    @Override
    public void onSwipe(SwipeDirection direction) {
        switch (direction){
            case UP:
                //ConnectorBluetooth.getInstance().stopScan();
                break;
            case DOWN:
                //ConnectorBluetooth.getInstance().startScan();
                break;
            case LEFT:
            case RIGH:
            default:
                break;
        }
    }

    //------------------ IMenuListener --------------------

    @Override
    public void menuScanStartListener() {
        ConnectorBluetooth.getInstance().startScan();
    }

    @Override
    public void menuScanStopListener() {
        ConnectorBluetooth.getInstance().stopScan();
    }

}
