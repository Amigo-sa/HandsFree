package by.citech.handsfree.logic;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IBaseAdder;
import by.citech.handsfree.common.IService;
import by.citech.handsfree.bluetoothlegatt.ConnectAction;
import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.bluetoothlegatt.BluetoothLeState;
import by.citech.handsfree.gui.IBtToUiCtrl;
import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.adapters.LeDeviceListAdapter;
import by.citech.handsfree.bluetoothlegatt.BluetoothLeService;
import by.citech.handsfree.bluetoothlegatt.LeBroadcastReceiver;
import by.citech.handsfree.bluetoothlegatt.LeScanner;
import by.citech.handsfree.bluetoothlegatt.commands.adapter.AddConnectDeviceToAdapterCommand;
import by.citech.handsfree.bluetoothlegatt.commands.adapter.AddToListCommand;
import by.citech.handsfree.bluetoothlegatt.commands.BLEController;
import by.citech.handsfree.bluetoothlegatt.commands.characteristics.CharacteristicsDisplayOnCommand;
import by.citech.handsfree.bluetoothlegatt.commands.receiver.RegisterReceiverCommand;
import by.citech.handsfree.bluetoothlegatt.commands.receiver.UnregisterReceiverCommand;
import by.citech.handsfree.bluetoothlegatt.commands.service.BindServiceCommand;
import by.citech.handsfree.bluetoothlegatt.commands.button.ButtonChangeViewOffCommand;
import by.citech.handsfree.bluetoothlegatt.commands.button.ButtonChangeViewOnCommand;
import by.citech.handsfree.bluetoothlegatt.commands.adapter.ClearConnectDeviceFromAdapterCommand;
import by.citech.handsfree.bluetoothlegatt.commands.adapter.ClearListCommand;
import by.citech.handsfree.bluetoothlegatt.commands.service.CloseServiceCommand;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.bluetoothlegatt.commands.dialogs.ConnInfoDialogCommand;
import by.citech.handsfree.bluetoothlegatt.commands.connect.ConnectCommand;
import by.citech.handsfree.bluetoothlegatt.commands.dialogs.ConnectDialogCommand;
import by.citech.handsfree.bluetoothlegatt.commands.dataexchange.DataExchangeOffCommand;
import by.citech.handsfree.bluetoothlegatt.commands.dataexchange.DataExchangeOnCommand;
import by.citech.handsfree.bluetoothlegatt.commands.dialogs.DisconnInfoDialogCommand;
import by.citech.handsfree.bluetoothlegatt.commands.connect.DisconnectCommand;
import by.citech.handsfree.bluetoothlegatt.commands.dialogs.DisconnectDialogCommand;
import by.citech.handsfree.bluetoothlegatt.commands.adapter.InitListCommand;
import by.citech.handsfree.bluetoothlegatt.commands.dataexchange.ReceiveDataOn;
import by.citech.handsfree.bluetoothlegatt.commands.dialogs.ReconnectDialogCommand;
import by.citech.handsfree.bluetoothlegatt.commands.scanner.ScanOffCommand;
import by.citech.handsfree.bluetoothlegatt.commands.scanner.ScanOnCommand;
import by.citech.handsfree.bluetoothlegatt.commands.service.StartServiceCommand;
import by.citech.handsfree.bluetoothlegatt.commands.service.UnbindServiceCommand;
import by.citech.handsfree.bluetoothlegatt.rwdata.Characteristics;
import by.citech.handsfree.bluetoothlegatt.rwdata.LeDataTransmitter;
import by.citech.handsfree.bluetoothlegatt.StorageListener;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.debug.IDebugListener;
import by.citech.handsfree.exchange.IMsgToUi;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.gui.ICallToUiExchangeListener;
import by.citech.handsfree.gui.IBtToUiListener;
import by.citech.handsfree.gui.IUiToBtListener;
import by.citech.handsfree.settings.Settings;

