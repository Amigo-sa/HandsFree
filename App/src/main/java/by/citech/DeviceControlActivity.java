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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.client.asynctask.ConnectTask;
import by.citech.connection.StreamTask;
import by.citech.client.network.IClientCtrl;
import by.citech.client.network.IClientOn;
import by.citech.connection.IMessage;
import by.citech.connection.IStream;
import by.citech.connection.IStreamOn;
import by.citech.data.SampleGattAttributes;
import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;
import by.citech.connection.RedirectTask;
import by.citech.server.asynctask.ServerOnTask;
import by.citech.connection.IRedirectCtrl;
import by.citech.connection.IRedirectOn;
import by.citech.server.network.IServerCtrl;
import by.citech.server.network.IServerOn;

import static by.citech.util.NetworkInfo.getIPAddress;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements IServerOn, IRedirectOn, IStreamOn, IClientOn, IMessage {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    // выводим на дисплей состояние соединения
    private TextView mConnectionState;
    //выводим на дисплей принимаемые данные
    private TextView mDataField;
    private TextView mwDataField;
    private String   mDeviceName;
    private String   mDeviceAddress;
    // Вьюхи для соединения с интернетом
    private Button btnCallOut;
    private Button btnCallIn;
    private EditText editTextSrvLocAddr;
    private EditText editTextSrvRemAddr;
    private EditText editTextSrvLocPort;
    private EditText editTextSrvRemPort;
    // разворачивающийся на экране список сервисов и характеристик переферийного устройства (сервера)
    private ExpandableListView mGattServicesList;
    // обьявляем сервис для обработки соединения и передачи данных (клиент - сервер)
    private BluetoothLeService mBluetoothLeService;
    // список характеристик устройства
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    // обьявляем характеристику для включения нотификации на периферийном устройстве(сервере)
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean callOutOneClick;
    private boolean callInOneClick;

    // работа с сетью
    private Handler handler;
    private IServerCtrl iServerCtrl;
    private IClientCtrl iClientCtrl;
    private IRedirectCtrl iRedirectCtrl;
    private IStream iStream;

    // хранилища данных
    private StorageData storageBtToNet;
    private StorageData storageNetToBt;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private boolean loopback = false;
    // Code to manage Service lifecycle.
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
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

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
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                //enableTransmitData();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
                displayWdata(intent.getStringExtra(BluetoothLeService.EXTRA_WDATA));
                //Log.w(TAG,"write data to dispaly");
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    // обработчик события в случае нажатия на расширяющемся списке на конкретную характеристику
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        // получаем характеристику
                        final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                        //final BluetoothGattCharacteristic characteristic_write = mGattCharacteristics.get(3).get(0);
                        Log.e(TAG, "characteritic groupPosition = " + groupPosition + "\n" +
                                   "childPosition = " + childPosition );
                        // получаем свойство характеристики
                        final int charaProp = characteristic.getProperties();
/*
                        Log.e(TAG, "charaProp = " + charaProp + "\n" +
                                "PROPERTY_READ = " + BluetoothGattCharacteristic.PROPERTY_READ + "\n" +
                                "PROPERTY_NOTIFY = " + BluetoothGattCharacteristic.PROPERTY_NOTIFY + "\n" +
                                "PROPERTY_WRITE_NO_RESPONSE = " + BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE + "\n" +
                                "PROPERTY_WRITE = " + BluetoothGattCharacteristic.PROPERTY_WRITE);

                        Log.e(TAG, "getUuid = " + characteristic.getUuid() + "\n" +
                                   "WRITE_BYTES = " + SampleGattAttributes.WRITE_BYTES + "\n" +
                                   "equals = " + SampleGattAttributes.WRITE_BYTES.equals(characteristic.getUuid().toString()));
                        */
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) == BluetoothGattCharacteristic.PROPERTY_READ) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        // если у характеристики есть нотификация то запускаем её
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                            //Log.e(TAG, "Notification enabled");
                        }
                        /*
                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        }
                        else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        }
                        */
                        // в случае, если включена характеристика со свойством записи то производим запись  PROPERTY_WRITE_NO_RESPONSE
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE ) == BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) {
                            //Log.e(TAG, "charaProp = " + charaProp + "\n");
                            //Log.e(TAG, "PROPERTY_WRITE_NO_RESPONSE = " + BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE + "\n");
                            if (SampleGattAttributes.WRITE_BYTES.equals(characteristic.getUuid().toString())) {
                                //Log.e(TAG, "Before write!!!!!!");
                                if (!loopback) {
                                    //Log.e(TAG, "write!!!!!!");
                                    mBluetoothLeService.initStore();
                                    loopback = true;
                                } else {
                                   // Log.e(TAG, "close write!!!!!!");
                                    mBluetoothLeService.closeStore();
                                    //mBluetoothLeService.cleanStore();
                                    loopback = false;
                                }
                                mBluetoothLeService.writeCharacteristic(characteristic);
                                //mBluetoothLeService.writeCharacteristic(characteristic_write);
                           }
                        }
                        return true;
                    }
                    return false;
                }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        final DeviceControlActivity activity = this;
        storageBtToNet = new StorageData(Tags.NET_STORE_BT2NET);
        storageNetToBt = new StorageData(Tags.NET_STORE_NET2BT);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case StatusMessages.SRV_ONMESSAGE:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONMESSAGE");
                        //if (Settings.debug) Log.i(Tags.ACT_DPL, String.format("handleMessage SRV_ONMESSAGE %s", ((WebSocketFrame) msg.obj).getTextPayload()));
                        break;
                    case StatusMessages.SRV_ONCLOSE:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONCLOSE");
                        break;
                    case StatusMessages.SRV_ONOPEN:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONOPEN");
                        callIn();
