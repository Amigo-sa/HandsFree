package by.citech.logic;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import by.citech.IService;
import by.citech.bluetoothlegatt.ConnectAction;
import by.citech.bluetoothlegatt.IReceive;
import by.citech.bluetoothlegatt.BluetoothLeState;
import by.citech.bluetoothlegatt.IVisible;
import by.citech.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.bluetoothlegatt.adapters.LeDeviceListAdapter;
import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.bluetoothlegatt.LeBroadcastReceiver;
import by.citech.bluetoothlegatt.LeConnector;
import by.citech.bluetoothlegatt.LeScanner;
import by.citech.bluetoothlegatt.commands.adapter.AddConnectDeviceToAdapterCommand;
import by.citech.bluetoothlegatt.commands.adapter.AddToListCommand;
import by.citech.bluetoothlegatt.commands.BLEController;
import by.citech.bluetoothlegatt.commands.characteristics.CharacteristicsDisplayOnCommand;
import by.citech.bluetoothlegatt.commands.receiver.RegisterReceiverCommand;
import by.citech.bluetoothlegatt.commands.receiver.UnregisterReceiverCommand;
import by.citech.bluetoothlegatt.commands.service.BindServiceCommand;
import by.citech.bluetoothlegatt.commands.button.ButtonChangeViewOffCommand;
import by.citech.bluetoothlegatt.commands.button.ButtonChangeViewOnCommand;
import by.citech.bluetoothlegatt.commands.adapter.ClearConnectDeviceFromAdapterCommand;
import by.citech.bluetoothlegatt.commands.adapter.ClearListCommand;
import by.citech.bluetoothlegatt.commands.service.CloseServiceCommand;
import by.citech.bluetoothlegatt.commands.Command;
import by.citech.bluetoothlegatt.commands.dialods.ConnInfoDialogCommand;
import by.citech.bluetoothlegatt.commands.connect.ConnectCommand;
import by.citech.bluetoothlegatt.commands.dialods.ConnectDialogCommand;
import by.citech.bluetoothlegatt.commands.dataexchange.DataExchangeOffCommand;
import by.citech.bluetoothlegatt.commands.dataexchange.DataExchangeOnCommand;
import by.citech.bluetoothlegatt.commands.dialods.DisconnInfoDialogCommand;
import by.citech.bluetoothlegatt.commands.connect.DisconnectCommand;
import by.citech.bluetoothlegatt.commands.dialods.DisconnectDialogCommand;
import by.citech.bluetoothlegatt.commands.adapter.InitListCommand;
import by.citech.bluetoothlegatt.commands.dataexchange.ReceiveDataOn;
import by.citech.bluetoothlegatt.commands.dialods.ReconnectDialogCommand;
import by.citech.bluetoothlegatt.commands.scanner.ScanOffCommand;
import by.citech.bluetoothlegatt.commands.scanner.ScanOnCommand;
import by.citech.bluetoothlegatt.commands.service.StartServiceCommand;
import by.citech.bluetoothlegatt.commands.service.UnbindServiceCommand;
import by.citech.bluetoothlegatt.rwdata.Characteristics;
import by.citech.bluetoothlegatt.rwdata.LeDataTransmitter;
import by.citech.bluetoothlegatt.StorageListener;
import by.citech.data.StorageData;
import by.citech.debug.IDebugListener;
import by.citech.exchange.IMsgToUi;
import by.citech.exchange.ITransmitter;
import by.citech.gui.ICallUiExchangeListener;
import by.citech.param.Settings;

