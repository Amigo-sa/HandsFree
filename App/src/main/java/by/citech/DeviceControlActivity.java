package by.citech;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.bluetoothlegatt.LeDeviceListAdapter;
import by.citech.contact.ActiveContactState;
import by.citech.contact.Contact;
import by.citech.contact.ContactState;
import by.citech.contact.Contactor;
import by.citech.contact.ContactsRecyclerAdapter;
import by.citech.contact.EditorState;
import by.citech.contact.IContactsListener;
import by.citech.dialog.DialogProcessor;
import by.citech.dialog.DialogState;
import by.citech.dialog.DialogType;
import by.citech.element.IElementAdd;
import by.citech.element.IElementDel;
import by.citech.element.IElementUpd;
import by.citech.logic.Caller;
import by.citech.logic.ConnectorBluetooth;
import by.citech.logic.IBluetoothListener;
import by.citech.logic.ICallNetListener;
import by.citech.gui.ICallUiListener;
import by.citech.debug.IDebugListener;
import by.citech.gui.IUiBtnGreenRedListener;
import by.citech.network.INetInfoListener;
import by.citech.network.INetListener;
import by.citech.logic.CallerState;
import by.citech.param.Colors;
import by.citech.param.DebugMode;
import by.citech.param.Settings;
import by.citech.param.Tags;
import by.citech.util.Buttons;
import by.citech.util.Contacts;
import by.citech.util.Keyboard;

import static by.citech.util.NetworkInfo.getIpAddr;

