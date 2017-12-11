package by.citech;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import by.citech.bluetoothlegatt.IReceive;
import by.citech.bluetoothlegatt.IVisible;
import by.citech.bluetoothlegatt.adapters.LeDeviceListAdapter;
import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.contact.ActiveContactState;
import by.citech.contact.Contact;
import by.citech.contact.Contactor;
import by.citech.contact.ContactsRecyclerAdapter;
import by.citech.contact.EditorState;
import by.citech.dialog.DialogProcessor;
import by.citech.dialog.DialogState;
import by.citech.dialog.DialogType;
import by.citech.exchange.IMsgToUi;
import by.citech.gui.ActiveContactHelper;
import by.citech.gui.ChosenContactHelper;
import by.citech.gui.ContactEditorHelper;
import by.citech.gui.IGetViewById;
import by.citech.gui.IGetViewGetter;
import by.citech.gui.ViewHelper;
import by.citech.logic.Caller;
import by.citech.logic.ConnectorBluetooth;
import by.citech.logic.IBase;
import by.citech.logic.IBaseAdder;
import by.citech.logic.IBluetoothListener;
import by.citech.gui.IUiBtnGreenRedListener;
import by.citech.network.INetInfoListener;
import by.citech.param.Colors;
import by.citech.param.OpMode;
import by.citech.param.PreferencesProcessor;
import by.citech.param.Settings;
import by.citech.param.Tags;
import by.citech.threading.CraftedThreadPool;
import by.citech.util.Keyboard;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;
import static by.citech.util.Network.getIpAddr;