public class ConnectorBluetooth
        implements ICallNetExchangeListener, ICallUiExchangeListener, IDebugListener, StorageListener, ConnectAction , IBase{

    private final static String TAG = "WSD_ConnectorBluetooth";

    private AlertDialog alertDialog;
    // обьявляем сервис для обработки соединения и передачи данных (клиент - сервер)
    private BluetoothLeService mBluetoothLeService;
    private Handler mHandler;
    // BLE устройство, с которым будем соединяться
    private BluetoothDevice mBTDevice;
    private BluetoothDevice mBTDeviceConn;
    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private BluetoothAdapter mBluetoothAdapter;
    // сканер имеющихся по близости BLE устройств
    private LeScanner leScanner;
    // хранилища данных
    private StorageData<byte[]> storageFromBt;
    private StorageData<byte[][]> storageToBt;
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
    //для дебага
    private boolean isDebugRunning;

private volatile BluetoothLeState BLEState;

    private IReceive iReceive;
    private IService iService;
    private IVisible iVisible;
    private IMsgToUi iMsgToUi;

    private Intent serviceIntent = new Intent("by.citech.bluetoothlegatt.BluetoothLeService");

    private BLEController bleController;

    // команды
    private Command scanOn;
    private Command scanOff;

    private Command addToList;
    private Command clearList;
    private Command initList;

    private Command connectDevice;
    private Command disconnectDevice;

    private Command exchangeDataOn;
    private Command exchangeDataOff;
    private Command receiveDataOn;

    private Command closeService;
    private Command startService;

    private Command bindService;
    private Command unbindService;

    private Command connDialogOn;
    private Command discDialogOn;
    private Command reconnDiaologOn;
    private Command connDialogInfoOn;
    private Command disconnDialogInfoOn;

    private Command buttonViewColorChangeOn;
    private Command buttonViewColorChangeOff;

    private Command addConnDeviceToAdapter;
    private Command clrConnDeviceFromAdapter;

    private Command registerReceiver;
    private Command unregisterReceiver;

    private Command characteristicDisplayOn;
    //--------------------- singleton

    private static volatile ConnectorBluetooth instance = null;

    private ConnectorBluetooth() {
        BLEState = BluetoothLeState.DISCONECTED;
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

     void build(){
        if (Settings.debug) Log.i(TAG,"build()");
        characteristics = new Characteristics(mIBluetoothListener);
        controlAdapter = new ControlAdapter(mIBluetoothListener);
        leScanner = new LeScanner(
                mHandler,
                mBluetoothAdapter,
                mIBluetoothListener,
                controlAdapter);
        leBroadcastReceiver = new LeBroadcastReceiver(this);
        leConnector = new LeConnector(leScanner);
        leDataTransmitter = new LeDataTransmitter(characteristics, mIBluetoothListener);
        leDataTransmitter.addIRxDataListener(iTransmitter);
        // инициализация контроллера команд
        bleController = new BLEController();


        // инициализация комманд
        scanOn = new ScanOnCommand(leScanner);
        scanOff = new ScanOffCommand(leScanner);

        addToList = new AddToListCommand(controlAdapter);
        clearList = new ClearListCommand(controlAdapter);
        initList = new InitListCommand(controlAdapter);

        connectDevice = new ConnectCommand(leConnector);
        disconnectDevice = new DisconnectCommand(leConnector);

        exchangeDataOn = new DataExchangeOnCommand(leDataTransmitter);
        exchangeDataOff = new DataExchangeOffCommand(leDataTransmitter);
        receiveDataOn = new ReceiveDataOn(leDataTransmitter);

        closeService = new CloseServiceCommand(mBluetoothLeService);
        startService = new StartServiceCommand(iService, serviceIntent);

        bindService = new BindServiceCommand(iService, mServiceConnection);
        unbindService = new UnbindServiceCommand(mServiceConnection, iService);

        registerReceiver = new RegisterReceiverCommand(iReceive, this);
        unregisterReceiver = new UnregisterReceiverCommand(iReceive, this);

        buttonViewColorChangeOn = new ButtonChangeViewOnCommand(mIBluetoothListener);
        buttonViewColorChangeOff = new ButtonChangeViewOffCommand(mIBluetoothListener);

        // привязываем сервис и регистрируем BroadcastReceiver
        bleController.setCommand(bindService)
                     .setCommand(registerReceiver)
                     .execute();
    }

    //--------------------- getters and setters

     ConnectorBluetooth setmHandler(Handler mHandler) {
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

     ConnectorBluetooth addIRxDataListener(ITransmitter iTransmitter) {
        this.iTransmitter = iTransmitter;
       return this;
    }

    public boolean ismScanning() {
        return leScanner.isScanning();
    }

    private void setmBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
        leConnector.setBTDevice(mBTDevice);
    }

    public BroadcastReceiver getBroadcastReceiver(){
        return  leBroadcastReceiver.getGattUpdateReceiver();
    }

    public LeDeviceListAdapter getmLeDeviceListAdapter() {
        return controlAdapter.getLeDeviceListAdapter();
    }

     ConnectorBluetooth setiBluetoothListener(IBluetoothListener mIBluetoothListener) {
        this.mIBluetoothListener = mIBluetoothListener;
        return this;
    }

     ConnectorBluetooth setStorageFromBt(StorageData<byte[]> storageFromBt){
        this.storageFromBt = storageFromBt;
        return this;
    }

     ConnectorBluetooth setStorageToBt(StorageData<byte[][]> storageToBt){
        this.storageToBt = storageToBt;
        return this;
    }

     ConnectorBluetooth setiReceive(IReceive iReceive) {
        this.iReceive = iReceive;
        return this;
    }

     ConnectorBluetooth setiService(IService iService) {
        this.iService = iService;
        return this;
    }

     ConnectorBluetooth setiVisible(IVisible iVisible) {
        this.iVisible = iVisible;
        return this;
    }

     ConnectorBluetooth setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
        return this;
    }

    //---------------------------------Bluetooth -----------------------------
    public boolean getBluetoothAdapter(BluetoothManager bluetoothManager) {
        if (Settings.debug) Log.i(TAG, "getBluetoothAdapter()");
        mBluetoothAdapter = bluetoothManager.getAdapter();
        return !(mBluetoothAdapter == null);
    }

    public BluetoothAdapter getBTAdapter(){
        return mBluetoothAdapter;
    }

    //------------------ inittialization List-------------------------

    public void initListBTDevice() {
        bleController.setCommand(initList).execute();
    }

    //------------------ inittialization List-------------------------

 public void clickItemList(int position,  AlertDialog.Builder adb){
        final BluetoothDevice device = getmLeDeviceListAdapter().getDevice(position);
        if (device == null) return;
        setmBTDevice(device);
        // инициализация комманд работающих с устройством
        //----------- Команды вызова диалоговых окон --------------
        discDialogOn = new DisconnectDialogCommand(mBTDevice, this, iMsgToUi);
        disconnDialogInfoOn = new DisconnInfoDialogCommand(mBTDevice, iMsgToUi, iVisible);
        reconnDiaologOn = new ReconnectDialogCommand(mBTDevice, this, iMsgToUi);
        connDialogInfoOn = new ConnInfoDialogCommand(mBTDevice, iMsgToUi, iVisible);
        connDialogOn = new ConnectDialogCommand(mBTDevice, this, iMsgToUi);
        //------------------ Команды работы с адаптером -----------
        addConnDeviceToAdapter = new AddConnectDeviceToAdapterCommand(controlAdapter, mBTDevice);
        clrConnDeviceFromAdapter = new ClearConnectDeviceFromAdapterCommand(controlAdapter, mBTDevice);
        //------------------- Отображение характеристик устройства -----------------------
        characteristicDisplayOn = new CharacteristicsDisplayOnCommand(characteristics, mBluetoothLeService);

        if (Settings.debug) Log.i(TAG, "mBTDevice = " + device);
        if (Settings.debug) Log.i(TAG, "mBTDeviceConn = " + mBTDeviceConn);
        if (mBTDevice.equals(mBTDeviceConn)) {
            bleController.setCommand(discDialogOn).execute();
        } else if (mBTDeviceConn != null) {
            bleController.setCommand(reconnDiaologOn).execute();
        } else {
            bleController.setCommand(connDialogOn)
                         .setCommand(connectDevice)
                         .execute();
        }
    }

    //--------------------------- Callbacks from BroadcastReceiver --------------------
    @Override
    public void actionConnected() {
        setBLEState(getBLEState(), BluetoothLeState.CONNECTED);
        if (Settings.debug) Log.i(TAG, "mBTDevice = " + mBTDevice);
        mBTDeviceConn = mBTDevice;

        bleController.setCommand(connDialogOn).undo();

        bleController.setCommand(connDialogInfoOn).execute();
        bleController.setCommand(connDialogInfoOn).undo();

        bleController.setCommand(buttonViewColorChangeOn)
                     .setCommand(addConnDeviceToAdapter)
                     .execute();

        setStorages();
    }

    @Override
    public void actionDisconnected() {

        if (BLEState != BluetoothLeState.DISCONECTED) {
            mBTDeviceConn = null;

            bleController.setCommand(connDialogOn).undo();

            if (BLEState == BluetoothLeState.TRANSMIT_DATA)
                bleController.setCommand(exchangeDataOff).execute();

            bleController.setCommand(disconnDialogInfoOn).execute();
            bleController.setCommand(disconnDialogInfoOn).undo();
            bleController.setCommand(buttonViewColorChangeOff)
                    .setCommand(clrConnDeviceFromAdapter)
                    .setCommand(clearList)
                    .setCommand(scanOn)
                    .execute();

            setBLEState(BLEState, BluetoothLeState.DISCONECTED);
        }
    }

    @Override
    public void actionServiceDiscovered() {
        setBLEState(getBLEState(), BluetoothLeState.SERVICES_DISCOVERED);
        bleController.setCommand(characteristicDisplayOn).execute();
    }
  //----------------------- Scanning ---------------------------

    public void startScanBTDevices(){
         bleController.setCommand(scanOn).execute();
    }

    public void stopScanBTDevice(){
         bleController.setCommand(scanOff).execute();
    }

    public void scanWork() {
        bleController.setCommand(clearList)
                     .setCommand(addToList)
                     .setCommand(scanOn)
                     .execute();
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

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            if (Settings.debug) Log.i(TAG, "onServiceConnected()");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                if (Settings.debug) Log.e(TAG, "Unable to initialize Bluetooth");
                mIBluetoothListener.finishConnection();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (mBluetoothLeService != null && leBroadcastReceiver != null && leConnector != null && leDataTransmitter != null) {
                if (mBTDevice != null)
                    mBluetoothLeService.connect(mBTDevice.getAddress());
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

    @Override
    public void baseStart(IBaseAdder iBaseAdder) {
        if (Settings.debug) Log.i(TAG, "baseStart");
        if (iBaseAdder == null) {
            if (Settings.debug) Log.e(TAG, "baseStart illegal parameters");
            return;
        } else {
            iBaseAdder.addBase(this);
        }
        build();
    }

    @Override
    public void baseStop(){
        if (Settings.debug) Log.i(TAG, "baseStop");

        bleController.setCommand(exchangeDataOff)
                     .setCommand(unregisterReceiver)
                     .setCommand(unbindService)
                     .setCommand(closeService)
                     .execute();

    }

    //---------------------------- blecontroller states ------------------------------

    private synchronized BluetoothLeState getBLEState() {
        if (Settings.debug) Log.i(TAG, "getBLEState is " + BLEState.getName());
        return BLEState;
    }

    private synchronized boolean setBLEState(BluetoothLeState fromBLEState, BluetoothLeState toBLEState) {
        if (Settings.debug) Log.w(TAG, String.format("setState from %s to %s", fromBLEState.getName(), toBLEState.getName()));
        if (BLEState == fromBLEState) {
            if (fromBLEState.availableStates().contains(toBLEState)) {
                BLEState = toBLEState;
                if (BLEState == BluetoothLeState.DISCONECTED) {
                    BLEState = BluetoothLeState.DISCONECTED;
                }
                return true;
            } else {
                if (Settings.debug) Log.e(TAG, String.format("setState: %s is not available from %s", toBLEState.getName(), fromBLEState.getName()));
            }
        } else {
            if (Settings.debug) Log.e(TAG, String.format("setState: current is not %s", fromBLEState.getName()));
        }
        return false;
    }


    //------------ устанавливаем хранилища для данных ---------------

    @Override
    public void setStorages(){
        if (Settings.debug) Log.i(TAG, "setStorages()");
        leDataTransmitter.setStorageFromBt((storageFromBt));
        leDataTransmitter.setStorageToBt(storageToBt);
        mBluetoothLeService.setCallbackWriteListener(leDataTransmitter);
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
        switch (Settings.opMode) {
            case AudIn2Bt:
                if (!isDebugRunning) {
                    isDebugRunning = true;
                    enableTransmitData();
                }
            case Bt2Bt:
                if (getBLEState() == BluetoothLeState.SERVICES_DISCOVERED) {
                    enableTransmitData();
                }
            case Record:
                if (currentState == CallerState.DebugRecord) {
                    enableTransmitData();
                }
                break;
            case Bt2AudOut:
                if (!isDebugRunning) {
                    isDebugRunning = true;
                    onlyReceiveData();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void stopDebug() {
        if (Settings.debug) Log.i(TAG, "stopDebug");
        switch (Settings.opMode) {
            case Record:
                disableTransmitData();
                break;
            default:
                break;
        }
    }

    private String getCallerStateName() {
        return Caller.getInstance().getCallerState().getName();
    }

    private CallerState getCallerState() {
        return Caller.getInstance().getCallerState();
    }

}