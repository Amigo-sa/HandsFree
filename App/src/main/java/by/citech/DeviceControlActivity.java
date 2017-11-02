/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package by.citech;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.bluetoothlegatt.LeDeviceListAdapter;
import by.citech.logic.Caller;
import by.citech.logic.ICallNetworkListener;
import by.citech.logic.ICallUiListener;
import by.citech.logic.IUiBtnGreenRedListener;
import by.citech.logic.INetworkInfoListener;
import by.citech.data.SampleGattAttributes;
import by.citech.data.StorageData;
import by.citech.logic.INetworkListener;
import by.citech.param.Settings;
import by.citech.param.Tags;
import static by.citech.util.NetworkInfo.getIPAddress;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements INetworkInfoListener, ICallNetworkListener, ICallUiListener {

    IUiBtnGreenRedListener iUiBtnGreenRedListener;
    INetworkListener iNetworkListener;
    private static final long SCAN_PERIOD = 10000;

    private static final int DEVICE_CONNECT = 1;
    private static final int DEVICE_CONNECTED = 2;
    private static final int DEVICE_DISCONNECT = 3;
    private static final int THIS_CONNECTED_DEVICE = 4;
    private static final int OTHER_CONNECTED_DEVICE = 5;

    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    // цвета кнопок
    private static final int GREEN = Color.rgb(0x00, 0x66, 0x33);
    private static final int GRAY = Color.GRAY;
    private static final int RED = Color.rgb(0xCC, 0x00, 0x00);
    private static final int DARKCYAN = Color.rgb(0, 139, 139);
    private static final int DARKKHAKI = Color.rgb(189, 183, 107);

    // выводим на дисплей состояние соединения
    private TextView mConnectionState;
    //выводим на дисплей принимаемые данные
    private TextView mDataField;
    private TextView mwDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    // Вьюхи для соединения с интернетом
    private Button btnGreen;
    private Button btnRed;
    private Animation animCall;
    private Button   btnChangeDevice;
    private EditText editTextSrvLocAddr;
    private EditText editTextSrvRemAddr;
    private EditText editTextSrvLocPort;
    private EditText editTextSrvRemPort;
    // кнопка отмены записанного текста
    private Button btnClearRemPort;
    private Button btnClearRemAddr;
    // разворачивающийся на экране список сервисов и характеристик переферийного устройства (сервера)
    private ExpandableListView mGattServicesList;
    // обьявляем сервис для обработки соединения и передачи данных (клиент - сервер)
    private BluetoothLeService mBluetoothLeService;
    // список характеристик устройства
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    // обьявляем характеристику для включения нотификации на периферийном устройстве(сервере)
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    // условие повторения анимации
    private boolean isCallAnim = false;

    // хранилища данных
    private StorageData storageBtToNet;
    private StorageData storageNetToBt;
    // список найденных устройств
    private ListView myListDevices;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothDevice mBTDevice;
    private BluetoothDevice mBTDeviceConn;
    // Класс BluetoothAdapter для связи софта с реальным железом BLE
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private LinearLayout MainView;
    private LinearLayout ScanView;

    private Handler mHandler;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private Intent gattServiceIntent;
    private AlertDialog alertDialog;
    private boolean visiblityMain = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        // And Checks if Bluetooth is supported on the device.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (!getBluetoothAdapter(bluetoothManager)) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        storageBtToNet = new StorageData(Tags.BLE2NET_STORE);
        storageNetToBt = new StorageData(Tags.NET2BLE_STORE);

        mHandler = new Handler();

        MainView = findViewById(R.id.MainView);
        ScanView = findViewById(R.id.ScanList);
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = findViewById(R.id.gatt_services_list);
        mConnectionState = findViewById(R.id.connection_state);
        mDataField = findViewById(R.id.data_value);
        mwDataField = findViewById(R.id.wdata_value);

        btnChangeDevice = findViewById(R.id.btnChangeHandsFree);
        btnChangeDevice.setText(R.string.connect_device);
        btnChangeDevice.setBackgroundColor(DARKKHAKI);

        editTextSrvLocAddr = findViewById(R.id.editTextSrvLocAddr);
        editTextSrvRemAddr = findViewById(R.id.editTextSrvRemAddr);
        editTextSrvLocPort = findViewById(R.id.editTextSrvLocPort);
        editTextSrvRemPort = findViewById(R.id.editTextSrvRemPort);

        btnClearRemAddr = findViewById(R.id.btnClearRemAddr);
        btnClearRemPort = findViewById(R.id.btnClearRemPort);
        btnClearRemAddr.setVisibility(View.VISIBLE);
        btnClearRemPort.setVisibility(View.VISIBLE);

        editTextSrvLocAddr.setText(getIPAddress(Settings.ipv4));
        editTextSrvLocPort.setText(String.format("%d", Settings.serverLocalPortNumber));
        editTextSrvLocAddr.setFocusable(false);
        editTextSrvRemPort.setText(String.format("%d", Settings.serverRemotePortNumber));
        editTextSrvLocPort.setFocusable(false);

        editTextSrvRemAddr.setText(Settings.serverRemoteIpAddress);

        //скрываем клавиатуру
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        getActionBar().setTitle("SecTel");
        //getActionBar().setDisplayHomeAsUpEnabled(true); - стрелочка в меню для перехода в другое активити
        // инициализируем сервис
        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        // привязываем сервис
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        btnGreen = findViewById(R.id.btnGreen);
        btnRed = findViewById(R.id.btnRed);
        animCall = AnimationUtils.loadAnimation(this, R.anim.anim_call);
        btnSetDisabled(btnGreen, "IDLE", GRAY);
        btnSetDisabled(btnRed, "IDLE", GRAY);

        iUiBtnGreenRedListener = Caller.getInstance().getiUiBtnGreenRedListener();
        iNetworkListener = Caller.getInstance().getiNetworkListener();

        Caller.getInstance()
                .setStorageBtToNet(storageBtToNet)
                .setStorageNetToBt(storageNetToBt)
                .setiCallUiListener(this)
                .setiCallNetworkListener(this)
                .setiNetworkInfoListener(this)
                .start();

        //скрываем клавиатуру
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {iUiBtnGreenRedListener.onClickBtnGreen();}});
        btnRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {iUiBtnGreenRedListener.onClickBtnRed();}});
        btnClearRemAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {editTextSrvRemAddr.setText("");}});
        btnClearRemPort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {editTextSrvRemPort.setText("");}});
        animCall.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {if (isCallAnim) {callAnimStart();}}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            Log.i(TAG, "mBluetoothLeService = " + mBluetoothLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    //---------------------------------Bluetooth -----------------------------
    private boolean getBluetoothAdapter(BluetoothManager bluetoothManager) {
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
            return false;
        else
            return true;
    }


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
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.w("WSD_BroadcastReceiver", " CONNECTED");
                mConnected = true;
                updateConnectionState(R.string.connected);
                onCreateDialogIsConnected(mBTDevice);
                setStorages();
                setUIData();
                btnChangeDevice.setText(R.string.change_device);
                btnChangeDevice.setBackgroundColor(DARKCYAN);
                invalidateOptionsMenu();
                mBTDeviceConn = mBTDevice;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.w("WSD_DCActivity", " DISCONNECTED");
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                onCreateDialogIsDisconnected(mBTDevice);
                mBTDeviceConn = null;
                clearUI();
                clearAllDevicesFromList();
                startScanBluetoothDevice();
                btnChangeDevice.setText(R.string.connect_device);
                btnChangeDevice.setBackgroundColor(DARKKHAKI);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
                displayWdata(intent.getStringExtra(BluetoothLeService.EXTRA_WDATA));
            }
        }
    };

    // Обновление данных BroadcastReceiver
    private void updateBroadcastReceiveData(){
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.i(TAG, "Connect request result=" + result);
        }
    }

    // кнопка на телефоне
    public void onBackPressed(){
        if(getVisiblityMain())
            super.onBackPressed();
        else
            setVisibleMain();
    }
    // кнопка подключения/изменения гарнитуры
    public void btnChangeDevice(View view){
        setVisibleList();
        myListDevices = (ListView) findViewById(R.id.ListDevices);
        initializeListBluetoothDevice();
        stopScanBluetoothDevice();
        startScanBluetoothDevice();

        myListDevices.setAdapter(mLeDeviceListAdapter);
        myListDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
            // останавливаем сканирование и запускаем новое Activity
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;
                mBTDevice = device;
                //выкидываем соответствующее диалоговое окно о подключении устройства
                Log.i("WSD_ONCLICK", "mBTDevice = " + mBTDevice);
                Log.i("WSD_ONCLICK", "mBTDeviceConn = " + mBTDeviceConn);
                if (mBTDevice.equals(mBTDeviceConn)) {
                    onCreateDialog(THIS_CONNECTED_DEVICE, mBTDevice);
                } else if (mBTDeviceConn != null) {
                    onCreateDialog(OTHER_CONNECTED_DEVICE, mBTDevice);
                } else {
                    onCreateDialog(DEVICE_CONNECT, mBTDevice);
                }
            }
        });
    }

    //------------------------Dialogs-----------------------------------------

    private void onCreateDialog(int id, BluetoothDevice device) {
        AlertDialog.Builder adb = new AlertDialog.Builder(DeviceControlActivity.this);

        switch (id) {
            case DEVICE_CONNECT: {
                onConnectBTDevice(mBTDevice);
                adb.setTitle(device.getName())
                        .setMessage(R.string.connect_message)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setCancelable(true)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                disconnectBTDevice();
                                dialog.cancel();
                            }
                        });
                alertDialog = adb.create();
                break;
            }
            case THIS_CONNECTED_DEVICE: {
                adb.setTitle(device.getName());
                adb.setMessage(R.string.click_connected_device_message);
                adb.setIcon(android.R.drawable.ic_dialog_info);
                adb.setPositiveButton(R.string.disconnect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearAllDevicesFromList();
                        startScanBluetoothDevice();
                        disconnectBTDevice();
                        dialog.dismiss();
                    }
                });
                adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alertDialog = adb.create();
                break;
            }
            case OTHER_CONNECTED_DEVICE: {
                adb.setTitle(device.getName());
                adb.setMessage(R.string.click_other_device_message);
                adb.setIcon(android.R.drawable.ic_dialog_info);
                adb.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        disconnectBTDevice();
                        onConnectBTDevice(mBTDevice);
                        dialog.dismiss();
                    }
                });
                adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alertDialog = adb.create();
                break;
            }
        }
        alertDialog.show();
    }

    //Окно когда устройство успешно подключилось
    private void onCreateDialogIsConnected(BluetoothDevice device) {
        alertDialog.dismiss();
        Log.i("WSD_DIALOG","onCreateDialogConnected");
        AlertDialog.Builder adb = new AlertDialog.Builder(DeviceControlActivity.this);
        adb.setTitle(device.getName())
                .setMessage(R.string.connected_message)
                .setIcon(android.R.drawable.checkbox_on_background)
                .setCancelable(true);

        final AlertDialog alertDialog = adb.create();
        alertDialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                alertDialog.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                setVisibleMain();
            }
        }, 2000); // after 2 second (or 2000 miliseconds), the task will be active.
    }
    // Окно когда устройство не может быть подсоединено или произошло разъединение
    private void onCreateDialogIsDisconnected(BluetoothDevice device) {
        alertDialog.dismiss();
        Log.i("WSD_DIALOG","onCreateDialogConnected");
        AlertDialog.Builder adb = new AlertDialog.Builder(DeviceControlActivity.this);
        adb.setTitle(device.getName())
                .setMessage(R.string.disconnected_message)
                .setIcon(android.R.drawable.ic_lock_power_off)
                .setCancelable(true);

        final AlertDialog alertDialog = adb.create();
        alertDialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                alertDialog.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                setVisibleList();
            }
        }, 2000); // after 2 second (or 2000 miliseconds), the task will be active.
    }

    //--------------------------Connection methods-----------------------------------

    private void disconnectBTDevice(){
        // производим отклчение от устройства
        mBluetoothLeService.disconnect();
    }

    private void onConnectBTDevice(BluetoothDevice device) {
        Log.i("WSD_DIALOG","onConnectBTDevice");
        // получаем данные от присоединяемого устройсва
        mDeviceName = device.getName();
        mDeviceAddress = device.getAddress();
        // останавливаем сканирование
        stopScanBluetoothDevice();
        // если сервис привязан производим соединение
        Log.i("WSD_DIALOG","mBluetoothLeService = " + mBluetoothLeService);
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(mDeviceAddress);
        // ответ ждём в Callback(BroadcastReceiver)
    }

    //-----------------------------------UI update------------------------------------
    private boolean getVisiblityMain(){
        return visiblityMain;
    }

    private void setVisiblityMain(boolean visiblityMain){
        this.visiblityMain = visiblityMain;
    }

    private void setUIData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("WSD_DIALOG","setUIData");
                ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
            }
        });
    }

    // процедура стирания списка характеристик и данных на дисплее
    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        ((TextView) findViewById(R.id.device_address)).setText(null);
        mDataField.setText(R.string.no_data);
        mwDataField.setText(R.string.no_data);
    }

    private void setVisibleMain() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
                setVisiblityMain(true);
                MainView.setVisibility(View.VISIBLE);
                ScanView.setVisibility(View.GONE);
            }
        });
    }

    private void setVisibleList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
                setVisiblityMain(false);
                MainView.setVisibility(View.GONE);
                ScanView.setVisibility(View.VISIBLE);
            }
        });
    }

    //-----------------------------List BT Devices ------------------------

    private void initializeListBluetoothDevice() {
        if (mLeDeviceListAdapter == null) {
            mLeDeviceListAdapter = new LeDeviceListAdapter(this.getLayoutInflater());
        } else
            clearAllDevicesFromList();
        if (mConnected)
            addConnectDeviceToList();
    }

    private void addConnectDeviceToList(){
        if (mConnected) {
            Log.i("WSD_CHANGE", "ADD DEVICE TO LIST " + mBTDevice + "\n");
            Log.i("WSD_CHANGE", "mLeDeviceListAdapter = " + mLeDeviceListAdapter + "\n");
            if ((mBTDevice != null) && mLeDeviceListAdapter != null)
                mLeDeviceListAdapter.addDevice(mBTDevice, 200);
        }
    }

    private void clearAllDevicesFromList(){
        if (mLeDeviceListAdapter != null)
            mLeDeviceListAdapter.clear();
    }

    //-----------------------------Scanning-------------------------------------

    private void startScanBluetoothDevice(){
        scanLeDevice(true);
    }

    private void stopScanBluetoothDevice(){
        scanLeDevice(false);
    }

    // процедура сканирования устройства
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device, rssi);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    //--------------------------Other Activity Methods-------------------------

    // по запуску регистрируем наш BroadcastReceiver
    @Override
    protected void onResume() {
        super.onResume();
        // по запуску регистрируем наш BroadcastReceiver
        updateBroadcastReceiveData();
    }

    // в случае засыпания активности сбрасываем регистрацию BroadcastReceiver
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        Caller.getInstance().stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // в случае переворачивания экрана или отключения программы отвязываем сервис и отключаем его
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("WSD_onDESTROY", " befor unbind");
        unbindService(mServiceConnection);
        if(mBluetoothLeService.getWriteThread() != null)
            mBluetoothLeService.stopWriteThread();
        mBluetoothLeService = null;
    }

    // создаём меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // в меню окна вызова указываем флажок подключенного устройства
        if (getVisiblityMain()) {
            getMenuInflater().inflate(R.menu.gatt_services, menu);
            if (mConnected) {
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_refresh).setVisible(true);
            } else {
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_refresh).setVisible(false);
            }
        }else {
            // в меню окна поиска устанавливаем кнопку сканирования
            getMenuInflater().inflate(R.menu.main, menu);
            if (!mScanning) {
                menu.findItem(R.id.menu_stop).setVisible(false);
                menu.findItem(R.id.menu_scan).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(null);
            } else {
                menu.findItem(R.id.menu_stop).setVisible(true);
                menu.findItem(R.id.menu_scan).setVisible(false);
                menu.findItem(R.id.menu_refresh).setActionView(
                        R.layout.actionbar_indeterminate_progress);
            }
        }
        return true;
    }

    // включаем/выключаем сканирование в зависимости от того нажата клавиша SCAN или нет
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                clearAllDevicesFromList();
                addConnectDeviceToList();
                startScanBluetoothDevice();
                break;
            case R.id.menu_stop:
                stopScanBluetoothDevice();
                break;
        }
        return true;
    }

    // процедура обновления данных на экране об соединении
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    // отображение данных нотификации на дисплее
    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    // отображение данных записи на дисплее
    private void displayWdata(String data) {
        if (data != null) {
            mwDataField.setText(data);
        }
    }

    private void setStorages(){
        mBluetoothLeService.setStorageBtToNet(storageBtToNet);
        mBluetoothLeService.setStorageNetToBt(storageNetToBt);
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    // процедура обработки и отображения доступных сервисов и характеристик на дисплее
    private void enableTransmitData() {
        mBluetoothLeService.initStore();
        if (!mGattCharacteristics.isEmpty()) {
            mNotifyCharacteristic = mGattCharacteristics.get(3).get(2);
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
            final BluetoothGattCharacteristic characteristic_write = mGattCharacteristics.get(3).get(1);
            mBluetoothLeService.writeCharacteristic(characteristic_write);
        } else{
            Toast.makeText(this, "Device not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableTransmitData() {
        if (mBluetoothLeService != null ){
            if( mBluetoothLeService.getWriteThread() != null){
                mBluetoothLeService.stopDataTransfer();
            }
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        // обьявляем переменные для будущих сервисов и характеристик
        String uuid = null;  // уникальный идентификатор сервиса или характеристики
        String unknownServiceString = getResources().getString(R.string.unknown_service);  // если имя атрибуда сервиса не известно пишем это
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic); // если имя атрибуда характеристики не известно пишем это
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
        // все полученные списки помещаем в SimpleExpandableListAdapter, который
        // работает с ExpandableListView для отображения на дисплее
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        // mGattServicesList.setAdapter(gattServiceAdapter);
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

    //--------------------- Buttons

    private void btnSetDisabled(Button button, String label, int color) {
        button.setEnabled(false);
        btnSetColorLabel(button, label, color);
    }

    private void btnSetEnabled(Button button, String label, int color) {
        button.setEnabled(true);
        btnSetColorLabel(button, label, color);
    }

    private void btnSetColorLabel(Button button, String label, int color) {
        button.setText(label);
        button.setBackgroundColor(color);
    }

    private void callAnimStart() {
        if (Settings.debug && !isCallAnim) Log.i(Tags.ACT_DEVICECTRL, "callAnimStart");
        btnGreen.startAnimation(animCall);
        isCallAnim = true;
    }

    private void callAnimStop() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callAnimStop");
        btnGreen.clearAnimation();
        isCallAnim = false;
    }

    //--------------------- ICallNetworkListener

    @Override
    public void callFailed() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callFailed");
        btnSetDisabled(btnRed, "FAIL", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
    }

    @Override
    public void callEndedExternally() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callEndedExternally");
        btnSetDisabled(btnRed, "ENDED", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
    }

    @Override
    public void callOutcomingConnected() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callOutcomingConnected");
        btnSetEnabled(btnRed, "CANCEL", RED);
        btnSetEnabled(btnGreen, "CALLING...", GREEN);
    }

    @Override
    public void callOutcomingAccepted() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callOutcomingAccepted");
        btnSetEnabled(btnRed, "END CALL", RED);
        btnSetDisabled(btnGreen, "ON CALL", GRAY);
        callAnimStop();
    }

    @Override
    public void callOutcomingRejected() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callOutcomingRejected");
        btnSetDisabled(btnRed, "BUSY", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
    }

    @Override
    public void callOutcomingFailed() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callOutcomingFailed");
        btnSetDisabled(btnRed, "OFFLINE", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
    }

    @Override
    public void callOutcomingLocal() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callOutcomingLocal");
        btnSetDisabled(btnRed, "LOCAL", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
    }

    @Override
    public void callIncomingDetected() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callIncomingDetected");
        btnSetEnabled(btnRed, "REJECT", RED);
        btnSetEnabled(btnGreen, "INCOMING...", GREEN);
        callAnimStart();
    }

    @Override
    public void callIncomingCanceled() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callIncomingCanceled");
        btnSetDisabled(btnRed, "CANCELED", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
    }

    @Override
    public void callIncomingFailed() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callIncomingFailed");
        btnSetDisabled(btnRed, "INCOME FAIL", GRAY);
        btnSetEnabled(btnGreen, "CALL", RED);
        callAnimStop();
    }

    @Override
    public void connectorFailure() {
        if (Settings.debug) Log.e(Tags.ACT_DEVICECTRL, "connectorFailure");
        btnSetDisabled(btnRed, "ERROR", GRAY);
        btnSetDisabled(btnGreen, "ERROR", GRAY);
    }

    @Override
    public void connectorReady() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "connectorReady");
        btnSetEnabled(btnGreen, "CALL", GREEN);
        btnSetDisabled(btnRed, "IDLE", GRAY);
    }

    //--------------------- ICallUiListener

    @Override
    public void callEndedInternally() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callEndedInternally");
        btnSetDisabled(btnRed, "ENDED", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
    }

    @Override
    public void callOutcomingCanceled() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callOutcomingCanceled");
        btnSetDisabled(btnRed, "CANCELED", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
    }

    @Override
    public void callOutcomingStarted() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callOutcomingStarted");
        btnSetEnabled(btnRed, "CANCEL", RED);
        btnSetDisabled(btnGreen, "CALLING...", GRAY);
        callAnimStart();
    }

    @Override
    public void callIncomingRejected() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callIncomingRejected");
        btnSetDisabled(btnRed, "REJECTED", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
    }

    @Override
    public void callIncomingAccepted() {
        if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "callIncomingAccepted");
        btnSetEnabled(btnRed, "END CALL", RED);
        btnSetDisabled(btnGreen, "ON CALL", GRAY);
        callAnimStop();
    }

    //--------------------- INetworkInfoListener

    @Override
    public String getRemAddr() {
        return editTextSrvRemAddr.getText().toString();
    }

    @Override
    public String getRemPort() {
        return editTextSrvRemPort.getText().toString();
    }

    @Override
    public String getLocPort() {
        return editTextSrvLocPort.getText().toString();
    }

}