public class DeviceControlActivity
        extends AppCompatActivity
        implements INetInfoListener,
        ICallNetListener,
        ICallUiListener,
        IBluetoothListener,
        IDebugListener,
        IContactsListener,
        LocationListener {

    private static final String TAG = Tags.ACT_DEVICECTRL;
    private static final boolean debug = Settings.debug;
    private static final DebugMode debugMode = Settings.debugMode;
    public static final int REQUEST_LOCATION = 99;
    public static final int REQUEST_MICROPHONE = 98;

    private IUiBtnGreenRedListener iUiBtnGreenRedListener;
    private INetListener iNetworkListener;

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
    // TODO: отображение траффика для дебага
    TextView textViewBtInTraffic, textViewBtOutTraffic, textViewNetInTraffic, textViewNetOutTraffic;

    // условие повторения анимации
    private boolean isCallAnim = false;

    // список найденных устройств
    private ListView myListDevices;
    private LinearLayout MainView;
    private LinearLayout ScanView;
    private FrameLayout FrameView;
    private TextView AvailDevice;

    private Intent gattServiceIntent;
    private AlertDialog alertDialog;
    private boolean visiblityMain = true;

    private Caller caller;
    private ConnectorBluetooth connectorBluetooth;
    // ддя списка контактов
    private DialogProcessor dialogProcessor;

    private View viewContacts, viewContactEditor, viewChosenContact;
    private IElementAdd<Contact> iContactAdd;
    private IElementDel<Contact> iContactDel;
    private IElementUpd<Contact> iContactUpd;
    private RecyclerView viewRecyclerContacts;
    private EditText editTextSearch, editTextContactName, editTextContactIp;
    private TextView textViewChosenContactName, textViewChosenContactIp;
    private ContactsRecyclerAdapter contactsAdapter;
    private Contactor contactor;
    private ImageButton btnClearContact;
    private Button btnDelContact, btnSaveContact, btnCancelContact;
    ContactsRecyclerAdapter.SwipeCrutch swipeCrutch;
    ContactEditorHelper contactEditorHelper;
    ActiveContactHelper activeContactHelper;
    ChosenContactHelper chosenContactHelper;
    ViewHelper viewHelper;
    Buttons buttons;
    // для включения разрешения местоположения
    private LocationManager locationManager;
    private String provider;

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (debug) Log.i(TAG, "onStart");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (debug) Log.i(TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (debug) Log.i(TAG, "onStart");
        // Для проверки разрешения местоположения
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);
        setContentView(R.layout.device_control_activity);

        // Проверяем поддерживается ли технология Bluetooth Le
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG,"ble not supported");
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        caller = Caller.getInstance()
                .setiCallUiListener(this)
                .setiDebugListener(this)
                .setiCallNetworkListener(this)
                .setiNetInfoListener(this)
                .setiBluetoothListener(this);

        connectorBluetooth = caller.getConnectorBluetooth();
        iUiBtnGreenRedListener = caller.getiUiBtnGreenRedListener();
        iNetworkListener = caller.getiNetworkListener();
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        // And Checks if Bluetooth is supported on the device.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        // Проверяем поддерживается ли технология Bluetooth
        if (!connectorBluetooth.getBluetoothAdapter(bluetoothManager)) {
            Log.e(TAG,"Bluetooth not supported");
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Включаем Bluetooth
        if (!connectorBluetooth.getBTAdapter().isEnabled()){
            Log.e(TAG,"Bluetooth is Disable");
            connectorBluetooth.getBTAdapter().enable();
            Toast.makeText(getApplicationContext(), "Bluetooth now enabling", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth already enabled", Toast.LENGTH_LONG).show();
        }

        MainView = findViewById(R.id.MainView);
        ScanView = findViewById(R.id.ScanList);
        FrameView = findViewById(R.id.MainFrame);

        viewContacts = findViewById(R.id.viewContacts);
        viewContactEditor = findViewById(R.id.viewContactEditor);
        viewChosenContact = findViewById(R.id.viewContactChosen);
        viewRecyclerContacts = findViewById(R.id.viewRecycler);
        btnDelContact = findViewById(R.id.btnDelContact);
        btnSaveContact = findViewById(R.id.btnSaveContact);
        btnCancelContact = findViewById(R.id.btnCancelContact);
        btnClearContact = findViewById(R.id.btnClearContact);
        textViewChosenContactName = findViewById(R.id.textViewChosenContactName);
        textViewChosenContactIp = findViewById(R.id.textViewChosenContactIp);
        editTextSearch = findViewById(R.id.editTextSearch);
        editTextContactName = findViewById(R.id.editTextContactName);
        editTextContactIp = findViewById(R.id.editTextContactIp);

        btnClearContact.setOnClickListener((v) -> btnClearContact());
        findViewById(R.id.btnAddContact).setOnClickListener((v) -> btnAddContact());
        btnDelContact.setOnClickListener((v) -> btnDelContact());
        btnSaveContact.setOnClickListener((v) -> btnSaveContact());
        btnCancelContact.setOnClickListener((v) -> btnCancelContact());

        buttons = Buttons.getInstance();
        viewHelper = this.new ViewHelper();
        chosenContactHelper = this.new ChosenContactHelper();
        activeContactHelper = this.new ActiveContactHelper();

        setupContactor();
        setupContactEditor();
        setupDialogConstructor();
        setupViewRecyclerContacts();
        contactor.start(DeviceControlActivity.this, DeviceControlActivity.this);

        btnChangeDevice = findViewById(R.id.btnChangeHandsFree);
        btnChangeDevice.setText(R.string.connect_device);
        btnChangeDevice.setBackgroundColor(DARKCYAN);

        btnGreen = findViewById(R.id.btnGreen);
        btnRed = findViewById(R.id.btnRed);
        animCall = AnimationUtils.loadAnimation(this, R.anim.anim_call);

        switch (debugMode) {
            case MicToAudio:
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

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(String.format(Locale.US, "SecTel %s:%d",
                getIpAddr(Settings.ipv4),
                Settings.serverLocalPortNumber));

//      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // инициализируем сервис
        gattServiceIntent = new Intent(this, BluetoothLeService.class);

        // привязываем сервис
        bindService(gattServiceIntent, caller.getServiceConnection(), BIND_AUTO_CREATE);

        btnGreen.setOnClickListener((v) -> iUiBtnGreenRedListener.onClickBtnGreen());
        btnRed.setOnClickListener((v) -> iUiBtnGreenRedListener.onClickBtnRed());
        animCall.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {if (isCallAnim) {callAnimStart();}}
            @Override public void onAnimationRepeat(Animation animation) {}
        });

        caller.build();
    }

    //-------------------------- base

    @Override
    protected void onResume() {
        super.onResume();
        if (Settings.debug) Log.i(TAG,"onResume");
        enPermissions();
        if (connectorBluetooth != null) {
            connectorBluetooth.updateBCRData();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (debug) Log.i(TAG, "onPostCreate");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (debug) Log.i(TAG, "onPostResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (debug) Log.i(TAG, "onPause");
        connectorBluetooth.unregisterReceiver();
        caller.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (debug) Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (debug) Log.i(TAG, "onDestroy");
        unbindService(connectorBluetooth.mServiceConnection);
        connectorBluetooth.closeLeService();
        caller.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!viewHelper.isMainViewHidden())
            onCreateConnectMenu(menu);
        else
            onCreateScanMenu(menu);
        return true;
    }

    private void onCreateConnectMenu(Menu menu){
        if (debug) Log.i(TAG, "onCreateConnectMenu()");
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        getSupportActionBar().setCustomView(null);
    }

    private void onCreateScanMenu(Menu menu){
        if (Settings.debug) Log.i(TAG, "onCreateScanMenu()");
        getMenuInflater().inflate(R.menu.main, menu);
        if (!connectorBluetooth.ismScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            getSupportActionBar().setCustomView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            getSupportActionBar().setCustomView(R.layout.actionbar);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                connectorBluetooth.scanWork();
                break;
            case R.id.menu_stop:
                connectorBluetooth.stopScanBTDevice();
                break;
            case R.id.menu_settings:
                if (debug) Log.i(TAG, "menu settings");
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (debug) Log.i(TAG, "onBackPressed");
        Keyboard.hideSoftKeyboard(this);
        if (viewHelper.isMainViewHidden()) {
            if (debug) Log.i(TAG, "onBackPressed get main visible");
            viewHelper.showMainView();
            if (contactEditorHelper.getState() != EditorState.Inactive)
                contactEditorHelper.goToState(EditorState.Inactive);
            getSupportActionBar().setCustomView(null);
            if (connectorBluetooth != null) {
                connectorBluetooth.stopScanBTDevice();
            }
            invalidateOptionsMenu();
        } else
            super.onBackPressed();
    }

    //------------------------------ Разрешения местоположения --------------

    private boolean checkPermission(String permission, int requestPermission){
        if (Settings.debug) Log.i(TAG, "checkPermission()");
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestPermission);
            return false;
        }
        return true;
    }

    private void enPermission(String permission){
        if (Settings.debug) Log.i(TAG, "enPermission()");
        // permission was granted, yay! Do the
        // location-related task you need to do.
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            //Request location updates:
            if (provider != null)
                locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
    }

    private void enPermissions(){
        if (Settings.debug) Log.i(TAG, "enPermissions()");
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION))
            enPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (checkPermission(Manifest.permission.RECORD_AUDIO, REQUEST_MICROPHONE))
            enPermission(Manifest.permission.RECORD_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    enPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                return;
            }
            case REQUEST_MICROPHONE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    enPermission(Manifest.permission.RECORD_AUDIO);
                return;
            }
        }
    }

    //--------------------- setup

    private void setupContactor() {
        if (debug) Log.i(TAG, "setupContactor");
        editTextSearch.setHintTextColor(Colors.GRAY);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable arg0) {contactsAdapter.filter(editTextSearch.getText().toString());}
            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });
        contactor = Contactor.getInstance();
        iContactUpd = contactor;
        iContactDel = contactor;
        iContactAdd = contactor;
    }

    private void setupContactEditor() {
        if (debug) Log.i(TAG, "setupContactEditor");
        contactEditorHelper = this.new ContactEditorHelper();
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void afterTextChanged(Editable arg0) {}
            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {contactEditorHelper.contactFieldChanged();}
        };
        editTextContactIp.addTextChangedListener(textWatcher);
        editTextContactName.addTextChangedListener(textWatcher);
    }

    private void setupDialogConstructor() {
        if (debug) Log.i(TAG, "setupDialogConstructor");
        dialogProcessor = new DialogProcessor(this);
    }

    private void setupViewRecyclerContacts() {
        if (debug) Log.i(TAG, "setupViewRecyclerContacts");
        contactsAdapter = new ContactsRecyclerAdapter(contactor.getContacts());
        contactsAdapter.setOnClickViewListener(this::tapChooseContact);
        swipeCrutch = contactsAdapter.new SwipeCrutch();
        viewRecyclerContacts.setHasFixedSize(false);
        viewRecyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        viewRecyclerContacts.setAdapter(contactsAdapter);
        viewRecyclerContacts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
        itemTouchHelper.attachToRecyclerView(viewRecyclerContacts);
    }

    private ItemTouchHelper.Callback createHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {return false;}
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (debug) Log.i(TAG, "onSwiped");
                int position = viewHolder.getAdapterPosition();
                swipeCrutch.designateSwipe(viewHolder.itemView, position);
                switch (swipeDir) {
                    case ItemTouchHelper.RIGHT:
                        contactEditorHelper.setSwipedIn();
                        contactEditorHelper.startEditorEditContact(contactsAdapter.getItem(position), position);
                        break;
                    default:
                        if (debug) Log.i(TAG, "swipe swipeDir is " + swipeDir);
                        break;
                }
            }
        };
    }

    //--------------------- Actions

    void btnCancelContact() {
        if (debug) Log.i(TAG, "btnCancelContact");
        contactEditorHelper.cancelContact();
    }

    void btnSaveContact() {
        if (debug) Log.i(TAG, "btnSaveContact");
        contactEditorHelper.saveContact();
    }

    void btnDelContact() {
        if (debug) Log.i(TAG, "btnDelContact");
        contactEditorHelper.deleteContact();
    }

    void btnAddContact() {
        if (debug) Log.i(TAG, "btnAddContact");
        contactEditorHelper.startEditorAddContact();
    }

    void btnClearContact() {
        if (debug) Log.i(TAG, "btnClearContact");
        if (chosenContactHelper.isChosen())
            chosenContactHelper.clear();
        else
            editTextSearch.setText("");
    }

    private void tapChooseContact(Contact contact, int position) {
        if (debug) Log.i(TAG, "tapChooseContact");
        chosenContactHelper.choose(contact, position);
        activeContactHelper.goToState(ActiveContactState.FromChosen);
    }

    public void btnChangeDevice(View view){
        setVisibleList();
        myListDevices = findViewById(R.id.ListDevices);
        AvailDevice = findViewById(R.id.AvailDevices);
        connectorBluetooth.initListBTDevice();
        myListDevices.setAdapter(connectorBluetooth.getmLeDeviceListAdapter());
        myListDevices.setOnTouchListener(new LinearLayoutTouchListener());
        FrameView.setOnTouchListener(new LinearLayoutTouchListener());
        Log.i("WSD_ACTIVITY","befor caller getBluetoothAdapter");
        // При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
// останавливаем сканирование и запускаем новое Activity
        myListDevices.setOnItemClickListener((parent, view1, position, id) -> {
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
        });
        connectorBluetooth.stopScanBTDevice();
        connectorBluetooth.startScanBTDevices();
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
                        .setNegativeButton(R.string.cancel, (dialog, identifier) -> {
                            connectorBluetooth.disconnectBTDevice();
                            dialog.cancel();
                        });
                alertDialog = adb.create();
                break;
            }
            case THIS_CONNECTED_DEVICE: {
                adb.setTitle(device.getName());
                adb.setMessage(R.string.click_connected_device_message);
                adb.setIcon(android.R.drawable.ic_dialog_info);
                adb.setPositiveButton(R.string.disconnect, (dialog, which) -> {
                    connectorBluetooth.disconnectWork();
                    dialog.dismiss();
                });
                adb.setNegativeButton(R.string.cancel, (dialog, identifier) -> dialog.dismiss());
                alertDialog = adb.create();
                break;
            }
            case OTHER_CONNECTED_DEVICE: {
                adb.setTitle(device.getName());
                adb.setMessage(R.string.click_other_device_message);
                adb.setIcon(android.R.drawable.ic_dialog_info);
                adb.setPositiveButton(R.string.connect, (dialog, which) -> {
                    connectorBluetooth.disconnectBTDevice();
                    connectorBluetooth.onConnectBTDevice();
                    dialog.dismiss();
                });
                adb.setNegativeButton(R.string.cancel, (dialog, identifier) -> dialog.dismiss());
                alertDialog = adb.create();
                break;
            }
        }
        alertDialog.show();
    }

    //Окно когда устройство успешно подключилось
    private void onCreateDialogIsConnected(BluetoothDevice device) {
        if (alertDialog != null)
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
        if (alertDialog != null)
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

    //--------------------- ICallNetListener

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
    public void callOutcomingInvalid() {
        if (debug) Log.i(TAG, "callOutcomingInvalid");
        btnSetEnabled(btnGreen, "CALL");
        btnSetDisabled(btnRed, "INVALID");
        callAnimStop();
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

    //--------------------- INetInfoListener

    @Override
    public String getRemAddr() {
        return activeContactHelper.getIp();
    }

    @Override
    public String getRemPort() {
        return Integer.toString(Settings.serverRemotePortNumber);
    }

    @Override
    public String getLocPort() {
        return Integer.toString(Settings.serverLocalPortNumber);
    }

    //--------------------- IBluetoothListener

    @Override
    public void changeOptionMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public void addDeviceToList(final LeDeviceListAdapter leDeviceListAdapter, final BluetoothDevice device, final int rssi) {
        runOnUiThread(() -> {
            leDeviceListAdapter.addDevice(device, rssi);
            leDeviceListAdapter.notifyDataSetChanged();
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
        changeMainGUI(DARKCYAN, R.string.connect_device);
        onCreateDialogIsDisconnected(bluetoothDevice);
    }

    @Override
    public void connectDialogInfo(BluetoothDevice bluetoothDevice) {
        changeMainGUI(DARKKHAKI, R.string.change_device);
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

    @Override
    public void onLocationChanged(Location location) {
        if (Settings.debug) Log.i(TAG, "onLocationChanged");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        if (Settings.debug) Log.i(TAG, "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String s) {
        if (Settings.debug) Log.i(TAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        if (Settings.debug) Log.i(TAG, "onProviderDisabled");
    }

    private void swipeScanStart(){
        if (!viewHelper.isScanViewHidden()) {
            connectorBluetooth.stopScanBTDevice();
            connectorBluetooth.scanWork();
        }
    }

    private void swipeScanStop(){
        if (!viewHelper.isScanViewHidden()) {
            connectorBluetooth.stopScanBTDevice();
        }
    }

    public class LinearLayoutTouchListener implements View.OnTouchListener {

            static final String logTag = "ActivitySwipeDetector";
            static final int MIN_DISTANCE = 100;// TODO change this runtime based on screen resolution. for 1920x1080 is to small the 100 distance
            private float downX, downY, upX, upY;

            public LinearLayoutTouchListener() {}
            public void onRightToLeftSwipe() {Log.i(logTag, "RightToLeftSwipe!");}
            public void onLeftToRightSwipe() {Log.i(logTag, "LeftToRightSwipe!");}

            public void onTopToBottomSwipe() {
                Log.i(logTag, "onTopToBottomSwipe!");
                    swipeScanStart();
            }

            public void onBottomToTopSwipe() {
                Log.i(logTag, "onBottomToTopSwipe!");
                swipeScanStop();
            }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = motionEvent.getX();
                    downY = motionEvent.getY();
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    upX = motionEvent.getX();
                    upY = motionEvent.getY();

                    float deltaX = downX - upX;
                    float deltaY = downY - upY;

                    // swipe horizontal?
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        // left or right
                        if (deltaX < 0) {
                            this.onLeftToRightSwipe();
                            return true;
                        }
                        if (deltaX > 0) {
                            this.onRightToLeftSwipe();
                            return true;
                        }
                    } else {
                        Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long horizontally, need at least " + MIN_DISTANCE);
                        // return false; // We don't consume the event
                    }

                    // swipe vertical?
                    if (Math.abs(deltaY) > MIN_DISTANCE) {
                        // top or down
                        if (deltaY < 0) {
                            this.onTopToBottomSwipe();
                            return true;
                        }
                        if (deltaY > 0) {
                            this.onBottomToTopSwipe();
                            return true;
                        }
                    } else {
                        Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long vertically, need at least " + MIN_DISTANCE);
                        // return false; // We don't consume the event
                    }

                    return false; // no swipe horizontally and no swipe vertically
                }// case MotionEvent.ACTION_UP:
            }
            return false;
        }

    }

    //--------------------- ViewHelper

    private boolean getVisiblityMain(){
        return visiblityMain;
    }

    private void setVisiblityMain(boolean visiblityMain){
        this.visiblityMain = visiblityMain;
    }

    private void setVisibleMain() {
        runOnUiThread(() -> {
            invalidateOptionsMenu();
            setVisiblityMain(true);
            MainView.setVisibility(View.VISIBLE);
            ScanView.setVisibility(View.GONE);
        });
    }

    private void setVisibleList() {
        runOnUiThread(() -> {
            invalidateOptionsMenu();
            setVisiblityMain(false);
            viewHelper.showScaner();
        });
    }

    private void setVisibleContact() {
        runOnUiThread(() -> {
            invalidateOptionsMenu();
            setVisiblityMain(false);
            MainView.setVisibility(View.GONE);
        });
    }

    private void changeMainGUI(int color, int buttonName){
        btnChangeDevice.setText(buttonName);
        btnChangeDevice.setBackgroundColor(color);
        invalidateOptionsMenu();
    }

    private class ViewHelper {

        private static final boolean debug = true;
        private static final String TAG = "WSD_ViewHelper";

        boolean isMainViewHidden() {return (MainView.getVisibility() != View.VISIBLE);}
        boolean isScanViewHidden() {return (ScanView.getVisibility() != View.VISIBLE);}

        void showMainView() {
            MainView.setVisibility(View.VISIBLE);
            viewContactEditor.setVisibility(View.GONE);
            ScanView.setVisibility(View.GONE);
        }

        void showEditor() {
            MainView.setVisibility(View.GONE);
            viewContactEditor.setVisibility(View.VISIBLE);
        }

        void showScaner() {
            MainView.setVisibility(View.GONE);
            ScanView.setVisibility(View.VISIBLE);
        }

        void hideEditor() {
            MainView.setVisibility(View.VISIBLE);
            viewContactEditor.setVisibility(View.GONE);
        }

        void showChosen() {
            viewChosenContact.setVisibility(View.VISIBLE);
            editTextSearch.setVisibility(View.GONE);
        }

        void hideChosen() {
            viewChosenContact.setVisibility(View.GONE);
            editTextSearch.setVisibility(View.VISIBLE);
        }

    }

    //--------------------- ContactEditorHelper

    private class ContactEditorHelper {

        private static final boolean debug = true;
        private static final String TAG = "WSD_ContactEditorHelper";

        Contact contactToEdit, contactToAdd;
        int contactToEditPosition, contactToDeletePosition;
        boolean isEditPending, isAddPending, isDeletePending, isEdited, isAdded, isDeleted, isSwipedIn;
        EditorState editorState;

        ContactEditorHelper() {
            contactToEditPosition = -1;
            contactToDeletePosition = -1;
            editorState = EditorState.Inactive;
        }

        void setSwipedIn() {isSwipedIn = true;}
        EditorState getState() {return editorState;}
        boolean isUpdPending(Contact contact) {return ((contactToEdit == contact) && isEditPending);}
        boolean isAddPending(Contact contact) {return ((contactToAdd == contact) && isAddPending);}
        boolean isDelPending(Contact contact) {return ((contactToEdit == contact) && isDeletePending);}

        void startEditorEditContact(Contact contact, int position) {
            if (debug) Log.i(TAG, "startEditorEditContact");
            contactEditorHelper.goToState(EditorState.Edit, contact, position);
        }

        void startEditorAddContact() {
            if (debug) Log.i(TAG, "startEditorAddContact");
            contactEditorHelper.goToState(EditorState.Add);
        }

        void goToState(EditorState toState) {
            if (toState != EditorState.Edit)
                goToState(toState, null, -1);
            else
                Log.e(TAG, "goToState editorState illegal");
        }

        void goToState(EditorState toState, Contact contact, int position) {
            if (debug) Log.i(TAG, "ContactEditorHelper goToState");
            editorState = toState;
            switch (editorState) {
                case Add:
                    btnSaveContact.setText("ADD");
                    Contacts.setContactInfo(editTextContactName, editTextContactIp);
                    btnDelContact.setVisibility(View.GONE);
                    Buttons.disable(btnDelContact, btnSaveContact, btnCancelContact);
                    break;
                case Edit:
                    contactToEdit = contact;
                    contactToEditPosition = position;
                    btnSaveContact.setText("SAVE");
                    Contacts.setContactInfo(contactToEdit, editTextContactName, editTextContactIp);
                    btnDelContact.setVisibility(View.VISIBLE);
                    Buttons.enable(btnDelContact);
                    Buttons.disable(btnSaveContact, btnCancelContact);
                    break;
                case Inactive:
                    contactToEdit = null;
                    contactToAdd = null;
                    contactToEditPosition = -1;
                    viewHelper.hideEditor();
                    if (isSwipedIn) {
                        if (debug) Log.i(TAG, "goToState Inactive isSwipedIn");
                        if (isDeleted || isEdited) {
                            swipeCrutch.resetSwipe();
                        } else {
                            swipeCrutch.resolveSwipe();
                        }
                        isSwipedIn = false;
                    }
                    isDeletePending = false;
                    isAddPending = false;
                    isEditPending = false;
                    isDeleted = false;
                    isAdded = false;
                    isEdited = false;
                    activeContactHelper.goToState(ActiveContactState.Default);
                    return;
                default:
                    Log.e(TAG, "goToState editorState default");
                    return;
            }
            viewHelper.showEditor();
            activeContactHelper.goToState(ActiveContactState.FromEditor);
        }

        void cancelContact() {
            if (debug) Log.i(TAG, "cancelContact");
            switch(editorState) {
                case Edit:
                    goToState(EditorState.Edit, contactToEdit, contactToEditPosition);
                    break;
                case Add:
                    goToState(EditorState.Add);
                    break;
                case Inactive:
                    Log.e(TAG, "cancelContact editorState Inactive");
                    break;
                default:
                    Log.e(TAG, "cancelContact editorState default");
                    break;
            }
        }

        void deleteContact() {
            if (debug) Log.i(TAG, "tryToDeleteContact");
            isDeletePending = true;
            freezeState();
            contactToDeletePosition = contactToEditPosition;
            Map<DialogState, Runnable> map = new HashMap<>();
            map.put(DialogState.Proceed, () -> iContactDel.deleteElement(contactToEdit));
            map.put(DialogState.Cancel, this::releaseState);
            dialogProcessor.runDialog(DialogType.Delete, map);
        }

        void saveContact() {
            if (debug) Log.i(TAG, "saveContact");
            switch (editorState) {
                case Add:
                    freezeState();
                    isAddPending = true;
                    contactToAdd = activeContactHelper.getContact();
                    iContactAdd.addElement(contactToAdd);
                    break;
                case Edit:
                    freezeState();
                    isEditPending = true;
                    iContactUpd.updateElement(contactToEdit, activeContactHelper.getContact());
                    break;
                case Inactive:
                    Log.e(TAG, "saveContact editorState Inactive");
                    break;
                default:
                    Log.e(TAG, "saveContact editorState default");
                    break;
            }
        }

        void onContactDelSuccess() {
            if (debug) Log.i(TAG, "onContactDelSuccess");
            isDeletePending = false;
            isDeleted = true;
            contactsAdapter.notifyItemRemoved(contactToDeletePosition);
            contactToEdit = null;
            contactToDeletePosition = -1;
            goToState(EditorState.Add);
        }

        void onContactAddSucc(int position) {
            if (debug) Log.i(TAG, "onContactAddSucc");
            isAddPending = false;
            isAdded = true;
            contactToEditPosition = position;
            goToState(EditorState.Edit, contactToAdd, position);
            contactToAdd = null;
        }

        void onContactEditSucc(int position) {
            if (debug) Log.i(TAG, "onContactEditSucc");
            isEditPending = false;
            isEdited = true;
            contactToEditPosition = position;
            goToState(EditorState.Edit, contactToEdit, position);
        }

        void onContactAddFail() {
            if (debug) Log.i(TAG, "onContactAddFail");
            isAddPending = false;
            contactToAdd = null;
            releaseState();
        }

        void onContactEditFail() {
            if (debug) Log.i(TAG, "onContactEditFail");
            isEditPending = false;
            releaseState();
        }

        private void contactFieldChanged() {Buttons.enable(btnSaveContact, btnCancelContact);}

        private void freezeState() {
            if (debug) Log.i(TAG, "freezeState");
            buttons.freezeState(TAG, btnDelContact, btnSaveContact, btnCancelContact);
        }

        private void releaseState() {
            if (debug) Log.i(TAG, "releaseState");
            buttons.releaseState(TAG);
        }

    }

    //--------------------- IContactsListener

    @Override
    public void doCallbackOnContactsChange(final Contact... contacts) {
        if (debug) Log.i(TAG, "doCallbackOnContactsChange");
//      runOnUiThread(() -> {
                    Contact contact = contacts[0];
                    ContactState state = contact.getState();
                    Toast.makeText(DeviceControlActivity.this, state.getMessage(), Toast.LENGTH_SHORT).show();
                    if (contacts.length > 1) {
                        contactsAdapter.notifyDataSetChanged();
                    } else {
                        int position = contactsAdapter.getItemPosition(contact);
                        if (debug) Log.w(TAG, String.format(Locale.US,
                                "doCallbackOnContactsChange: state is %s, pos is %d, contact is %s",
                                state.getMessage(), position, contact.toString()));
                        switch (state) {
                            case SuccessAdd:
                                contactsAdapter.notifyItemInserted(position);
                                if (contactEditorHelper.isAddPending(contact))
                                    contactEditorHelper.onContactAddSucc(position);
                                break;
                            case SuccessUpdate:
                                contactsAdapter.notifyItemChanged(position);
                                if (contactEditorHelper.isUpdPending(contact))
                                    contactEditorHelper.onContactEditSucc(position);
                                break;
                            case FailInvalidContact:
                            case FailNotUniqueContact:
                                if (contactEditorHelper.isUpdPending(contact))
                                    contactEditorHelper.onContactEditFail();
                                if (contactEditorHelper.isAddPending(contact))
                                    contactEditorHelper.onContactAddFail();
                                break;
                            case SuccessDelete:
                                if (contactEditorHelper.isDelPending(contact))
                                    contactEditorHelper.onContactDelSuccess();
                                else
                                    contactsAdapter.notifyDataSetChanged();
                                break;
                            case FailToAdd:
                                if (contactEditorHelper.isAddPending(contact))
                                    contactEditorHelper.onContactAddFail();
                            case Null:
                                Log.e(TAG, "doCallbackOnContactsChange state Null");
                                break;
                            default:
                                Log.e(TAG, "doCallbackOnContactsChange state default");
                                break;
                        }
                    }
//              });
    }

    //--------------------- ChosenContactHelper

    private class ChosenContactHelper {

        private static final boolean debug = true;
        private static final String TAG = "WSD_ChosenContactHelper";

        boolean isChosen;
        Contact chosenContact;
        int chosenContactPosition;

        ChosenContactHelper() {
            chosenContactPosition = -1;
        }

        boolean isChosen() {return isChosen;}
        public Contact getContact() {return chosenContact;}

        void choose(Contact contact, int position) {
            isChosen = true;
            chosenContact = contact;
            chosenContactPosition = position;
            viewHelper.showChosen();
            Contacts.setContactInfo(chosenContact, textViewChosenContactName, textViewChosenContactIp);
        }

        void clear() {
            isChosen = false;
            chosenContact = null;
            chosenContactPosition = -1;
            viewHelper.hideChosen();
            activeContactHelper.goToState(ActiveContactState.Default);
            Contacts.setContactInfo(textViewChosenContactName, textViewChosenContactIp);
        }

    }

    //--------------------- ActiveContactHelper

    private class ActiveContactHelper {

        private static final boolean debug = true;
        private static final String TAG = "WSD_ActiveContactHelper";

        ActiveContactState activeContactState;

        ActiveContactHelper() {
            activeContactState = ActiveContactState.IpFromSearch;
        }

        Contact getContact() {
            switch (activeContactState) {
                case IpFromSearch:
                    if (debug) Log.i(TAG, "getContact IpFromSearch");
                case FromEditor:
                    if (debug) Log.i(TAG, "getContact FromEditor");
                    return new Contact(getName(), getIp());
                case FromChosen:
                    if (debug) Log.i(TAG, "getContact FromChosen");
                    return chosenContactHelper.getContact();
                case Null:
                    Log.e(TAG, "ActiveContactHelper getContact state null");
                    break;
                default:
                    Log.e(TAG, "ActiveContactHelper getContact state default");
                    break;
            }
            return null;
        }

        ActiveContactState getState() {return activeContactState;}

        void goToState(ActiveContactState toState) {
            if (debug) Log.i(TAG, "goToState");
            this.activeContactState = toState;
            switch (activeContactState) {
                case FromChosen:
                    break;
                case Null:
                    break;
                case Default:
                    if (chosenContactHelper.isChosen())
                        activeContactState = ActiveContactState.FromChosen;
                    else
                        activeContactState = ActiveContactState.IpFromSearch;
                    break;
                case IpFromSearch:
                    break;
                case FromEditor:
                    break;
                default:
                    Log.e(TAG, "goToState activeContactState default");
                    break;
            }
        }

        private String getName() {
            if (debug) Log.i(TAG, "getName");
            switch (activeContactHelper.getState()) {
                case FromChosen:
                    return activeContactHelper.getContact().getName();
                case FromEditor:
                    return editTextContactName.getText().toString();
                case IpFromSearch:
                    return "";
                default:
                    Log.e(TAG, "getName editorState default");
                    return "";
            }
        }

        private String getIp() {
            if (debug) Log.i(TAG, "getIp");
            switch (activeContactHelper.getState()) {
                case FromChosen:
                    if (debug) Log.i(TAG, "getIp FromChosen");
                    return activeContactHelper.getContact().getIp();
                case FromEditor:
                    if (debug) Log.i(TAG, "getIp FromEditor");
                    return editTextContactIp.getText().toString();
                case IpFromSearch:
                    if (debug) Log.i(TAG, "getIp IpFromSearch");
                    return editTextSearch.getText().toString();
                default:
                    Log.e(TAG, "getIp editorState default");
                    return "";
            }
        }

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
            case MicToAudio:
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
            case MicToAudio:
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
