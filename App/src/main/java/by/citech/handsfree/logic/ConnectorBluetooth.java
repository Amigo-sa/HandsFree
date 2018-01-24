package by.citech.handsfree.logic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import by.citech.handsfree.application.ThisApplication;
import by.citech.handsfree.bluetoothlegatt.BluetoothLeCore;
import by.citech.handsfree.bluetoothlegatt.IBtList;
import by.citech.handsfree.connection.fsm.EConnectionReport;
import by.citech.handsfree.connection.fsm.EConnectionState;
import by.citech.handsfree.connection.fsm.IConnectionFsmListener;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.common.IService;
import by.citech.handsfree.bluetoothlegatt.ConnectAction;
import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.bluetoothlegatt.BluetoothLeState;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.ui.IBtToUiCtrl;
import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.adapters.LeDeviceListAdapter;
import by.citech.handsfree.bluetoothlegatt.LeBroadcastReceiver;
import by.citech.handsfree.bluetoothlegatt.LeScanner;
import by.citech.handsfree.bluetoothlegatt.commands.adapter.AddConnectDeviceToAdapterCommand;
import by.citech.handsfree.bluetoothlegatt.commands.adapter.AddToListCommand;
import by.citech.handsfree.bluetoothlegatt.commands.BLEController;
import by.citech.handsfree.bluetoothlegatt.commands.characteristics.CharacteristicsDisplayOnCommand;
import by.citech.handsfree.bluetoothlegatt.commands.receiver.RegisterReceiverCommand;
import by.citech.handsfree.bluetoothlegatt.commands.receiver.UnregisterReceiverCommand;
import by.citech.handsfree.bluetoothlegatt.commands.button.ButtonChangeViewOffCommand;
import by.citech.handsfree.bluetoothlegatt.commands.button.ButtonChangeViewOnCommand;
import by.citech.handsfree.bluetoothlegatt.commands.adapter.ClearConnectDeviceFromAdapterCommand;
import by.citech.handsfree.bluetoothlegatt.commands.adapter.ClearListCommand;
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
import by.citech.handsfree.bluetoothlegatt.rwdata.Characteristics;
import by.citech.handsfree.bluetoothlegatt.rwdata.LeDataTransmitter;
import by.citech.handsfree.bluetoothlegatt.StorageListener;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.ui.IBtToUiListener;
import by.citech.handsfree.ui.IUiToBtListener;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.logic.ECallReport.CallFailedInt;
import static by.citech.handsfree.logic.ECallReport.StopDebug;
import static by.citech.handsfree.logic.ECallReport.SysIntConnectedIncompatible;
import static by.citech.handsfree.logic.ECallReport.SysIntError;
import static by.citech.handsfree.logic.ECallReport.SysIntReady;

