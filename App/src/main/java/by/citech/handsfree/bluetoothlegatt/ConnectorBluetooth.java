package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import by.citech.handsfree.application.ThisApplication;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.call.fsm.ICallFsmListenerRegister;
import by.citech.handsfree.call.fsm.ICallFsmReporter;
import by.citech.handsfree.call.fsm.ICallFsmListener;
import by.citech.handsfree.data.StorageListener;
import by.citech.handsfree.connection.fsm.EConnectionReport;
import by.citech.handsfree.connection.fsm.EConnectionState;
import by.citech.handsfree.connection.fsm.IConnectionFsmListener;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.bluetoothlegatt.commands.blecommands.BLEController;
import by.citech.handsfree.bluetoothlegatt.commands.blecommands.characteristics.CharacteristicsDisplayOnCommand;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.bluetoothlegatt.commands.blecommands.connect.ConnectCommand;
import by.citech.handsfree.bluetoothlegatt.commands.blecommands.dataexchange.DataExchangeOffCommand;
import by.citech.handsfree.bluetoothlegatt.commands.blecommands.dataexchange.DataExchangeOnCommand;
import by.citech.handsfree.bluetoothlegatt.commands.blecommands.connect.DisconnectCommand;
import by.citech.handsfree.bluetoothlegatt.commands.blecommands.dataexchange.ReceiveDataOn;
import by.citech.handsfree.bluetoothlegatt.commands.blecommands.scanner.ScanOffCommand;
import by.citech.handsfree.bluetoothlegatt.commands.blecommands.scanner.ScanOnCommand;
import by.citech.handsfree.bluetoothlegatt.rwdata.Characteristics;
import by.citech.handsfree.bluetoothlegatt.rwdata.LeDataTransmitter;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.call.fsm.ECallReport.CallFailedInt;
import static by.citech.handsfree.call.fsm.ECallReport.StopDebug;
import static by.citech.handsfree.call.fsm.ECallReport.SysIntConnectedIncompatible;
import static by.citech.handsfree.call.fsm.ECallReport.SysIntError;
import static by.citech.handsfree.call.fsm.ECallReport.SysIntReady;

