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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.bluetoothlegatt.LeDeviceListAdapter;
import by.citech.logic.Caller;
import by.citech.logic.ConnectorBluetooth;
import by.citech.logic.IBluetoothListener;
import by.citech.logic.ICallNetworkListener;
import by.citech.logic.ICallUiListener;
import by.citech.logic.IUiBtnGreenRedListener;
import by.citech.logic.INetworkInfoListener;
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
public class DeviceControlActivity extends Activity
        implements INetworkInfoListener, ICallNetworkListener, ICallUiListener, IBluetoothListener {

    IUiBtnGreenRedListener iUiBtnGreenRedListener;
    INetworkListener iNetworkListener;

    private static final int DEVICE_CONNECT = 1;
    private static final int THIS_CONNECTED_DEVICE = 4;
    private static final int OTHER_CONNECTED_DEVICE = 5;

    private final static String TAG = "WSD_DCActivity";
    // цвета кнопок
    private static final int GREEN = Color.rgb(0x00, 0x66, 0x33);
    private static final int GRAY = Color.GRAY;
    private static final int RED = Color.rgb(0xCC, 0x00, 0x00);
    private static final int DARKCYAN = Color.rgb(0, 139, 139);
    private static final int DARKKHAKI = Color.rgb(189, 183, 107);
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
    // условие повторения анимации
    private boolean isCallAnim = false;
    // список найденных устройств
    private ListView myListDevices;
    private LinearLayout MainView;
    private LinearLayout ScanView;

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
        if (!ConnectorBluetooth.getInstance().getBluetoothAdapter(bluetoothManager)) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MainView = findViewById(R.id.MainView);
        ScanView = findViewById(R.id.ScanList);
        // Sets up UI references.
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
        getActionBar().setTitle("SecTel");
        //getActionBar().setDisplayHomeAsUpEnabled(true); - стрелочка в меню для перехода в другое активити
        // инициализируем сервис
        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        // привязываем сервис
        bindService(gattServiceIntent, ConnectorBluetooth.getInstance().mServiceConnection, BIND_AUTO_CREATE);

        btnGreen = findViewById(R.id.btnGreen);
        btnRed = findViewById(R.id.btnRed);
        animCall = AnimationUtils.loadAnimation(this, R.anim.anim_call);
        btnSetDisabled(btnGreen, "IDLE", GRAY);
        btnSetDisabled(btnRed, "IDLE", GRAY);

        iUiBtnGreenRedListener = Caller.getInstance().getiUiBtnGreenRedListener();
        iNetworkListener = Caller.getInstance().getiNetworkListener();

        Caller.getInstance()
                .setiCallUiListener(this)
                .setiCallNetworkListener(this)
                .setiNetworkInfoListener(this)
                .setiBluetoothListener(this)
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

    private boolean getVisiblityMain(){
        return visiblityMain;
    }

    private void setVisiblityMain(boolean visiblityMain){
        this.visiblityMain = visiblityMain;
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

    private void changeMainGUI(int color, int buttonName){
        btnChangeDevice.setText(buttonName);
        btnChangeDevice.setBackgroundColor(color);
        invalidateOptionsMenu();
    }

    //------------------------BT-Buttons---------------------

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
        ConnectorBluetooth.getInstance().initializeListBluetoothDevice();
        myListDevices.setAdapter(ConnectorBluetooth.getInstance().getmLeDeviceListAdapter());
        myListDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
            // останавливаем сканирование и запускаем новое Activity
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = ConnectorBluetooth.getInstance().getmLeDeviceListAdapter().getDevice(position);
                if (device == null) return;
                ConnectorBluetooth.getInstance().setmBTDevice(device);
                //выкидываем соответствующее диалоговое окно о подключении устройства
                if (Settings.debug) Log.i(TAG, "mBTDevice = " + device);
                if (Settings.debug) Log.i(TAG, "mBTDeviceConn = " + ConnectorBluetooth.getInstance().getmBTDeviceConn());
                if (ConnectorBluetooth.getInstance().getmBTDevice().equals(ConnectorBluetooth.getInstance().getmBTDeviceConn())) {
                    onCreateDialog(THIS_CONNECTED_DEVICE, ConnectorBluetooth.getInstance().getmBTDevice());
                } else if (ConnectorBluetooth.getInstance().getmBTDeviceConn() != null) {
                    onCreateDialog(OTHER_CONNECTED_DEVICE, ConnectorBluetooth.getInstance().getmBTDevice());
                } else {
                    onCreateDialog(DEVICE_CONNECT, ConnectorBluetooth.getInstance().getmBTDevice());
                }
            }
        });
        ConnectorBluetooth.getInstance().stopScanBluetoothDevice();
        ConnectorBluetooth.getInstance().startScanBluetoothDevice();
    }

    //------------------------Dialogs-----------------------------------------

    private void onCreateDialog(int id, BluetoothDevice device) {
        AlertDialog.Builder adb = new AlertDialog.Builder(DeviceControlActivity.this);

        switch (id) {
            case DEVICE_CONNECT: {
                ConnectorBluetooth.getInstance().onConnectBTDevice();
                adb.setTitle(device.getName())
                        .setMessage(R.string.connect_message)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setCancelable(true)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ConnectorBluetooth.getInstance().disconnectBTDevice();
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
                        ConnectorBluetooth.getInstance().clearAllDevicesFromList();
                        ConnectorBluetooth.getInstance().startScanBluetoothDevice();
                        ConnectorBluetooth.getInstance().disconnectBTDevice();
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
                        ConnectorBluetooth.getInstance().disconnectBTDevice();
                        ConnectorBluetooth.getInstance().onConnectBTDevice();
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
        if (Settings.debug) Log.i(TAG,"onCreateDialogConnected");
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

    //--------------------------Other Activity Methods-------------------------

    // по запуску регистрируем наш BroadcastReceiver
    @Override
    protected void onResume() {
        super.onResume();
        // по запуску регистрируем наш BroadcastReceiver
        ConnectorBluetooth.getInstance().updateBroadcastReceiveData();
    }

    // в случае засыпания активности сбрасываем регистрацию BroadcastReceiver
    @Override
    protected void onPause() {
        super.onPause();
        ConnectorBluetooth.getInstance().unregisterReceiver();
        Caller.getInstance().stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // в случае отключения программы отвязываем сервис и отключаем его
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectorBluetooth.getInstance().closeLeService();
    }

    // создаём меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // в меню окна вызова указываем флажок подключенного устройства
        if (getVisiblityMain()) {
            getMenuInflater().inflate(R.menu.gatt_services, menu);
            if (ConnectorBluetooth.getInstance().ismConnected()) {
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_refresh).setVisible(true);
            } else {
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_refresh).setVisible(false);
            }
        }else {
            // в меню окна поиска устанавливаем кнопку сканирования
            getMenuInflater().inflate(R.menu.main, menu);
            if (!ConnectorBluetooth.getInstance().ismScanning()) {
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
                ConnectorBluetooth.getInstance().clearAllDevicesFromList();
                ConnectorBluetooth.getInstance().addConnectDeviceToList();
                ConnectorBluetooth.getInstance().startScanBluetoothDevice();
                break;
            case R.id.menu_stop:
                ConnectorBluetooth.getInstance().stopScanBluetoothDevice();
                break;
        }
        return true;
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

    //--------------------- IBluetoothListener

    @Override
    public void changeOptionMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public void addDeviceToList(final LeDeviceListAdapter leDeviceListAdapter, final BluetoothDevice device , final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                leDeviceListAdapter.addDevice(device, rssi);
                leDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public LeDeviceListAdapter addLeDeviceListAdapter() {
        return new LeDeviceListAdapter(this.getLayoutInflater());
    }

    @Override
    public void finishConnection() {
        finish();
    }

    @Override
    public void disconnectToast() {
        Toast.makeText(this, "Device not connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void disconnectDialogInfo(BluetoothDevice bluetoothDevice) {
        changeMainGUI(DARKKHAKI, R.string.connect_device);
        onCreateDialogIsDisconnected(bluetoothDevice);
    }

    @Override
    public void connectDialogInfo(BluetoothDevice bluetoothDevice) {
        changeMainGUI(DARKCYAN, R.string.change_device);
        onCreateDialogIsConnected(bluetoothDevice);
    }

    @Override
    public void registerIReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void unregisterIReceiver(BroadcastReceiver broadcastReceiver) {
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public String getUnknownServiceString() {
        return getResources().getString(R.string.unknown_service);
    }

    @Override
    public String unknownCharaString() {
        return  getResources().getString(R.string.unknown_characteristic);
    }

}
