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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.bluetoothlegatt.LeDeviceListAdapter;
import by.citech.logic.Caller;
import by.citech.logic.ConnectorBluetooth;
import by.citech.logic.IBluetoothListener;
import by.citech.logic.ICallNetworkListener;
import by.citech.gui.ICallUiListener;
import by.citech.debug.IDebugListener;
import by.citech.gui.IUiBtnGreenRedListener;
import by.citech.network.INetworkInfoListener;
import by.citech.network.INetworkListener;
import by.citech.logic.CallerState;
import by.citech.param.DebugMode;
import by.citech.param.Settings;
import by.citech.param.Tags;

import static by.citech.util.NetworkInfo.getIpAddr;

public class DeviceControlActivity extends Activity
        implements INetworkInfoListener, ICallNetworkListener, ICallUiListener, IBluetoothListener, IDebugListener {

    private static final String TAG = Tags.ACT_DEVICECTRL;
    private static final boolean debug = Settings.debug;
    private static final DebugMode debugMode = Settings.debugMode;

    IUiBtnGreenRedListener iUiBtnGreenRedListener;
    INetworkListener iNetworkListener;

    private static final int DEVICE_CONNECT = 1;
    private static final int THIS_CONNECTED_DEVICE = 4;
    private static final int OTHER_CONNECTED_DEVICE = 5;

    // цвета кнопок
    private static final int GREEN = Color.rgb(0x00, 0x66, 0x33);
    private static final int GRAY = Color.GRAY;
    private static final int RED = Color.rgb(0xCC, 0x00, 0x00);
    private static final int DARKCYAN = Color.rgb(0, 139, 139);
    private static final int DARKKHAKI = Color.rgb(189, 183, 107);
    // Вьюхи для соединения с интернетом
    private Animation animCall;
    private Button btnGreen, btnRed, btnChangeDevice;
    private EditText editTextSrvLocAddr, editTextSrvRemAddr, editTextSrvLocPort, editTextSrvRemPort;
    // Отображение траффика для дебага
    TextView textViewBtInTraffic, textViewBtOutTraffic, textViewNetInTraffic, textViewNetOutTraffic;

    // условие повторения анимации
    private boolean isCallAnim = false;

    // список найденных устройств
    private ListView myListDevices;
    private LinearLayout MainView;
    private LinearLayout ScanView;  
    
    private Intent gattServiceIntent;
    private AlertDialog alertDialog;
    private boolean visiblityMain = true;

    private Caller caller;
    private ConnectorBluetooth connectorBluetooth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        caller = Caller.getInstance()
                .setiCallUiListener(this)
                .setiDebugListener(this)
                .setiCallNetworkListener(this)
                .setiNetworkInfoListener(this)
                .setiBluetoothListener(this);

        connectorBluetooth = caller.getConnectorBluetooth();
        iUiBtnGreenRedListener = caller.getiUiBtnGreenRedListener();
        iNetworkListener = caller.getiNetworkListener();

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
        if (!connectorBluetooth.getBluetoothAdapter(bluetoothManager)) {
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

        btnGreen = findViewById(R.id.btnGreen);
        btnRed = findViewById(R.id.btnRed);
        animCall = AnimationUtils.loadAnimation(this, R.anim.anim_call);

        switch (debugMode) {
            case LoopbackBtToBt:
                btnSetEnabled(btnGreen, "LBACK ON");
                btnSetDisabled(btnRed, "LBACK OFF");
                break;
            case Record:
                btnSetEnabled(btnGreen, "RECORD");
                btnSetDisabled(btnRed, "PLAY");
                break;
            case Normal:
                btnSetDisabled(btnGreen, "IDLE");
                btnSetDisabled(btnRed, "IDLE");
            case LoopbackNetToNet:
                break;
            default:
                break;
        }

        editTextSrvLocAddr.setText(getIpAddr(Settings.ipv4));
        editTextSrvLocPort.setText(String.format("%d", Settings.serverLocalPortNumber));
        editTextSrvLocAddr.setFocusable(false);
        editTextSrvRemPort.setText(String.format("%d", Settings.serverRemotePortNumber));
        editTextSrvLocPort.setFocusable(false);
        editTextSrvRemAddr.setText(Settings.serverRemoteIpAddress);

        getActionBar().setTitle("SecTel");
        
        // скрываем клавиатуру
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // инициализируем сервис
        gattServiceIntent = new Intent(this, BluetoothLeService.class);

        // привязываем сервис
        bindService(gattServiceIntent, caller.getServiceConnection(), BIND_AUTO_CREATE);

        btnGreen.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {iUiBtnGreenRedListener.onClickBtnGreen();}});
        btnRed.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {iUiBtnGreenRedListener.onClickBtnRed();}});
        findViewById(R.id.btnClearRemAddr).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {editTextSrvRemAddr.setText("");}});
        findViewById(R.id.btnClearRemPort).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {editTextSrvRemPort.setText("");}});
        animCall.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {if (isCallAnim) {callAnimStart();}}
            @Override public void onAnimationRepeat(Animation animation) {}
        });

        caller.build();
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
        connectorBluetooth.initializeListBluetoothDevice();
        myListDevices.setAdapter(connectorBluetooth.getmLeDeviceListAdapter());
        myListDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
            // останавливаем сканирование и запускаем новое Activity
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = connectorBluetooth.getmLeDeviceListAdapter().getDevice(position);
                if (device == null) return;
                connectorBluetooth.setmBTDevice(device);
                //выкидываем соответствующее диалоговое окно о подключении устройства
                if (debug) Log.i(TAG, "mBTDevice = " + device);
                if (debug) Log.i(TAG, "mBTDeviceConn = " + connectorBluetooth.getmBTDeviceConn());
                if (connectorBluetooth.getmBTDevice().equals(connectorBluetooth.getmBTDeviceConn())) {
                    onCreateDialog(THIS_CONNECTED_DEVICE, connectorBluetooth.getmBTDevice());
                } else if (connectorBluetooth.getmBTDeviceConn() != null) {
                    onCreateDialog(OTHER_CONNECTED_DEVICE, connectorBluetooth.getmBTDevice());
                } else {
                    onCreateDialog(DEVICE_CONNECT, connectorBluetooth.getmBTDevice());
                }
            }
        });
        connectorBluetooth.stopScanBluetoothDevice();
        connectorBluetooth.startScanBluetoothDevice();
    }

    //------------------------Dialogs-----------------------------------------

    private void onCreateDialog(int id, BluetoothDevice device) {
        AlertDialog.Builder adb = new AlertDialog.Builder(DeviceControlActivity.this);

        switch (id) {
            case DEVICE_CONNECT: {
                connectorBluetooth.onConnectBTDevice();
                adb.setTitle(device.getName())
                        .setMessage(R.string.connect_message)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setCancelable(true)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                connectorBluetooth.disconnectBTDevice();
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
                        connectorBluetooth.clearAllDevicesFromList();
                        connectorBluetooth.startScanBluetoothDevice();
                        connectorBluetooth.disconnectBTDevice();
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
                        connectorBluetooth.disconnectBTDevice();
                        connectorBluetooth.onConnectBTDevice();
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
        if (debug) Log.i(TAG,"onCreateDialogConnected");
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
        connectorBluetooth.updateBroadcastReceiveData();
    }

    // в случае засыпания активности сбрасываем регистрацию BroadcastReceiver
    @Override
    protected void onPause() {
        super.onPause();
        connectorBluetooth.unregisterReceiver();
        caller.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // в случае отключения программы отвязываем сервис и отключаем его
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connectorBluetooth.mServiceConnection);
        connectorBluetooth.closeLeService();
        caller.stop();
    }

    // создаём меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // в меню окна вызова указываем флажок подключенного устройства
        if (getVisiblityMain()) {
            getMenuInflater().inflate(R.menu.gatt_services, menu);
            if (connectorBluetooth.ismConnected()) {
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_refresh).setVisible(true);
            } else {
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_refresh).setVisible(false);
            }
        }else {
            // в меню окна поиска устанавливаем кнопку сканирования
            getMenuInflater().inflate(R.menu.main, menu);
            if (!connectorBluetooth.ismScanning()) {
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
                connectorBluetooth.clearAllDevicesFromList();
                connectorBluetooth.addConnectDeviceToList();
                connectorBluetooth.startScanBluetoothDevice();
                break;
            case R.id.menu_stop:
                connectorBluetooth.stopScanBluetoothDevice();
                break;
        }
        return true;
    }

    //--------------------- Network Buttons

    private void btnSetDisabled(Button button, String label) {
        button.setEnabled(false);
        btnSetColorLabel(button, label, GRAY);
    }

    private void btnSetEnabled(Button button, String label) {
        button.setEnabled(true);
        int color;
        if (button == btnGreen) {
            color = GREEN;
        } else if (button == btnRed) {
            color = RED;
        } else {
            color = GRAY;
            if (debug) Log.e(TAG, "btnSetEnabled color not defined");
        }
        btnSetColorLabel(button, label, color);
    }

    private void btnSetColorLabel(Button button, String label, int color) {
        button.setText(label);
        button.setBackgroundColor(color);
    }

    private void callAnimStart() {
        if (debug && !isCallAnim) Log.i(TAG, "callAnimStart");
        btnGreen.startAnimation(animCall);
        isCallAnim = true;
    }

    private void callAnimStop() {
        if (debug) Log.i(TAG, "callAnimStop");
        btnGreen.clearAnimation();
        isCallAnim = false;
    }

    //--------------------- ICallNetworkListener

    @Override
    public void callFailed() {
        if (debug) Log.i(TAG, "callFailed");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "FAIL");
    }

    @Override
    public void callEndedExternally() {
        if (debug) Log.i(TAG, "callEndedExternally");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "ENDED");
    }

    @Override
    public void callOutcomingConnected() {
        if (debug) Log.i(TAG, "callOutcomingConnected");
        btnSetEnabled(btnGreen, "CALLING...");
        btnSetEnabled(btnRed, "CANCEL");
    }

    @Override
    public void callOutcomingAccepted() {
        if (debug) Log.i(TAG, "callOutcomingAccepted");
        btnSetDisabled(btnGreen, "ON CALL");
        btnSetEnabled(btnRed, "END CALL");
        callAnimStop();
    }

    @Override
    public void callOutcomingRejected() {
        if (debug) Log.i(TAG, "callOutcomingRejected");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "BUSY");
        callAnimStop();
    }

    @Override
    public void callOutcomingFailed() {
        if (debug) Log.i(TAG, "callOutcomingFailed");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "OFFLINE");
        callAnimStop();
    }

    @Override
    public void callOutcomingLocal() {
        if (debug) Log.i(TAG, "callOutcomingLocal");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "LOCAL");
    }

    @Override
    public void callIncomingDetected() {
        if (debug) Log.i(TAG, "callIncomingDetected");
        btnSetEnabled(btnGreen, "INCOMING...");
        btnSetEnabled(btnRed, "REJECT");
        callAnimStart();
    }

    @Override
    public void callIncomingCanceled() {
        if (debug) Log.i(TAG, "callIncomingCanceled");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "CANCELED");
        callAnimStop();
    }

    @Override
    public void callIncomingFailed() {
        if (debug) Log.i(TAG, "callIncomingFailed");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "INCOME FAIL");
        callAnimStop();
    }

    @Override
    public void connectorFailure() {
        if (debug) Log.e(TAG, "connectorFailure");
        btnSetDisabled(btnGreen, "ERROR");
        btnSetDisabled(btnRed, "ERROR");
    }

    @Override
    public void connectorReady() {
        if (debug) Log.i(TAG, "connectorReady");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "IDLE");
    }

    //--------------------- ICallUiListener

    @Override
    public void callOutcomingStarted() {
        if (debug) Log.i(TAG, "callOutcomingStarted");
        btnSetDisabled(btnGreen, "CALLING...");
        btnSetEnabled(btnRed, "CANCEL");
        callAnimStart();
    }

    @Override
    public void callEndedInternally() {
        if (debug) Log.i(TAG, "callEndedInternally");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "ENDED");
    }

    @Override
    public void callOutcomingCanceled() {
        if (debug) Log.i(TAG, "callOutcomingCanceled");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "CANCELED");
        callAnimStop();
    }

    @Override
    public void callIncomingRejected() {
        if (debug) Log.i(TAG, "callIncomingRejected");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "REJECTED");
        callAnimStop();
    }

    @Override
    public void callIncomingAccepted() {
        if (debug) Log.i(TAG, "callIncomingAccepted");
        btnSetDisabled(btnGreen, "ON CALL");
        btnSetEnabled(btnRed, "END CALL");
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

    //--------------------- debug

    private CallerState getCallerState() {
        return Caller.getInstance().getCallerState();
    }

    private String getCallerStateName() {
        return Caller.getInstance().getCallerState().getName();
    }

    @Override
    public void startDebug() {
        switch (debugMode) {
            case LoopbackBtToBt:
                btnSetDisabled(btnGreen, "LBACK ON");
                btnSetEnabled(btnRed, "LBACK OFF");
                break;
            case Record:
                switch (getCallerState()) {
                    case DebugPlay:
                        btnSetDisabled(btnGreen, "PLAYING");
                        btnSetEnabled(btnRed, "STOP");
                        break;
                    case DebugRecord:
                        btnSetDisabled(btnGreen, "RECORDING");
                        btnSetEnabled(btnRed, "STOP");
                        break;
                    default:
                        if (debug) Log.e(TAG, "startDebug " + getCallerStateName());
                        break;
                }
                break;
            case LoopbackNetToNet:
                break;
            case Normal:
                break;
            default:
                break;
        }
    }

    @Override
    public void stopDebug() {
        switch (debugMode) {
            case LoopbackBtToBt:
                btnSetEnabled(btnGreen, "LBACK ON");
                btnSetDisabled(btnRed, "LBACK OFF");
                break;
            case Record:
                btnSetEnabled(btnGreen, "PLAY");
                btnSetDisabled(btnRed, "RECORDED");
                break;
            case LoopbackNetToNet:
                break;
            case Normal:
                break;
            default:
                break;
        }
    }
}