public class ConnectorBluetooth
        implements ICallNetExchangeListener, ICallToUiExchangeListener, IDebugListener, StorageListener, ConnectAction , IBase {

    private final static String TAG = "WSD_ConnectorBluetooth";

    private AlertDialog alertDialog;
    // обьявляем сервис для обработки соединения и передачи данных (клиент - сервер)
    private BluetoothLeService mBluetoothLeService;
    private Handler mHandler;
    // BLE устройство, с которым будем соединяться
    private BluetoothDevice mBTDevice;
    private BluetoothDevice mBTDeviceConn;
    // сканер имеющихся по близости BLE устройств
    private LeScanner leScanner;
    // хранилища данных
    private StorageData<byte[]> storageFromBt;
    private StorageData<byte[][]> storageToBt;
    // адаптер найденных устройств и управление для него
    private ControlAdapter controlAdapter;
    // список сервисов и характеристик устройства
    private Characteristics characteristics;
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

    private IBroadcastReceiver iBroadcastReceiver;
    private IService iService;
    private IBtToUiCtrl iBtToUiCtrl;
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
                mIBluetoothListener,
                controlAdapter);
        leBroadcastReceiver = new LeBroadcastReceiver(this);
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

        exchangeDataOn = new DataExchangeOnCommand(leDataTransmitter);
        exchangeDataOff = new DataExchangeOffCommand(leDataTransmitter);
        receiveDataOn = new ReceiveDataOn(leDataTransmitter);

        closeService = new CloseServiceCommand(mBluetoothLeService);
        startService = new StartServiceCommand(iService, serviceIntent);

        bindService = new BindServiceCommand(iService, mServiceConnection);
        unbindService = new UnbindServiceCommand(mServiceConnection, iService);

        registerReceiver = new RegisterReceiverCommand(iBroadcastReceiver, this);
        unregisterReceiver = new UnregisterReceiverCommand(iBroadcastReceiver, this);

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
    }

    public BroadcastReceiver getBroadcastReceiver(){
        return  leBroadcastReceiver.getGattUpdateReceiver();
    }

    public LeDeviceListAdapter getmLeDeviceListAdapter() {
        return controlAdapter.getLeDeviceListAdapter();
    }

    public IUiToBtListener getUiBtListener() {
        return BluetoothUi.getInstance();
    }

    public IBtToUiListener getIbtToUiListener(){
        return leScanner;
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

     ConnectorBluetooth setiBroadcastReceiver(IBroadcastReceiver iBroadcastReceiver) {
        this.iBroadcastReceiver = iBroadcastReceiver;
        return this;
    }

     ConnectorBluetooth setiService(IService iService) {
        this.iService = iService;
        return this;
    }

     ConnectorBluetooth setiBtToUiCtrl(IBtToUiCtrl iBtToUiCtrl) {
        this.iBtToUiCtrl = iBtToUiCtrl;
        return this;
    }

     ConnectorBluetooth setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
        return this;
    }

    //------------------ inittialization List-------------------------

    public void initListBTDevice() {
        bleController.setCommand(initList).execute();
    }

    //------------------ inittialization List-------------------------

 public void clickItemList(int position){
        final BluetoothDevice device = getmLeDeviceListAdapter().getDevice(position);
        if (device == null) return;
        setmBTDevice(device);
        // инициализация комманд работающих с устройством
        //----------- Команды вызова диалоговых окон --------------
        discDialogOn = new DisconnectDialogCommand(mBTDevice, this, iMsgToUi);
        disconnDialogInfoOn = new DisconnInfoDialogCommand(mBTDevice, iMsgToUi, iBtToUiCtrl);
        reconnDiaologOn = new ReconnectDialogCommand(mBTDevice, this, iMsgToUi);
        connDialogInfoOn = new ConnInfoDialogCommand(mBTDevice, iMsgToUi, iBtToUiCtrl);
        connDialogOn = new ConnectDialogCommand(mBTDevice, this, iMsgToUi);
        //------------------ Команды соединения/разьединения -----------
        connectDevice = new ConnectCommand(mBTDevice, mBluetoothLeService);
        disconnectDevice = new DisconnectCommand(mBluetoothLeService);
        //------------------ Команды работы с адаптером -----------
        addConnDeviceToAdapter = new AddConnectDeviceToAdapterCommand(controlAdapter, mBTDevice);
        clrConnDeviceFromAdapter = new ClearConnectDeviceFromAdapterCommand(controlAdapter, mBTDevice);
        //------------------- Отображение характеристик устройства -----------------------
        characteristicDisplayOn = new CharacteristicsDisplayOnCommand(characteristics, mBluetoothLeService);

        bleController.setCommand(scanOff).execute();
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
        if (Settings.debug) Log.i(TAG, "actionDisconnected()");
        if (BLEState != BluetoothLeState.DISCONECTED) {
            mBTDeviceConn = null;

            bleController.setCommand(connDialogOn).undo();

            if (BLEState == BluetoothLeState.TRANSMIT_DATA)
                bleController.setCommand(exchangeDataOff).execute();

            bleController.setCommand(disconnDialogInfoOn).execute();
            bleController.setCommand(disconnDialogInfoOn).undo();
            bleController.setCommand(clrConnDeviceFromAdapter)
                    .setCommand(buttonViewColorChangeOff)
                    .setCommand(clearList)
                    .setCommand(scanOn)
                    .execute();
        }
        setBLEState(BLEState, BluetoothLeState.DISCONECTED);
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
            if (mBluetoothLeService != null && leBroadcastReceiver != null && leDataTransmitter != null) {
                if (mBTDevice != null) {
                    addConnDeviceToAdapter = new AddConnectDeviceToAdapterCommand(controlAdapter, mBTDevice);
                    bleController.setCommand(connectDevice).
                            setCommand(addConnDeviceToAdapter).execute();
//                    mBluetoothLeService.connect(mBTDevice.getAddress());
//
                }
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

        bleController = null;
        characteristics = null;
        controlAdapter = null;
        leScanner = null;
        leDataTransmitter = null;
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