package by.citech.logic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.bluetoothlegatt.Characteristics;
import by.citech.bluetoothlegatt.ControlAdapter;
import by.citech.bluetoothlegatt.LeBroadcastReceiver;
import by.citech.bluetoothlegatt.LeConnector;
import by.citech.bluetoothlegatt.LeDataTransmitter;
import by.citech.bluetoothlegatt.LeDeviceListAdapter;
import by.citech.bluetoothlegatt.LeScanner;
import by.citech.bluetoothlegatt.StorageListener;
import by.citech.data.StorageData;
import by.citech.debug.IDebugListener;
import by.citech.exchange.ITransmitter;
import by.citech.gui.ICallUiExchangeListener;
import by.citech.param.DebugMode;
import by.citech.param.Settings;

public class ConnectorBluetooth
        implements ICallNetExchangeListener, ICallUiExchangeListener, IDebugListener, StorageListener{

    private final static String TAG = "WSD_ConnectorBluetooth";
    private final static DebugMode debugMode = Settings.debugMode;

    // обьявляем сервис для обработки соединения и передачи данных (клиент - сервер)
    private BluetoothLeService mBluetoothLeService;
    private Handler mHandler;
    // BLE устройство, с которым будем соединяться
    private BluetoothDevice mBTDevice;
    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private BluetoothAdapter mBluetoothAdapter;
    // сканер имеющихся по близости BLE устройств
    private LeScanner leScanner;
    // хранилища данных
    private StorageData<byte[]> storageBtToNet;
    private StorageData<byte[][]> storageNetToBt;
    // адаптер найденных устройств и управление для него
    private ControlAdapter controlAdapter;
    // список сервисов и характеристик устройства
    private Characteristics characteristics;
    // обьект производящий соединение/разьединение BLE устройства
    private LeConnector leConnector;
    // приёмник/передатчик данных
    private LeDataTransmitter leDataTransmitter;
    //////////////////////////////////////////
    // Слушатели событий всей  BLE  периферии
    //////////////////////////////////////////
    private LeBroadcastReceiver leBroadcastReceiver;
    private IBluetoothListener mIBluetoothListener;
    private ITransmitter iTransmitter;

    //--------------------- singleton

    private static volatile ConnectorBluetooth instance = null;

    private ConnectorBluetooth() {

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

    public void build(){
        if (Settings.debug) Log.i(TAG,"build()");
        characteristics = new Characteristics(mIBluetoothListener);
        controlAdapter = new ControlAdapter(mIBluetoothListener);
        leScanner = new LeScanner(
                mHandler,
                mBluetoothAdapter,
                mIBluetoothListener,
                controlAdapter);
        leBroadcastReceiver = new LeBroadcastReceiver(
                controlAdapter,
                leScanner,
                characteristics,
                mIBluetoothListener,
                this);
        leBroadcastReceiver.addIRxDataListener(iTransmitter);
        leConnector = new LeConnector(
                leScanner,
                leBroadcastReceiver,
                mIBluetoothListener);
        leDataTransmitter = new LeDataTransmitter(
                characteristics,
                mIBluetoothListener);
    }

    //--------------------- getters and setters

    public ConnectorBluetooth setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
        return this;
    }

    //TODO: добавить
    public IDebugListener getiDebugBtToNetListener() {
        return null;
    }

    //TODO: добавить
    public IDebugListener getiDebugNetToBtListener() {
        return null;
    }

    public boolean ismConnected() {
        return leBroadcastReceiver.isConnected();
    }

    public ConnectorBluetooth addIRxDataListener(ITransmitter iTransmitter) {
        this.iTransmitter = iTransmitter;
       return this;
    }

    public boolean ismScanning() {
        return leScanner.isScanning();
    }

    public void setmBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
        leBroadcastReceiver.setBTDevice(mBTDevice);
        leConnector.setBTDevice(mBTDevice);
    }

    public BluetoothDevice getmBTDevice() {
        return mBTDevice;
    }

    public BluetoothDevice getmBTDeviceConn() {
        return leBroadcastReceiver.getmBTDeviceConn();
    }

    public LeDeviceListAdapter getmLeDeviceListAdapter() {
        return controlAdapter.getLeDeviceListAdapter();
    }

    public ConnectorBluetooth setiBluetoothListener(IBluetoothListener mIBluetoothListener) {
        this.mIBluetoothListener = mIBluetoothListener;
        return this;
    }

    public ConnectorBluetooth setStorageBtToNet(StorageData<byte[]> storageBtToNet){
        this.storageBtToNet = storageBtToNet;
        return this;
    }

    public ConnectorBluetooth setStorageNetToBt(StorageData<byte[][]> storageNetToBt){
        this.storageNetToBt = storageNetToBt;
        return this;
    }

    //---------------------------------Bluetooth -----------------------------
    public boolean getBluetoothAdapter(BluetoothManager bluetoothManager) {
        if (Settings.debug) Log.i(TAG, "getBluetoothAdapter()");
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
            return false;
        else
            return true;
    }

    public BluetoothAdapter getBTAdapter(){
        return mBluetoothAdapter;
    }

    //------------------ inittialization-------------------------

    public void initListBTDevice() {
        controlAdapter.initializeListBluetoothDevice();
    }

    //----------------------- Scanning---------------------------

    public void startScanBTDevices(){
        if (leScanner != null) {
            leScanner.startScanBluetoothDevice();
        }
    }

    public void stopScanBTDevice(){
        if (leScanner != null) {
            leScanner.stopScanBluetoothDevice();
        }
    }

    public void scanWork() {
        if (controlAdapter != null) {
            controlAdapter.clearAllDevicesFromList();
            controlAdapter.addConnectDeviceToList();
        }
        startScanBTDevices();
    }

    //----------------- Connection/Disconnection ----------------

    public void disconnectWork() {// for dialog
        if (controlAdapter != null && leConnector != null) {
            controlAdapter.clearAllDevicesFromList();
            leConnector.disconnectBTDevice();
        }
        startScanBTDevices();
    }

    public void disconnectBTDevice() {
        leConnector.disconnectBTDevice();
    }

    public void onConnectBTDevice() {
        leConnector.onConnectBTDevice();
    }

    public final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                if (Settings.debug) Log.e(TAG, "Unable to initialize Bluetooth");
                mIBluetoothListener.finishConnection();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (mBluetoothLeService != null && leBroadcastReceiver != null && leConnector != null && leDataTransmitter != null) {
                if (mBTDevice != null)
                    mBluetoothLeService.connect(mBTDevice.getAddress());
                leBroadcastReceiver.setBluetoothLeService(mBluetoothLeService);
                leConnector.setBluetoothLeService(mBluetoothLeService);
                leDataTransmitter.setBluetoothLeService(mBluetoothLeService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (Settings.debug) Log.i(TAG, "onServiceDisconnected()");
            mBluetoothLeService = null;
        }
    };

    public void closeLeService(){
        if (Settings.debug) Log.i(TAG, "closeLeService()");
        if(mBluetoothLeService.getWriteThread() != null)
            mBluetoothLeService.stopWriteThread();
        mBluetoothLeService = null;
    }

    //----------------------- BroadcastReceiver ---------------------

    public void updateBCRData(){
        if (leBroadcastReceiver != null) {
            leBroadcastReceiver.updateBroadcastReceiveData();
        }
    }

    public void unregisterReceiver(){
        if (leBroadcastReceiver != null) {
            leBroadcastReceiver.unregisterReceiver();
        }
    }

    //------------ устанавливаем хранилища для данных ---------------

    @Override
    public void setStorages(){
        if (Settings.debug) Log.i(TAG, "setStorages()");
        mBluetoothLeService.setStorageBtToNet(storageBtToNet);
        mBluetoothLeService.setStorageNetToBt(storageNetToBt);
    }

    //---------------------- dataexchange ---------------------------

    public void enableTransmitData() {
        leDataTransmitter.enableTransmitData();
    }

    public void disableTransmitData() {
        leDataTransmitter.disableTransmitData();
    }

    @Override
    public void callEndedInternally() {
        disableTransmitData();
    }

    @Override
    public void callIncomingAccepted() {
        enableTransmitData();
    }

    @Override
    public void callOutcomingAccepted() {
        enableTransmitData();
    }

    @Override
    public void callFailed() {
        disableTransmitData();
    }

    @Override
    public void callEndedExternally() {
        disableTransmitData();
    }

    //--------------------- debug

    @Override
    public void startDebug() {
        if (Settings.debug) Log.i(TAG, "startDebug");
        CallerState currentState = getCallerState();
        if (Settings.debug) Log.i(TAG, currentState.getName());
        switch (debugMode) {
            case Record:
                if (currentState == CallerState.DebugRecord) {
                    enableTransmitData();
                }
                break;
            default:
                enableTransmitData();
                break;
        }
    }

    @Override
    public void stopDebug() {
        if (debugMode != DebugMode.Record) {
            disableTransmitData();
        }
    }

    private String getCallerStateName() {
        return Caller.getInstance().getCallerState().getName();
    }

    private CallerState getCallerState() {
        return Caller.getInstance().getCallerState();
    }

}