public class ConnectorBluetooth
        implements StorageListener, ConnectAction, ICallerFsmListener,
        ICallerFsm, ICallerFsmRegisterListener, IConnectionFsmListener {

    private final static String STAG = Tags.ConnectorBluetooth;

    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    // обьявляем сервис для обработки соединения и передачи данных (клиент - сервер)
    private BluetoothLeCore mBluetoothLeService;
    private Handler mHandler;
    // BLE устройство, с которым будем соединяться
    private BluetoothDevice mBTDevice;
    private BluetoothDevice mBTDeviceConn;
    // сканер имеющихся по близости BLE устройств
    private LeScanner leScanner;
    // хранилища данных
    private volatile StorageData<byte[]> storageFromBt;
    private volatile StorageData<byte[][]> storageToBt;
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
    private IRxComplex iRxComplex;
    //для дебага
    private long start_time;

    private volatile BluetoothLeState BLEState;

    private IBroadcastReceiver iBroadcastReceiver;
    private IBtToUiCtrl iBtToUiCtrl;
    private IMsgToUi iMsgToUi;
    private IBtList iBtList;

    private BLEController bleController;

    // команды
    private Command scanOn;
    private Command scanOff;

    private AddToListCommand addToList;
    private Command clearList;
    private InitListCommand initList;

    private ConnectCommand connectDevice;
    private DisconnectCommand disconnectDevice;

    private Command exchangeDataOn;
    private Command exchangeDataOff;
    private Command receiveDataOn;

//  private CloseServiceCommand closeService;
//  private Command startService;
//  private BindServiceCommand bindService;
//  private UnbindServiceCommand unbindService;

    private ConnectDialogCommand connDialogOn;
    private DisconnectDialogCommand discDialogOn;
    private ReconnectDialogCommand reconnDiaologOn;
    private ConnInfoDialogCommand connDialogInfoOn;
    private DisconnInfoDialogCommand disconnDialogInfoOn;

    private ButtonChangeViewOnCommand buttonViewColorChangeOn;
    private ButtonChangeViewOffCommand buttonViewColorChangeOff;

    private AddConnectDeviceToAdapterCommand addConnDeviceToAdapter;
    private ClearConnectDeviceFromAdapterCommand clrConnDeviceFromAdapter;

    private RegisterReceiverCommand registerReceiver;
    private UnregisterReceiverCommand unregisterReceiver;

    private CharacteristicsDisplayOnCommand characteristicDisplayOn;
    //--------------------- singleton

    private static volatile ConnectorBluetooth instance = null;

    private ConnectorBluetooth() {
        BLEState = BluetoothLeState.DISCONECTED;
        leBroadcastReceiver = new LeBroadcastReceiver(this);
        controlAdapter = new ControlAdapter(this);
        leScanner = new LeScanner(controlAdapter);

        characteristics = new Characteristics();
        leDataTransmitter = new LeDataTransmitter(characteristics);
        // инициализация контроллера команд
        bleController = new BLEController();

        exchangeDataOn = new DataExchangeOnCommand(leDataTransmitter);
        exchangeDataOff = new DataExchangeOffCommand(leDataTransmitter);
        receiveDataOn = new ReceiveDataOn(leDataTransmitter);

//      closeService = new CloseServiceCommand();
//      startService = new StartServiceCommand(iService, serviceIntent);
//      bindService = new BindServiceCommand();
//      unbindService = new UnbindServiceCommand();

        registerReceiver = new RegisterReceiverCommand(this);
        unregisterReceiver = new UnregisterReceiverCommand(this);

        buttonViewColorChangeOn = new ButtonChangeViewOnCommand();
        buttonViewColorChangeOff = new ButtonChangeViewOffCommand();

        discDialogOn = new DisconnectDialogCommand(this);
        reconnDiaologOn = new ReconnectDialogCommand(this);
        connDialogOn = new ConnectDialogCommand(this);
        disconnDialogInfoOn = new DisconnInfoDialogCommand();
        connDialogInfoOn = new ConnInfoDialogCommand();

        addToList = new AddToListCommand(controlAdapter);
        clearList = new ClearListCommand(controlAdapter);
        initList = new InitListCommand(controlAdapter);

        addConnDeviceToAdapter = new AddConnectDeviceToAdapterCommand(controlAdapter);
        clrConnDeviceFromAdapter = new ClearConnectDeviceFromAdapterCommand(controlAdapter);
        // инициализация комманд
        scanOn = new ScanOnCommand(leScanner);
        scanOff = new ScanOffCommand(leScanner);

        connectDevice = new ConnectCommand();
        disconnectDevice = new DisconnectCommand();

        characteristicDisplayOn = new CharacteristicsDisplayOnCommand(characteristics);
        mBluetoothLeService = BluetoothLeCore.getInstance();
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

    private void build() {
        if (Settings.debug) Log.i(TAG, "build");
        leScanner.setHandler(mHandler);
        leScanner.setIBluetoothListener(mIBluetoothListener);
        characteristics.setIBluetoothListener(mIBluetoothListener);
        leDataTransmitter.setIBluetoothListener(mIBluetoothListener);
        leDataTransmitter.addIRxDataListener(iRxComplex);

        //-----------------set data for command -------------

//      closeService.setBluetoothLeService(mBluetoothLeService);
//      bindService.setiService(iService);
//      bindService.setServiceConnection(mServiceConnection);
//      unbindService.setiService(iService);
//      unbindService.setServiceConnection(mServiceConnection);
        registerReceiver.setiBroadcastReceiver(iBroadcastReceiver);
        unregisterReceiver.setiBroadcastReceiver(iBroadcastReceiver);

        buttonViewColorChangeOn.setiBluetoothListener(mIBluetoothListener);
        buttonViewColorChangeOff.setBluetoothListener(mIBluetoothListener);
        connDialogOn.setiMsgToUi(iMsgToUi);
        discDialogOn.setiMsgToUi(iMsgToUi);
        reconnDiaologOn.setiMsgToUi(iMsgToUi);
        disconnDialogInfoOn.setiBtToUiCtrl(iBtToUiCtrl);
        disconnDialogInfoOn.setiMsgToUi(iMsgToUi);
        connDialogInfoOn.setiBtToUiCtrl(iBtToUiCtrl);
        connDialogInfoOn.setiMsgToUi(iMsgToUi);
    }

    //--------------------- IBase

    public void onStop() {
        if (Settings.debug) Log.i(TAG, "onStop");
        unregisterCallerFsmListener(this, TAG);

        if (getBLEState() == BluetoothLeState.TRANSMIT_DATA)
            bleController.setCommand(exchangeDataOff).execute();

        iBtList = null;
        mBTDevice = null;
        mBTDeviceConn = null;
        initList.setDevice(null);
        addToList.setDevice(null);
    }

    //--------------------- getters and setters

    ConnectorBluetooth setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
        return this;
    }

    ConnectorBluetooth addIRxDataListener(IRxComplex iRxComplex) {
        this.iRxComplex = iRxComplex;
        return this;
    }

    private void setmBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
    }

    public BroadcastReceiver getBroadcastReceiver() {
        return leBroadcastReceiver.getGattUpdateReceiver();
    }

    public IUiToBtListener getUiBtListener() {
        return BluetoothUi.getInstance();
    }

    public IBtToUiListener getIbtToUiListener() {
        return leScanner;
    }

    ConnectorBluetooth setiBluetoothListener(IBluetoothListener mIBluetoothListener) {
        this.mIBluetoothListener = mIBluetoothListener;
        return this;
    }

    ConnectorBluetooth setStorageFromBt(StorageData<byte[]> storageFromBt) {
        this.storageFromBt = storageFromBt;
        return this;
    }

    ConnectorBluetooth setStorageToBt(StorageData<byte[][]> storageToBt) {
        this.storageToBt = storageToBt;
        return this;
    }

    ConnectorBluetooth setiBroadcastReceiver(IBroadcastReceiver iBroadcastReceiver) {
        this.iBroadcastReceiver = iBroadcastReceiver;
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

    ConnectorBluetooth setiBtList(IBtList iBtList) {
        //BluetoothUi.getInstance().setiBtList(iBtList);
        this.iBtList = iBtList;
        return this;
    }

    public IBtList getiBtList() {
        return iBtList;
    }

    //------------------------ init command when device is chosen -------

    private void initCommandForDevice(BluetoothDevice device) {
        // инициализация комманд работающих с устройством
        //----------- установка устройства для команд --------------
        if (Settings.debug) Log.i(TAG, "initCommandForDevice mBTDevice = " + device);
        connDialogOn.setDevice(device);
        discDialogOn.setDevice(device);
        reconnDiaologOn.setDevice(device);
        disconnDialogInfoOn.setDevice(device);
        connDialogInfoOn.setDevice(device);
        //------------------ Команда соединения -----------

        connectDevice.setmBluetoothLeService(mBluetoothLeService);
        disconnectDevice.setmBluetoothLeService(mBluetoothLeService);
        connectDevice.setmBTDevice(device);
        //------------------ Команды работы с адаптером -----------
        initList.setDevice(device);
        //clrConnDeviceFromAdapter.setBluetoothDevice(mBTDevice);
        //-------------------Команды для определения характеристик --------
        characteristicDisplayOn.setBluetoothLeService(mBluetoothLeService);
    }

    //------------------ inittialization List-------------------------

    void initListBTDevice() {
        bleController.setCommand(initList).execute();
    }

    //------------------ inittialization List-------------------------

    void clickItemList(int position) {
        final BluetoothDevice device = ((LeDeviceListAdapter) iBtList).getDevice(position);
        if (device == null) return;
        setmBTDevice(device);
        initCommandForDevice(device);

        bleController.setCommand(scanOff).execute();
        if (Settings.debug) Log.i(TAG, "mBTDevice = " + device);
        if (Settings.debug) Log.i(TAG, "mBTDeviceConn = " + mBTDeviceConn);
        if (device.equals(mBTDeviceConn)) {
            bleController.setCommand(discDialogOn).execute();
        } else if (mBTDeviceConn != null) {
            bleController.setCommand(reconnDiaologOn).execute();
        } else {
            start_time = System.currentTimeMillis();
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
        addToList.setDevice(mBTDeviceConn);

        long end_time = System.currentTimeMillis();
        if (Settings.debug) Log.i(TAG, "Connecting await time = " + (end_time - start_time));

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
        bleController.setCommand(connDialogOn).undo();
        bleController.setCommand(disconnDialogInfoOn).execute();
        bleController.setCommand(disconnDialogInfoOn).undo();
        addToList.setDevice(null);

        if (BLEState != BluetoothLeState.DISCONECTED) {
            processState();
            mBTDeviceConn = null;

            if (BLEState == BluetoothLeState.TRANSMIT_DATA) {
                bleController.setCommand(exchangeDataOff).execute();

            }

            bleController.setCommand(clrConnDeviceFromAdapter)
                    .setCommand(buttonViewColorChangeOff)
                    .setCommand(clearList)
                    .setCommand(scanOn)
                    .execute();

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
        ECallerState callerState = getCallerFsmState();
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

    void startScanBTDevices() {
        bleController.setCommand(scanOn).execute();
    }

    void stopScanBTDevice() {
        bleController.setCommand(scanOff).execute();
    }

    void scanWork() {
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

//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            if (Settings.debug) Log.i(TAG, "onServiceConnected()");
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
//                if (Settings.debug) Log.e(TAG, "Unable to initialize Bluetooth");
//                mIBluetoothListener.finishConnection();
//            }
////          Automatically connects to the device upon successful start-up initialization.
////            if (mBluetoothLeService != null && leBroadcastReceiver != null) {
////                if (mBTDevice != null) {
////                    bleController.setCommand(connectDevice).execute();
////                }
////            }
//            if (leDataTransmitter != null)
//                leDataTransmitter.setBluetoothLeService(mBluetoothLeService);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            if (Settings.debug) Log.i(TAG, "onServiceDisconnected()");
//            mBluetoothLeService = null;
//        }
//    };

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

    //--------------------- ICallerFsmListener

    @Override
    public void onCallerStateChange(ECallerState from, ECallerState to, ECallReport why) {
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
                        if (getCallerFsmState() == ECallerState.DebugRecord) {
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

    }
}