package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Handler;

import by.citech.handsfree.application.ThisApp;
import by.citech.handsfree.bluetoothlegatt.fsm.EBtState;
import by.citech.handsfree.data.StorageListener;
import by.citech.handsfree.bluetoothlegatt.fsm.BtFsm.IBtFsmListener;
import by.citech.handsfree.bluetoothlegatt.fsm.BtFsm.IBtFsmReporter;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.bluetoothlegatt.fsm.EBtReport;
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
import by.citech.handsfree.settings.PreferencesProcessor;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.bluetoothlegatt.fsm.EBtReport.*;

public class ConnectorBluetooth
        implements StorageListener,
                   ConnectAction,
                   IBtFsmListener,
                   IBtFsmReporter,
                   IScanListener{

    private final static String STAG = Tags.ConnectorBluetooth;

    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    // обьявляем сервис для обработки соединения и передачи данных (клиент - сервер)
    private BluetoothLeCore mBluetoothLeCore;
    // BLE устройство, с которым будем соединяться
    private BluetoothDevice mBTDevice;
    private BluetoothDevice mBTDeviceConn;
    // сканер имеющихся по близости BLE устройств
    private LeScanner leScanner;
    private boolean scanWithFilter;
    private IScanListener mIScanListener;
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

        leScanner = new LeScanner();
        characteristics = new Characteristics();
        leDataTransmitter = new LeDataTransmitter(characteristics);
        // инициализация контроллера команд
        bleController = new BLEController();
        // инициализация комманд управления
        scanOn = new ScanOnCommand(leScanner);
        scanOff = new ScanOffCommand(leScanner);

        connectDevice = new ConnectCommand();
        disconnectDevice = new DisconnectCommand();

        characteristicDisplayOn = new CharacteristicsDisplayOnCommand(characteristics);

        exchangeDataOn = new DataExchangeOnCommand(leDataTransmitter);
        exchangeDataOff = new DataExchangeOffCommand(leDataTransmitter);
        receiveDataOn = new ReceiveDataOn(leDataTransmitter);
        // инициализация ядра BluetoothLe
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

    //--------------------- getters and setters ----------------

    public BluetoothDevice getConnectedDevice() {
        return mBTDeviceConn;
    }

    public boolean isScanning() {
        return leScanner.isScanning();
    }

    public boolean isConnected() {
        return getBLEState() != BluetoothLeState.DISCONECTED;
    }

    public ConnectorBluetooth setmHandler(Handler mHandler) {
        if (Settings.debug) Timber.i(TAG, "mHandler = %s", mHandler);
        leScanner.setHandler(mHandler);
        return this;
    }

    public ConnectorBluetooth addIRxDataListener(IRxComplex iRxComplex) {
        this.iRxComplex = iRxComplex;
        return this;
    }

    public ConnectorBluetooth setiBluetoothListener(IBluetoothListener mIBluetoothListener) {
        if (Settings.debug) Timber.i(TAG, "mIBluetoothListener = %s", mIBluetoothListener);
        characteristics.setIBluetoothListener(mIBluetoothListener);
        leDataTransmitter.setIBluetoothListener(mIBluetoothListener);
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

    public ConnectorBluetooth setiScanListener(IScanListener mIScanListener, boolean scanWithFilter) {
        this.mIScanListener = mIScanListener;
        setScanFilter(scanWithFilter);
        return this;
    }

    public void setScanFilter(boolean scanWithFilter){
        this.scanWithFilter = scanWithFilter;
        leScanner.setScanWithFilter(scanWithFilter);
        leScanner.setiScanListener(scanWithFilter ? this : mIScanListener);
    }

    //------------------ internal methods -------------------------

    private void setmBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
    }

    private boolean isBleSupported(){
        return ThisApp.getAppContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private boolean isBtSuppported(){
        BluetoothManager bluetoothManager = ThisApp.getBluetoothManager();
        BluetoothAdapter bluetoothAdapter = ThisApp.getBluetoothAdapter();
        return !(bluetoothManager == null || bluetoothAdapter == null);
    }

    private void enableBt(){
        BluetoothAdapter bluetoothAdapter = ThisApp.getBluetoothAdapter();
        if (!bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();
    }

    private void build() {
        if (Settings.debug) Timber.i(TAG, "build");

        leDataTransmitter.addIRxDataListener(iRxComplex);
        leDataTransmitter.setBluetoothLeCore(mBluetoothLeCore);
        ThisApp.registerBroadcastListener(this);
    }

    private void onStop() {
        if (Settings.debug) Timber.i(TAG, "onStop");
        if (getBLEState() == BluetoothLeState.TRANSMIT_DATA)
            bleController.setCommand(exchangeDataOff)
                         .setCommand(disconnectDevice)
                         .execute();

        mBTDevice = null;
        mBTDeviceConn = null;
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
        if (Settings.debug) Timber.i(TAG, "mBTDevice = %s", device);
        if (Settings.debug) Timber.i(TAG, "mBTDeviceConn = %s", mBTDeviceConn);

        if (!device.equals(mBTDeviceConn) && mBTDeviceConn == null) {
            start_time = System.currentTimeMillis();
            bleController.setCommand(connectDevice).execute();
        }
    }

    //--------------------------- Callbacks from Scanner

    @Override
    public void onStartScan() {

    }

    @Override
    public void onStopScan() {

    }

    @Override
    public void scanCallback(BluetoothDevice device, int rssi) {
        toBtFsm(EBtReport.RP_BtFound);
        stopScan();
    }

    //--------------------------- Callbacks from BroadcastReceiver --------------------

    @Override
    public void actionConnected() {
        setBLEState(getBLEState(), BluetoothLeState.CONNECTED);
        if (Settings.debug) Timber.i(TAG, "mBTDevice = %s", mBTDevice);
        mBTDeviceConn = mBTDevice;

        long end_time = System.currentTimeMillis();
        if (Settings.debug) Timber.i(TAG, "Connecting await time = %s", (end_time - start_time));
        setStorages();
    }

    @Override
    public void actionDisconnected() {
        if (Settings.debug) Timber.i(TAG, "actionDisconnected()");

        if (BLEState != BluetoothLeState.DISCONECTED) {
            mBTDeviceConn = null;

            if (BLEState == BluetoothLeState.TRANSMIT_DATA) {
                bleController.setCommand(exchangeDataOff).execute();
            }
            bleController.setCommand(scanOn).execute();
            setBLEState(BLEState, BluetoothLeState.DISCONECTED);
        }
        toBtFsm(RP_BtDisconnected);
    }

    @Override
    public void actionServiceDiscovered() {
        setBLEState(getBLEState(), BluetoothLeState.SERVICES_DISCOVERED);
        bleController.setCommand(characteristicDisplayOn).execute();
        if (characteristics.getNotifyCharacteristic() != null && characteristics.getWriteCharacteristic() != null) {
            toBtFsm(RP_BtConnectedCompatible);
            PreferencesProcessor.saveBtChosenAddrPref(mBTDevice.getAddress());
        } else
            toBtFsm(RP_BtConnectedIncompatible);
    }

    @Override
    public void actionDescriptorWrite() {
        if (BLEState == BluetoothLeState.TRANSMIT_DATA)
            toBtFsm(RP_BtExchangeEnabled);
        else
            toBtFsm(RP_BtExchangeDisabled);

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
            bleController.setCommand(disconnectDevice).execute();
    }

    public void connecting() {
        bleController.setCommand(connectDevice).execute();
    }

    //---------------------------- blecontroller states ------------------------------

    private synchronized BluetoothLeState getBLEState() {
        if (Settings.debug) Timber.i(TAG, "getBLEState is %s", BLEState.getName());
        return BLEState;
    }

    private synchronized boolean setBLEState(BluetoothLeState fromBLEState, BluetoothLeState toBLEState) {
        if (Settings.debug)
            Timber.w(TAG, "setState from %s to %s", fromBLEState.getName(), toBLEState.getName());
        if (BLEState == fromBLEState) {
            if (fromBLEState.availableStates().contains(toBLEState)) {
                BLEState = toBLEState;
                if (BLEState == BluetoothLeState.DISCONECTED) {
                    BLEState = BluetoothLeState.DISCONECTED;
                }
                return true;
            } else {
                if (Settings.debug)
                    Timber.e(TAG, "setState: %s is not available from %s", toBLEState.getName(), fromBLEState.getName());
            }
        } else {
            if (Settings.debug)
                Timber.e(TAG,  "setState: current is not %s", fromBLEState.getName());
        }
        return false;
    }

    //------------ устанавливаем хранилища для данных ---------------

    @Override
    public void setStorages() {
        if (Settings.debug) Timber.i(TAG, "setStorages()");
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

    private void searchDevice() {
        if (PreferencesProcessor.getBtChosenAddrPref() != null && PreferencesProcessor.getBtChosenAddrPref().length() > 0) {
            leScanner.setDeviceAddress(PreferencesProcessor.getBtChosenAddrPref());
            startScan();
        }
    }

    private boolean toBtFsm(EBtReport report) {
        return reportToBtFsm(report, getBtFsmState(), TAG);
    }

    private boolean toBtFsm(EBtReport report, EBtState from) {
        return reportToBtFsm(report, from, TAG);
    }

    @Override
    public void onFsmStateChange(EBtState from, EBtState to, EBtReport report) {
        switch (report) {
            case RP_TurningOn:

                if (Settings.debug) Timber.i(TAG, "RP_TurningOn");

                if (!isBtSuppported()) {
                    toBtFsm(RP_BtNotSupported, to);
                    return;
                }

                if (!isBleSupported()) {
                    toBtFsm(RP_BtLeNotSupported, to);
                    return;
                }

                mBluetoothLeCore.initialize();
                build();
                toBtFsm(RP_BtPrepared, to);
                break;

            case RP_Enable:
                if (Settings.debug) Timber.i(TAG, "RP_Enable");
                enableBt();
                if (ThisApp.getBluetoothAdapter().isEnabled())
                    toBtFsm(RP_BtEnabled, to);
                else
                    toBtFsm(RP_BtDisabled, to);
                break;

            case RP_BtEnabled:
                if (BluetoothAdapter.checkBluetoothAddress(PreferencesProcessor.getBtChosenAddrPref()))
                    toBtFsm(RP_BtChosenValid, to);
                else
                    toBtFsm(RP_BtChosenInvalid, to);
                break;

            case RP_SearchStart:
                if (Settings.debug) Timber.i(TAG, "RP_SearchStart");
                searchDevice();
                break;

            case RP_Connect:
                if (Settings.debug) Timber.i(TAG, "RP_Connect");
                connecting();
                break;

            case RP_ExchangeEnable:
                if (Settings.debug) Timber.i(TAG, "RP_ExchangeEnable");
                enableTransmitData();
                break;

            case RP_ExchangeDisable:
                if (Settings.debug) Timber.i(TAG, "RP_ExchangeDisable");
                disableTransmitData();
                break;

            case RP_SearchStop:
                if (Settings.debug) Timber.i(TAG, "RP_SearchStop");
                stopScan();
                break;

            case RP_Disconnect:
                if (Settings.debug) Timber.i(TAG, "ReportConnectStop");
                disconnect();
                break;

            case RP_Disable:
                if (ThisApp.getBluetoothAdapter().isEnabled())
                    ThisApp.getBluetoothAdapter().disable();
                break;

            case RP_TurningOff:
                if (Settings.debug) Timber.i(TAG, "RP_TurningOff");
                onStop();
                break;

            default:
                break;
        }
    }
}