//                      if (Settings.testSendOneOnCall) {
//                          new ConnectTask(DeviceControlActivity.this, handler).execute(String.format("ws://%s:%s",
//                                  editTextSrvRemAddr.getText().toString(),
//                                  editTextSrvRemPort.getText().toString()));
//                          new SendMessageTask(DeviceControlActivity.this, iClientCtrl).execute("FUCK YOU ASSHOLE");
//                      } else {
//                          btnCallIn.setEnabled(true);
//                      }
                        break;
                    case StatusMessages.SRV_ONPONG:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONPONG");
                        break;
                    case StatusMessages.SRV_ONEXCEPTION:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONEXCEPTION");
                        break;
                    case StatusMessages.SRV_ONDEBUGFRAMERX:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONDEBUGFRAMERX");
                        break;
                    case StatusMessages.SRV_ONDEBUGFRAMETX:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONDEBUGFRAMETX");
                        break;

                    case StatusMessages.CLT_ONOPEN:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONOPEN");
                        new StreamTask(DeviceControlActivity.this, iClientCtrl.getTransmitter(), Settings.dataSource, storageBtToNet).execute();
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONOPEN post new StreamTask");
                        new RedirectTask(DeviceControlActivity.this, iClientCtrl.getReceiverRegister(), Settings.dataSource, storageNetToBt).execute();
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONOPEN post new RedirectTask");
//                      new RedirectTask(DeviceControlActivity.this, (IReceiverRegister) iServerCtrl, Settings.dataSource, storageNetToBt).execute();
//                      new StreamTask(DeviceControlActivity.this, (ITransmitter) iClientCtrl, Settings.dataSource, storageBtToNet).execute();
                        break;
                    case StatusMessages.CLT_ONMESSAGE_BYTES:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONMESSAGE_BYTES");
                        break;
                    case StatusMessages.CLT_ONMESSAGE_TEXT:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONMESSAGE_TEXT");
                        break;
                    case StatusMessages.CLT_ONCLOSING:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONCLOSING");
                        break;
                    case StatusMessages.CLT_ONCLOSED:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONCLOSED");
                        break;
                    case StatusMessages.CLT_ONFAILURE:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONFAILURE");
                        Toast.makeText(activity, "SUBSCRIBER NOT ONLINE", Toast.LENGTH_SHORT).show();
                        break;
                    case StatusMessages.CLT_CANCEL:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_CANCEL");
                        break;
                    default:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage DEFAULT");
                        break;
                }
            }
        };

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = findViewById(R.id.connection_state);
        mDataField = findViewById(R.id.data_value);
        mwDataField = findViewById(R.id.wdata_value);

        btnCallOut = findViewById(R.id.btnCallOut);
        btnCallIn = findViewById(R.id.btnCallIn);
        editTextSrvLocAddr = findViewById(R.id.editTextSrvLocAddr);
        editTextSrvRemAddr = findViewById(R.id.editTextSrvRemAddr);
        editTextSrvLocPort = findViewById(R.id.editTextSrvLocPort);
        editTextSrvRemPort = findViewById(R.id.editTextSrvRemPort);

        btnCallOut.setEnabled(false);
        btnCallIn.setEnabled(false);
        editTextSrvLocAddr.setText(getIPAddress(Settings.ipv4));
        editTextSrvLocAddr.setFocusable(false);
        editTextSrvLocPort.setText(String.format("%d", Settings.serverLocalPortNumber));
        editTextSrvRemPort.setText(String.format("%d", Settings.serverRemotePortNumber));
        editTextSrvRemAddr.setText(Settings.serverRemoteIpAddress);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // привязываем сервис
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        new ServerOnTask(this, handler).execute(editTextSrvLocPort.getText().toString());

        // устанавливаем только одно нажатие клавиши Call
        callOutOneClick = true;
        btnCallOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callOutOneClick) {
                    enableTransmitData();
                    callOut();
                    callOutOneClick = false;
                }
            }
        });

        callInOneClick = true;
        btnCallIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callInOneClick) {
                    enableTransmitData();
                    callIn();
                    callInOneClick = false;
                }
            }
        });
    }

    private void callIn() {
        enableTransmitData();
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callIn");
//      new ConnectTask(DeviceControlActivity.this, handler).execute(String.format("ws://%s:%s",
//              editTextSrvRemAddr.getText().toString(),
//              editTextSrvRemPort.getText().toString()));
//      if (iClientCtrl == null) {
//          if (Settings.debug) Log.i(Tags.ACT_DPL, "callIn iClientCtrl is null");
//          return;
//      }
        new StreamTask(DeviceControlActivity.this, iServerCtrl.getTransmitter(), Settings.dataSource, storageBtToNet).execute();
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callIn post new StreamTask");
        new RedirectTask(DeviceControlActivity.this, iServerCtrl.getReceiverRegister(), Settings.dataSource, storageNetToBt).execute();
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callIn post new RedirectTask");
    }

    private void callOut() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callOut");
        new ConnectTask(DeviceControlActivity.this, handler).execute(String.format("ws://%s:%s",
                editTextSrvRemAddr.getText().toString(),
                editTextSrvRemPort.getText().toString()));