public class DeviceControlActivity
        extends AppCompatActivity
        implements INetInfoListener, IBluetoothListener, LocationListener, IGetViewById,
        IMsgToUi, IBaseAdder, IReceive, IService, IVisible, IGetViewGetter {

    private static final String TAG = Tags.ACT_DEVICECTRL;
    private static final boolean debug = Settings.debug;
    private OpMode opMode;

    public static final int REQUEST_LOCATION = 99;
    public static final int REQUEST_MICROPHONE = 98;

    // цвета
    private static final int DARKCYAN = Colors.DARKCYAN;
    private static final int DARKKHAKI = Colors.DARKKHAKI;

    // основные элементы управления
    private Button btnChangeDevice;

    // TODO: отображение траффика для дебага
    private TextView textViewBtInTraffic, textViewBtOutTraffic, textViewNetInTraffic, textViewNetOutTraffic;

    private ActionBar actionBar;

    // список найденных устройств
    private ListView myListDevices;
    private LinearLayout mainView;
    private LinearLayout scanView;
    private FrameLayout baseView;

    private Intent gattServiceIntent;
    private boolean visiblityMain = true;

    // основная логика
    private IUiBtnGreenRedListener iUiBtnGreenRedListener;
    private ConnectorBluetooth connectorBluetooth;

    // ддя списка контактов
    private DialogProcessor dialogProcessor;
    private RecyclerView viewRecyclerContacts;
    private EditText editTextSearch, editTextContactName, editTextContactIp;
    private ContactsRecyclerAdapter contactsAdapter;
    private ContactsRecyclerAdapter.SwipeCrutch swipeCrutch;
    private ContactEditorHelper contactEditorHelper;
    private ActiveContactHelper activeContactHelper;
    private ChosenContactHelper chosenContactHelper;

    private ViewHelper viewHelper;
    private CraftedThreadPool threadPool;

    // для включения разрешения местоположения
    private LocationManager locationManager;
    private String provider;
    private List<IBase> iBaseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (debug) Log.w(TAG, "onCreate");
        PreferencesProcessor.process(this);
        opMode = Settings.opMode;
        if (debug) Log.i(TAG, "onCreate opMode is " + opMode.getSettingName());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (debug) Log.w(TAG, "onStart");
        iBaseList = new ArrayList<>();
        // Для проверки разрешения местоположения
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);
        setContentView(R.layout.activity_device_control);

        // Проверяем поддерживается ли технология Bluetooth Le
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG,"ble not supported");
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        baseView = findViewById(R.id.baseView);
        mainView = findViewById(R.id.mainView);
        scanView = findViewById(R.id.scanView);
        viewRecyclerContacts = findViewById(R.id.viewRecycler);
        editTextSearch = findViewById(R.id.editTextSearch);
        editTextContactName = findViewById(R.id.editTextContactName);
        editTextContactIp = findViewById(R.id.editTextContactIp);

        findViewById(R.id.btnClearContact).setOnClickListener((v) -> clickBtnClearContact());
        findViewById(R.id.btnAddContact).setOnClickListener((v) -> clickBtnAddContact());
        findViewById(R.id.btnDelContact).setOnClickListener((v) -> clickBtnDelContact());
        findViewById(R.id.btnSaveContact).setOnClickListener((v) -> clickBtnSaveContact());
        findViewById(R.id.btnCancelContact).setOnClickListener((v) -> clickBtnCancelContact());
        findViewById(R.id.btnGreen).setOnClickListener((v) -> iUiBtnGreenRedListener.onClickBtnGreen());
        findViewById(R.id.btnRed).setOnClickListener((v) -> iUiBtnGreenRedListener.onClickBtnRed());

        try {
            viewHelper = new ViewHelper(this, this);
            viewHelper.baseStart(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        chosenContactHelper = new ChosenContactHelper(viewHelper);
        activeContactHelper = new ActiveContactHelper(chosenContactHelper, viewHelper);

        Caller.getInstance()
                .setiCallUiListener(viewHelper)
                .setiDebugListener(viewHelper)
                .setiCallNetListener(viewHelper)
                .setiNetInfoListener(this)
                .setiBluetoothListener(this)
                .setiReceive(this)
                .setiService(this)
                .setiVisible(this);

        connectorBluetooth = Caller.getInstance().getConnectorBluetooth();
        iUiBtnGreenRedListener = Caller.getInstance().getiUiBtnGreenRedListener();

        // Initializes a Bluetooth adapter. For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager. Checks if Bluetooth is supported on the device.
        // Проверяем поддерживается ли технология Bluetooth
        if (!connectorBluetooth.getBluetoothAdapter((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))) {
            Log.e(TAG,"Bluetooth not supported");
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Включаем Bluetooth
        if (!connectorBluetooth.getBTAdapter().isEnabled()) {
            Log.e(TAG,"Bluetooth is disable");
            connectorBluetooth.getBTAdapter().enable();
            Toast.makeText(getApplicationContext(), "Bluetooth now enabling", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth already enabled", Toast.LENGTH_LONG).show();
        }

        dialogProcessor = new DialogProcessor(this);
        threadPool = new CraftedThreadPool(Settings.threadNumber);
        threadPool.baseStart(this);

        setupViewRecyclerContacts();
        setupContactEditor();
        setupContactor();

        btnChangeDevice = findViewById(R.id.btnChangeDevice);
        btnChangeDevice.setText(R.string.connect_device);
        btnChangeDevice.setBackgroundColor(DARKCYAN);

        setupActionBar();

//      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // инициализируем сервис
        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Contactor.getInstance().baseStart(this);
        threadPool.addRunnable(() -> Contactor.getInstance().getAllContacts());
        Caller.getInstance().baseStart(this);
    }

    //-------------------------- base

    @Override
    protected void onResume() {
        super.onResume();
        if (Settings.debug) Log.w(TAG,"onResume");
        enPermissions();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (debug) Log.w(TAG, "onPostCreate");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (debug) Log.w(TAG, "onPostResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (debug) Log.w(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (debug) Log.w(TAG, "onStop");
        if (iBaseList != null) {
            for (IBase iBase : iBaseList) {
                if (iBase != null) {
                    iBase.baseStop();
                }
            }
            iBaseList.clear();
            iBaseList = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (debug) Log.w(TAG, "onDestroy");
    }

    //-------------------------- menu

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
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        actionBar.setCustomView(null);
    }

    private void onCreateScanMenu(Menu menu){
        if (Settings.debug) Log.i(TAG, "onCreateScanMenu()");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (!connectorBluetooth.ismScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            actionBar.setCustomView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            actionBar.setCustomView(R.layout.actionbar);
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
                startActivity(new Intent(this, SettingsActivity.class));
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
            if (debug) Log.i(TAG, "onBackPressed get main_menu visible");
            viewHelper.showMainView();
            actionBar.setCustomView(null);
            if (contactEditorHelper.getState() != EditorState.Inactive)
                contactEditorHelper.goToState(EditorState.Inactive);
            if (connectorBluetooth != null)
                connectorBluetooth.stopScanBTDevice();
            invalidateOptionsMenu();
        } else {
            super.onBackPressed();
        }
    }

    //-------------------------- permissons

    private boolean checkPermission(String permission, int requestPermission){
        if (Settings.debug) Log.i(TAG, "checkPermission()");
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            //if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestPermission);
            return false;
        }
        return true;
    }

    private void enPermission(String permission){
        if (Settings.debug) Log.i(TAG, "enPermission()");
        // permission was granted, yay! Do the location-related task you need to do.
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    enPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                break;
            case REQUEST_MICROPHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    enPermission(Manifest.permission.RECORD_AUDIO);
                break;
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
        Contactor.getInstance()
                .setContext(this)
                .setiMsgToUi(this)
                .setListener(contactEditorHelper);
    }

    private void setupContactEditor() {
        if (debug) Log.i(TAG, "setupContactEditor");
        try {
            contactEditorHelper = new ContactEditorHelper(viewHelper, swipeCrutch, activeContactHelper,
                    this, threadPool, Contactor.getInstance(), contactsAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void afterTextChanged(Editable arg0) {}
            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {contactEditorHelper.contactFieldChanged();}
        };
        editTextContactIp.addTextChangedListener(textWatcher);
        editTextContactName.addTextChangedListener(textWatcher);
    }

    private void setupViewRecyclerContacts() {
        if (debug) Log.i(TAG, "setupViewRecyclerContacts");
        contactsAdapter = new ContactsRecyclerAdapter(Contactor.getInstance().getContacts());
        contactsAdapter.setOnClickViewListener(this::clickContactItem);
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

    private void setupActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            String title = String.format(Locale.US, "%s %s:%d %s",
                    getTitle().toString(),
                    getIpAddr(Settings.ipv4),
                    Settings.serverLocalPortNumber,
                    opMode.getSettingName()
            );
            SpannableString s = new SpannableString(title);
            if (title != null) {
                s.setSpan(new ForegroundColorSpan(Colors.WHITE), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                s.setSpan(new AbsoluteSizeSpan(56), 0, title.length(), SPAN_INCLUSIVE_INCLUSIVE);
                s.setSpan(new RelativeSizeSpan(0.7f), 7, title.length(), 0);
            }
            actionBar.setTitle(s);
        }
    }

    //--------------------- actions

    void clickBtnCancelContact() {
        if (debug) Log.i(TAG, "clickBtnCancelContact");
        contactEditorHelper.cancelContact();
    }

    void clickBtnSaveContact() {
        if (debug) Log.i(TAG, "clickBtnSaveContact");
        contactEditorHelper.saveContact();
    }

    void clickBtnDelContact() {
        if (debug) Log.i(TAG, "clickBtnDelContact");
        contactEditorHelper.deleteContact();
    }

    void clickBtnAddContact() {
        if (debug) Log.i(TAG, "clickBtnAddContact");
        contactEditorHelper.startEditorAddContact();
    }

    void clickBtnClearContact() {
        if (debug) Log.i(TAG, "clickBtnClearContact");
        if (chosenContactHelper.isChosen()) {
            chosenContactHelper.clear();
            activeContactHelper.goToState(ActiveContactState.Default);
        }
        else {
            editTextSearch.setText("");
        }
    }

    private void clickContactItem(Contact contact, int position) {
        if (debug) Log.i(TAG, "clickContactItem");
        chosenContactHelper.choose(contact, position);
        activeContactHelper.goToState(ActiveContactState.FromChosen);
    }

    public void clickBtnChangeDevice(View view) {
        setVisibleList();
        myListDevices = findViewById(R.id.ListDevices);
        connectorBluetooth.initListBTDevice();
        myListDevices.setAdapter(connectorBluetooth.getmLeDeviceListAdapter());
        myListDevices.setOnTouchListener(new LinearLayoutTouchListener());
        baseView.setOnTouchListener(new LinearLayoutTouchListener());
        Log.i("WSD_ACTIVITY","befor caller getBluetoothAdapter");
        // При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
// останавливаем сканирование и запускаем новое Activity
        myListDevices.setOnItemClickListener((parent, view1, position, id) -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            connectorBluetooth.clickItemList(position, adb);
        });
        connectorBluetooth.stopScanBTDevice();
        connectorBluetooth.startScanBTDevices();
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
    public void withoutDeviceView() {
        changeMainGUI(DARKCYAN, R.string.connect_device);
    }

    @Override
    public void withDeviceView() {
        changeMainGUI(DARKKHAKI, R.string.change_device);
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

    @Override
    public Intent getServiceIntent() {
        return gattServiceIntent;
    }

    public class LinearLayoutTouchListener
            implements View.OnTouchListener {

            static final String logTag = "ActivitySwipeDetector";
            // TODO change this runtime based on screen resolution. for 1920x1080 is to small the 100 distance
            static final int MIN_DISTANCE = 100;
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

    private void setVisiblityMain(boolean visiblityMain){
        this.visiblityMain = visiblityMain;
    }

    public void setVisibleMain() {
        runOnUiThread(() -> {
            invalidateOptionsMenu();
            setVisiblityMain(true);
            mainView.setVisibility(View.VISIBLE);
            scanView.setVisibility(View.GONE);
        });
    }

    public void setVisibleList() {
        runOnUiThread(() -> {
            invalidateOptionsMenu();
            setVisiblityMain(false);
            viewHelper.showScaner();
        });
    }

    private void changeMainGUI(int color, int buttonName){
        btnChangeDevice.setText(buttonName);
        btnChangeDevice.setBackgroundColor(color);
        invalidateOptionsMenu();
    }

    //--------------------- IBaseAdder

    @Override
    public void addBase(IBase iBase) {
        if (debug) Log.i(TAG, "addBase");
        if (iBaseList == null || iBase == null) {
            Log.e(TAG, "addBase iBaseList or iBase is null");
        } else {
            iBaseList.add(iBase);
        }
    }

    //--------------------- IGetViewGetter

    @Override
    public IGetViewById getViewGetter() {
        if (debug) Log.i(TAG, "getViewGetter");
        return this;
    }

    //--------------------- IMsgToUi

    @Override
    public void sendToUiToast(boolean isFromUiThread, String message) {
        if (debug) Log.i(TAG, "sendToUiToast");
        sendToUiRunnable(isFromUiThread, () -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void sendToUiDialog(boolean isFromUiThread, DialogType toRun, Map<DialogState, Runnable> toDoMap, String... messages) {
        if (debug) Log.i(TAG, "sendToUiDialog");
        sendToUiRunnable(isFromUiThread, () -> dialogProcessor.runDialog(toRun, toDoMap, messages));
    }

    @Override
    public void recallFromUiDialog(boolean isFromUiThread, DialogType toDeny) {
        if (debug) Log.i(TAG, "recallFromUiDialog");
        sendToUiRunnable(isFromUiThread, () -> dialogProcessor.denyDialog(toDeny));
    }

    @Override
    public void sendToUiRunnable(boolean isFromUiThread, Runnable toDo) {
        if (debug) Log.i(TAG, "sendToUiRunnable");
        if (isFromUiThread) {
            toDo.run();
        } else {
            runOnUiThread(toDo);
        }
    }

}