public class ConnectorBluetooth
        implements StorageListener,
                   ConnectAction,
                   ICallFsmListener,
                   ICallFsmReporter,
                   ICallFsmListenerRegister,
                   IConnectionFsmListener {

    private final static String STAG = Tags.ConnectorBluetooth;

    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    // обьявляем сервис для обработки соединения и передачи данных (клиент - сервер)
    private BluetoothLeCore mBluetoothLeCore;
    private Handler mHandler;
    // BLE устройство, с которым будем соединяться
    private BluetoothDevice mBTDevice;
    private BluetoothDevice mBTDeviceConn;
    // сканер имеющихся по близости BLE устройств
    private LeScanner leScanner;
    // хранилища данных
    private volatile StorageData<byte[]> storageFromBt;
    private volatile StorageData<byte[][]> storageToBt;
    // список сервисов и характеристик устройства
    private Characteristics characteristics;
    // приёмник/передатчик данных
    private LeDataTransmitter leDataTransmitter;
    //////////////////////////////////////////
    // Слушатели событий всей  BLE  периферии
    //////////////////////////////////////////
    //private BroadcastReceiverWrapper broadcastReceiverWrapper;
    private IBluetoothListener mIBluetoothListener;
    private IRxComplex iRxComplex;
    //для дебага
    private long start_time;

    private volatile BluetoothLeState BLEState;

    private BLEController bleController;

    // команды
    private Command scanOn;
    private Command scanOff;

    private ConnectCommand connectDevice;
    private DisconnectCommand disconnectDevice;

    private Command exchangeDataOn;
    private Command exchangeDataOff;
    private Command receiveDataOn;

    private CharacteristicsDisplayOnCommand characteristicDisplayOn;
    //--------------------- singleton

    private static volatile ConnectorBluetooth instance = null;

    private ConnectorBluetooth() {
        BLEState = BluetoothLeState.DISCONECTED;
        //broadcastReceiverWrapper = new BroadcastReceiverWrapper();
        leScanner = new LeScanner();

        characteristics = new Characteristics();
        leDataTransmitter = new LeDataTransmitter(characteristics);
        // инициализация контроллера команд
        bleController = new BLEController();

        exchangeDataOn = new DataExchangeOnCommand(leDataTransmitter);
        exchangeDataOff = new DataExchangeOffCommand(leDataTransmitter);
        receiveDataOn = new ReceiveDataOn(leDataTransmitter);

        // инициализация комманд
        scanOn = new ScanOnCommand(leScanner);
        scanOff = new ScanOffCommand(leScanner);

        connectDevice = new ConnectCommand();
        disconnectDevice = new DisconnectCommand();

        characteristicDisplayOn = new CharacteristicsDisplayOnCommand(characteristics);
        mBluetoothLeCore = BluetoothLeCore.getInstance();
    }

    public static ConnectorBluetooth getInstance() {
        if (instance == null) {
            synchronized (ConnectorBluetooth.class) {
                if (instance == null) {
                    instance = new ConnectorBluetooth();
                }
            }
        }
        return instance;
    }

    private boolean isBleSupported(){
        return ThisApplication.getAppContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private boolean isBtSuppported(){
        BluetoothManager bluetoothManager = ThisApplication.getBluetoothManager();
        BluetoothAdapter bluetoothAdapter = ThisApplication.getBluetoothAdapter();
        return !(bluetoothManager == null || bluetoothAdapter == null);
    }

    private void enableBt(){
        BluetoothAdapter bluetoothAdapter = ThisApplication.getBluetoothAdapter();
        if (!bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();
    }

    public void build() {
        if (Settings.debug) Log.i(TAG, "build");

        leDataTransmitter.addIRxDataListener(iRxComplex);
        leDataTransmitter.setBluetoothLeCore(mBluetoothLeCore);
        ThisApplication.registerBroadcastListener(this);
    }

    //--------------------- IBase

    public void onStop() {
        if (Settings.debug) Log.i(TAG, "onStop");
        unregisterCallerFsmListener(this, TAG);

        if (getBLEState() == BluetoothLeState.TRANSMIT_DATA)
            bleController.setCommand(exchangeDataOff).execute();

        mBTDevice = null;
        mBTDeviceConn = null;
    }

    //--------------------- getters and setters

    public ConnectorBluetooth setmHandler(Handler mHandler) {
        if (Settings.debug) Log.i(TAG, "mHandler = " + mHandler);
        leScanner.setHandler(mHandler);
        this.mHandler = mHandler;
        return this;
    }

    public ConnectorBluetooth addIRxDataListener(IRxComplex iRxComplex) {
        this.iRxComplex = iRxComplex;
        return this;
    }

    private void setmBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
    }

    public BluetoothDevice getConnectDevice() {
        return mBTDeviceConn;
    }

    public boolean isScanning() {
        return leScanner.isScanning();
    }

    public boolean isConnecting() {
        return getBLEState() != BluetoothLeState.DISCONECTED;
    }

    public ConnectorBluetooth setiBluetoothListener(IBluetoothListener mIBluetoothListener) {
        if (Settings.debug) Log.i(TAG, "mIBluetoothListener = " + mIBluetoothListener);
        characteristics.setIBluetoothListener(mIBluetoothListener);
        leDataTransmitter.setIBluetoothListener(mIBluetoothListener);
        this.mIBluetoothListener = mIBluetoothListener;
        return this;
    }

    public ConnectorBluetooth setStorageFromBt(StorageData<byte[]> storageFromBt) {
        this.storageFromBt = storageFromBt;
        return this;
    }

    public ConnectorBluetooth setStorageToBt(StorageData<byte[][]> storageToBt) {
        this.storageToBt = storageToBt;
        return this;
    }

    public ConnectorBluetooth setiScanListener(IScanListener mIScanListener) {
        leScanner.setiScanListener(mIScanListener);
        return this;
    }

    //------------------ get Device from adapter-------------------------

    public void getDeviceForConnecting(BluetoothDevice device) {
        setmBTDevice(device);
        //------------------ Команда соединения -----------
        connectDevice.setmBluetoothLeService(mBluetoothLeCore);
        disconnectDevice.setmBluetoothLeService(mBluetoothLeCore);
        connectDevice.setmBTDevice(device);
        //-------------------Команды для определения характеристик --------
        characteristicDisplayOn.setBluetoothLeService(mBluetoothLeCore);

        bleController.setCommand(scanOff).execute();
        if (Settings.debug) Log.i(TAG, "mBTDevice = " + device);
        if (Settings.debug) Log.i(TAG, "mBTDeviceConn = " + mBTDeviceConn);

        if (!device.equals(mBTDeviceConn) && mBTDeviceConn == null) {
            start_time = System.currentTimeMillis();
            bleController.setCommand(connectDevice).execute();
        }
    }

    //--------------------------- Callbacks from BroadcastReceiver --------------------
    @Override
    public void actionConnected() {
        setBLEState(getBLEState(), BluetoothLeState.CONNECTED);
        if (Settings.debug) Log.i(TAG, "mBTDevice = " + mBTDevice);
        mBTDeviceConn = mBTDevice;

        long end_time = System.currentTimeMillis();
        if (Settings.debug) Log.i(TAG, "Connecting await time = " + (end_time - start_time));
        setStorages();
    }

    @Override
    public void actionDisconnected() {
        if (Settings.debug) Log.i(TAG, "actionDisconnected()");

        if (BLEState != BluetoothLeState.DISCONECTED) {
            processState();
            mBTDeviceConn = null;

            if (BLEState == BluetoothLeState.TRANSMIT_DATA) {
                bleController.setCommand(exchangeDataOff).execute();
            }
            bleController.setCommand(scanOn).execute();
            setBLEState(BLEState, BluetoothLeState.DISCONECTED);
        }
        switch (Settings.Common.opMode) {
            case Normal:
                reportToCallerFsm(getCallerFsmState(), SysIntError, TAG);
                break;
            default:
                reportToCallerFsm(getCallerFsmState(), StopDebug, TAG);
                break;
        }
    }

    private void processState() {
        ECallState callerState = getCallerFsmState();
        switch (callerState) {
            case Call:
                if (reportToCallerFsm(callerState, CallFailedInt, TAG)) return;
                else break;
            default:
                if (Settings.debug) Log.e(TAG, "processState " + callerState);
                return;
        }
        if (Settings.debug) Log.w(TAG, "processState recursive call");
        processState();
    }

    @Override
    public void actionServiceDiscovered() {
        setBLEState(getBLEState(), BluetoothLeState.SERVICES_DISCOVERED);
        bleController.setCommand(characteristicDisplayOn).execute();
        if (characteristics.getNotifyCharacteristic() != null && characteristics.getWriteCharacteristic() != null) {
            reportToCallerFsm(getCallerFsmState(), SysIntReady, TAG);
        } else
            reportToCallerFsm(getCallerFsmState(), SysIntConnectedIncompatible, TAG);
    }

    //----------------------- Scanning ---------------------------

    public void startScan() {
        bleController.setCommand(scanOn).execute();
    }

    public void stopScan() {
        bleController.setCommand(scanOff).execute();
    }

    //----------------- Connection/Disconnection ----------------

    public void disconnect() {
        if (getBLEState() == BluetoothLeState.TRANSMIT_DATA)
            bleController.setCommand(exchangeDataOff)
                    .setCommand(disconnectDevice)
                    .execute();
        else
            bleController.setCommand(disconnectDevice)
                    .execute();
    }

    public void connecting() {
        bleController.setCommand(connectDevice).execute();
    }

    //---------------------------- blecontroller states ------------------------------

    private synchronized BluetoothLeState getBLEState() {
        if (Settings.debug) Log.i(TAG, "getBLEState is " + BLEState.getName());
        return BLEState;
    }

    private synchronized boolean setBLEState(BluetoothLeState fromBLEState, BluetoothLeState toBLEState) {
        if (Settings.debug)
            Log.w(TAG, String.format("setState from %s to %s", fromBLEState.getName(), toBLEState.getName()));
        if (BLEState == fromBLEState) {
            if (fromBLEState.availableStates().contains(toBLEState)) {
                BLEState = toBLEState;
                if (BLEState == BluetoothLeState.DISCONECTED) {
                    BLEState = BluetoothLeState.DISCONECTED;
                }
                return true;
            } else {
                if (Settings.debug)
                    Log.e(TAG, String.format("setState: %s is not available from %s", toBLEState.getName(), fromBLEState.getName()));
            }
        } else {
            if (Settings.debug)
                Log.e(TAG, String.format("setState: current is not %s", fromBLEState.getName()));
        }
        return false;
    }

    //------------ устанавливаем хранилища для данных ---------------

    @Override
    public void setStorages() {
        if (Settings.debug) Log.i(TAG, "setStorages()");
        leDataTransmitter.setStorageFromBt((storageFromBt));
        leDataTransmitter.setStorageToBt(storageToBt);
        mBluetoothLeCore.setCallbackWriteListener(leDataTransmitter);
    }

    //---------------------- dataexchange ---------------------------

    private void enableTransmitData() {
        bleController.setCommand(exchangeDataOn).execute();
        setBLEState(BluetoothLeState.SERVICES_DISCOVERED, BluetoothLeState.TRANSMIT_DATA);
    }

    private void onlyReceiveData() {
        bleController.setCommand(receiveDataOn).execute();
        setBLEState(BluetoothLeState.SERVICES_DISCOVERED, BluetoothLeState.TRANSMIT_DATA);
    }

    private void disableTransmitData() {
        bleController.setCommand(exchangeDataOff).execute();
        setBLEState(BluetoothLeState.TRANSMIT_DATA, BluetoothLeState.SERVICES_DISCOVERED);
    }

    //--------------------- ICallFsmListener

    @Override
    public void onCallerStateChange(ECallState from, ECallState to, ECallReport why) {
        if (Settings.debug) Log.i(TAG, "onCallerStateChange");
        switch (why) {
            case InCallAcceptedByLocalUser:
            case OutCallAcceptedByRemoteUser:
                enableTransmitData();
                break;
            case CallEndedByLocalUser:
            case CallFailedExt:
            case CallEndedByRemoteUser:
                disableTransmitData();
                break;
            case StartDebug:
                switch (Settings.Common.opMode) {
                    case DataGen2Bt:
                    case AudIn2Bt:
                        enableTransmitData();
                        break;
                    case Bt2Bt:
                        if (getBLEState() == BluetoothLeState.SERVICES_DISCOVERED) {
                            enableTransmitData();
                        }
                        break;
                    case Record:
                        if (getCallerFsmState() == ECallState.DebugRecord) {
                            enableTransmitData();
                        }
                        break;
                    case Bt2AudOut:
                        onlyReceiveData();
                        break;
                    default:
                        break;
                }
                break;
            case StopDebug:
                switch (Settings.Common.opMode) {
                    default:
                        disableTransmitData();
                        break;
                }
                break;
        }
    }

    @Override
    public void onConnectionFsmStateChange(EConnectionState from, EConnectionState to, EConnectionReport why) {
        switch (why) {
            case TurningOn:
                if (Settings.debug) Log.i(TAG, "TurningOn");
                if (!isBtSuppported()) {
//                    reportToConnectionFsm(to, EConnectionReport.BtNotSupported, TAG);
                    return;
                }
                if (!isBleSupported()) {
//                    reportToConnectionFsm(to, EConnectionReport.BtLeNotSupported, TAG);
                    return;
                }
                enableBt();
                //build(); ToDo:

//                registerSuperDataConsumer(getTransmitter());
//                leDataTransmitter.addIRxDataListener(getReceiver());

                mBluetoothLeCore.initialize();
//                reportToConnectionFsm(getConnectionFsmState(), EConnectionReport.BtPrepared, TAG);
                break;
            case SearchStarted:
                if (Settings.debug) Log.i(TAG, "SearchStarted");
//                startScan();
//                state = STATE_SCANNING;
                break;
            case SearchStopped:
                if (Settings.debug) Log.i(TAG, "SearchStopped");
//                stopScan();
//                state = STATE_SCANSTOPED;
                break;
            case ConnectStarted:
                if (Settings.debug) Log.i(TAG, "ConnectStarted");
                connecting();
                break;
            case ConnectStopped:
                if (Settings.debug) Log.i(TAG, "ConnectStopped");
                disconnect();
                break;
            case TurningOff:
                if (Settings.debug) Log.i(TAG, "TurningOff");
                onStop();
                break;
            default:
                break;
        }
    }
}