//      if (Settings.debug) Log.i(Tags.ACT_DPL, "callOut post new ConnectTask");
//      if (iClientCtrl == null) {
//          if (Settings.debug) Log.i(Tags.ACT_DPL, "callOut iClientCtrl is null");
//          return;
//      }

//      new StreamTask(DeviceControlActivity.this, iClientCtrl, Settings.dataSource, storageBtToNet).execute();
//      if (Settings.debug) Log.i(Tags.ACT_DPL, "callOut post new StreamTask");
//      new RedirectTask(DeviceControlActivity.this, iServerCtrl, Settings.dataSource, storageNetToBt).execute();
//      if (Settings.debug) Log.i(Tags.ACT_DPL, "callOut post new RedirectTask");
    }

    // процедура стирания списка характеристик и данных на дисплее
    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
        mwDataField.setText(R.string.no_data);
    }

    // по запуску регистрируем наш BroadcastReceiver
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    // в случае засыпания активности сбрасываем регистрацию BroadcastReceiver
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    //  в случае переворачивания экрана или отключения программы отвязываем сервис и отключаем его
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        if(mBluetoothLeService.getWriteThread() != null)
            mBluetoothLeService.stopWriteThread();
        mBluetoothLeService = null;

    }

    // создаём меню в котором указываем кнопку соединения устройства
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            callOutOneClick = true;
            mBluetoothLeService.setStorageBtToNet(storageBtToNet);
            mBluetoothLeService.setStorageNetToBt(storageNetToBt);
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    // устанавливаем принудетельное соединение с выбранным из списка устройством при нажатии connect
    // и сбрасываем соединение в случае нажатия disconnect
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:

                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    // процедура обработки и отображения доступных сервисов и характеристик на дисплее

    private void enableTransmitData(){
        mBluetoothLeService.initStore();
        if (mGattCharacteristics != null)
            mNotifyCharacteristic = mGattCharacteristics.get(3).get(2);
        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
        final BluetoothGattCharacteristic characteristic_write = mGattCharacteristics.get(3).get(1);
        mBluetoothLeService.writeCharacteristic(characteristic_write);

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

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "serverStarted");
        btnCallOut.setEnabled(true);
        this.iServerCtrl = iServerCtrl;
    }

    @Override
    public void serverCantStart() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "serverCantStart");
        final DeviceControlActivity activity = this;
        btnCallOut.setEnabled(false);
        Toast.makeText(activity, "CANT START SERVER", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setRedirect(IRedirectCtrl iRedirectCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "setRedirect");
        this.iRedirectCtrl = iRedirectCtrl;
    }

    @Override
    public void setStream(IStream iStream) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "setStream");
        this.iStream = iStream;
    }

    @Override
    public void clientStarted(IClientCtrl iClientCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "clientStarted");
        if (iClientCtrl == null) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "clientStarted iClientCtrl is null");
        }
        this.iClientCtrl = iClientCtrl;
    }

    @Override
    public void messageSended() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "messageSended");
    }

    @Override
    public void messageCantSend() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "messageCantSend");
    }
}
