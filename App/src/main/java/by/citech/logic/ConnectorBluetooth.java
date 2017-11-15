package by.citech.logic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.bluetoothlegatt.LeDeviceListAdapter;
import by.citech.data.SampleGattAttributes;
import by.citech.data.StorageData;
import by.citech.param.Settings;

public class ConnectorBluetooth implements ICallNetworkExchangeListener, ICallUiExchangeListener{

    private final static String TAG = "WSD_ConnectorBluetooth";
    private static final long SCAN_PERIOD = 10000;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // обьявляем сервис для обработки соединения и передачи данных (клиент - сервер)
    private BluetoothLeService mBluetoothLeService;
    private Handler mHandler;
    //выводим на дисплей принимаемые данные
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothDevice mBTDevice;
    private BluetoothDevice mBTDeviceConn;
    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    // хранилища данных
    private StorageData storageBtToNet;
    private StorageData storageNetToBt;
    // список характеристик устройства
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    // обьявляем характеристику для включения нотификации на периферийном устройстве(сервере)
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private IBluetoothListener mIBluetoothListener;

    //--------------------- singleton
    private static volatile ConnectorBluetooth instance = null;

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

    //--------------------- getters and setters
    public ConnectorBluetooth setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
        return this;
    }

    public boolean ismConnected() {
        return mConnected;
    }

    public boolean ismScanning() {
        return mScanning;
    }

    public void setmBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
    }

    public BluetoothDevice getmBTDevice() {
        return mBTDevice;
    }

    public BluetoothDevice getmBTDeviceConn() {
        return mBTDeviceConn;
    }

    public LeDeviceListAdapter getmLeDeviceListAdapter() {
        return mLeDeviceListAdapter;
    }

    public ConnectorBluetooth setiBluetoothListener(IBluetoothListener mIBluetoothListener) {
        this.mIBluetoothListener = mIBluetoothListener;
        return this;
    }

    public ConnectorBluetooth setStorageBtToNet(StorageData storageBtToNet){
        this.storageBtToNet = storageBtToNet;
        return this;
    }

    public ConnectorBluetooth setStorageNetToBt(StorageData storageNetToBt){
        this.storageNetToBt = storageNetToBt;
        return this;
    }

    //--------------------------Connection methods-----------------------------------

    public void disconnectBTDevice(){
        // производим отключение от устройства
        if (Settings.debug)Log.i(TAG,"disconnectBTDevice()");
        mBluetoothLeService.disconnect();
    }

    public void onConnectBTDevice() {
        if (Settings.debug) Log.i(TAG,"onConnectBTDevice()");
        // получаем данные от присоединяемого устройсва
        mDeviceName = mBTDevice.getName();
        mDeviceAddress = mBTDevice.getAddress();
        // останавливаем сканирование
        stopScanBluetoothDevice();
        // если сервис привязан производим соединение
        if (Settings.debug) Log.i(TAG,"mBluetoothLeService = " + mBluetoothLeService);
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(mDeviceAddress);
        // ответ ждём в Callback(BroadcastReceiver)
    }

    //-----------------------------Scanning-------------------------------------

    public void startScanBluetoothDevice(){
        scanLeDevice(true);
    }

    public void stopScanBluetoothDevice(){
        scanLeDevice(false);
    }

    // процедура сканирования устройства
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if (Settings.debug) Log.i(TAG, "start scanLeDevice()");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mIBluetoothListener.changeOptionMenu();

                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            if (Settings.debug) Log.i(TAG, "stop scanLeDevice()");
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        mIBluetoothListener.changeOptionMenu();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    if (Settings.debug) Log.i(TAG, "onLeScan()");
                    mIBluetoothListener.addDeviceToList(mLeDeviceListAdapter, device, rssi);
                }
            };


    public void closeLeService(){
        if (Settings.debug) Log.i(TAG, "closeLeService()");
        if(mBluetoothLeService.getWriteThread() != null)
            mBluetoothLeService.stopWriteThread();
        mBluetoothLeService = null;
    }

    //-----------------------------List BT Devices ------------------------

    public void initializeListBluetoothDevice() {
        if (Settings.debug) Log.i(TAG, "initializeListBluetoothDevice()");
        if (mLeDeviceListAdapter == null) {
            mLeDeviceListAdapter = mIBluetoothListener.addLeDeviceListAdapter();
        } else
            clearAllDevicesFromList();
        if (mConnected)
            addConnectDeviceToList();
    }

    public void addConnectDeviceToList(){
        if (mConnected) {
            if (Settings.debug) Log.i(TAG, "ADD DEVICE TO LIST " + mBTDevice + "\n");
            if (Settings.debug) Log.i(TAG, "mLeDeviceListAdapter = " + mLeDeviceListAdapter + "\n");
            if ((mBTDevice != null) && mLeDeviceListAdapter != null) {
                mLeDeviceListAdapter.addDevice(mBTDevice, 200);
                // чтобы не сыпался в исклюение adapter
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void clearAllDevicesFromList(){
        if (Settings.debug) Log.e(TAG, "clearAllDevicesFromList()");
        if (mLeDeviceListAdapter != null){
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
        }
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

    public final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                if (Settings.debug) Log.e(TAG, "Unable to initialize Bluetooth");
                mIBluetoothListener.finishConnection();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (Settings.debug) Log.i(TAG, "onServiceDisconnected()");
            mBluetoothLeService = null;
        }
    };

    //---------------------------------Broadcast Receiver -----------------------------

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    // обьявляем обработчик(слушатель) соединения, для отображения состояния соединения на дисплее
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Settings.debug) Log.i(TAG, "onReceive");
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                if (Settings.debug) Log.i(TAG, "ACTION_GATT_CONNECTED");
                mConnected = true;
                mBTDeviceConn = mBTDevice;
                mIBluetoothListener.connectDialogInfo(mBTDevice);
                setStorages();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mBTDeviceConn = null;
                if (Settings.debug) Log.i(TAG, "ACTION_GATT_DISCONNECTED");
                mIBluetoothListener.disconnectDialogInfo(mBTDevice);
                clearAllDevicesFromList();
                startScanBluetoothDevice();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (Settings.debug) Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (Settings.debug) Log.i(TAG, "ACTION_DATA_AVAILABLE");

                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
                if (Settings.debug) Log.i(TAG, "ACTION_DATA_WRITE");
                // displayWdata(intent.getStringExtra(BluetoothLeService.EXTRA_WDATA));
            }
        }
    };

    // Обновление данных BroadcastReceiver
    public void updateBroadcastReceiveData(){
        if (Settings.debug) Log.i(TAG, "updateBroadcastReceiveData()");
        mIBluetoothListener.registerIReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            if (Settings.debug) Log.i(TAG, "Connect request result=" + result);
        }
    }

    public void unregisterReceiver(){
        if (Settings.debug) Log.i(TAG, "unregisterReceiver()");
        mIBluetoothListener.unregisterIReceiver(mGattUpdateReceiver);
    }

    // определяем фильтр для нашего BroadcastReceivera, чтобы регистрировать конкретные события
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        return intentFilter;
    }

    //-----------------------------BLE Data Transfer-------------------------------------
    // устанавливаем хранилища для данных
    private void setStorages(){
        if (Settings.debug) Log.i(TAG, "setStorages()");
        mBluetoothLeService.setStorageBtToNet(storageBtToNet);
        mBluetoothLeService.setStorageNetToBt(storageNetToBt);
    }

    //Собираем все имеющиеся характеристики устройства в коллекции
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        // обьявляем переменные для будущих сервисов и характеристик
        String uuid = null;  // уникальный идентификатор сервиса или характеристики
        String unknownServiceString = mIBluetoothListener.getUnknownServiceString();// если имя атрибуда сервиса не известно пишем это
        String unknownCharaString = mIBluetoothListener.unknownCharaString(); // если имя атрибуда характеристики не известно пишем это
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>(); // список доступных данных сервисов периферийного устройства
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>(); // список доступных данных характеристик периферийного устройства
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {  // прогоняем список всех сервисов устройства
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString(); // получаем идентификатор каждого сервиса
            // По таблице соответствия uuid имени сервиса на дисплей выводим именно имя из таблицы
            // если соответствия в таблице нет то выводим на дисплей unknown_service
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            // добавляем сервис в список
            gattServiceData.add(currentServiceData);
            // создаём список данных характеристики
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            // получаем все характеристики из сервиса(неименованные)
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            // создаём новый список характеристик
            ArrayList<BluetoothGattCharacteristic> charas =  new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                //добавляем характеристики в новый список
                charas.add(gattCharacteristic);
                // создаём карту данных характеристики
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                // получаем идентификатор каждой характеристики
                uuid = gattCharacteristic.getUuid().toString();
                // именуем все характеристики какие возможно согласно таблице uuid - SampleGattAttributes
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                // добавляем именнованные характеристики в список
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    //запускаем запись и нотификацию с устройства
    private void enableTransmitData() {
        if (Settings.debug) Log.i(TAG, "enableTransmitData()");
        mBluetoothLeService.initStore();
        if (!mGattCharacteristics.isEmpty()) {
            mNotifyCharacteristic = mGattCharacteristics.get(3).get(2);
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
            final BluetoothGattCharacteristic characteristic_write = mGattCharacteristics.get(3).get(1);
            mBluetoothLeService.writeCharacteristic(characteristic_write);
        } else{
            if (Settings.debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }
    //отключаем поток записи и нотификации
    private void disableTransmitData() {
        if (Settings.debug) Log.i(TAG, "disableTransmitData()");
        if (mBluetoothLeService != null ){
            if( mBluetoothLeService.getWriteThread() != null){
                mBluetoothLeService.stopDataTransfer();
            }
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
        }
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
}
