package by.citech.handsfree;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;

import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.common.IService;
import by.citech.handsfree.common.ResourceManager;
import by.citech.handsfree.gui.IBtToUiCtrl;
import by.citech.handsfree.bluetoothlegatt.adapters.LeDeviceListAdapter;
import by.citech.handsfree.bluetoothlegatt.BluetoothLeService;
import by.citech.handsfree.gui.helper.IContactEditor;
import by.citech.handsfree.gui.helper.state.ActiveContactState;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.contact.Contactor;
import by.citech.handsfree.contact.ContactsRecyclerAdapter;
import by.citech.handsfree.gui.helper.state.EditorState;
import by.citech.handsfree.dialog.DialogProcessor;
import by.citech.handsfree.dialog.DialogState;
import by.citech.handsfree.dialog.DialogType;
import by.citech.handsfree.exchange.IMsgToUi;
import by.citech.handsfree.gui.helper.ActiveContactHelper;
import by.citech.handsfree.gui.helper.ChosenContactHelper;
import by.citech.handsfree.gui.helper.ContactEditorHelper;
import by.citech.handsfree.gui.IGetView;
import by.citech.handsfree.gui.IGetViewGetter;
import by.citech.handsfree.gui.IBtToUiListener;
import by.citech.handsfree.gui.IUiToBtListener;
import by.citech.handsfree.gui.helper.ViewManager;
import by.citech.handsfree.logic.Caller;
import by.citech.handsfree.logic.ConnectorBluetooth;
import by.citech.handsfree.logic.IBluetoothListener;
import by.citech.handsfree.gui.IUiToCallListener;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.param.Colors;
import by.citech.handsfree.settings.enumeration.OpMode;
import by.citech.handsfree.settings.PreferencesProcessor;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.threading.IThreadManager;
import by.citech.handsfree.threading.ThreadManager;
import by.citech.handsfree.util.Keyboard;

import static by.citech.handsfree.util.Network.getIpAddr;

public class DeviceControlActivity
        extends AppCompatActivity
        implements INetInfoGetter, IBluetoothListener, LocationListener, IGetView,
        IContactEditor, IBroadcastReceiver, IService, IBtToUiCtrl, IGetViewGetter,
        IThreadManager, IUiToCallListener, IMsgToUi {

    private static final String STAG = Tags.ACT_DEVICECTRL;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private OpMode opMode;

    public static final int REQUEST_LOCATION = 99;
    public static final int REQUEST_MICROPHONE = 98;

    // TODO: отображение траффика для дебага
    private TextView textViewBtInTraffic, textViewBtOutTraffic, textViewNetInTraffic, textViewNetOutTraffic;

    private ViewManager viewManager;
    private ActionBar actionBar;

    // список найденных устройств
    private ListView listDevices;
    private LeDeviceListAdapter deviceListAdapter;

    private Intent gattServiceIntent;

    // ддя списка контактов
    private DialogProcessor dialogProcessor;
    private RecyclerView viewRecyclerContacts;
    private EditText editTextSearch, editTextContactName, editTextContactIp;
    private ContactsRecyclerAdapter contactsAdapter;
    private ContactsRecyclerAdapter.SwipeCrutch swipeCrutch;
    private ActiveContactHelper activeContactHelper;
    private ChosenContactHelper chosenContactHelper;

    // для включения разрешения местоположения
    private LocationManager locationManager;
    private String provider;

    // интерфейсы для работы gui с bt
    private IUiToBtListener IUiToBtListener;
    private IBtToUiListener IBtToUiListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        if (debug) Log.w(TAG, "onCreate");

        PreferencesProcessor.process(this);
        opMode = Settings.opMode;
        if (debug) Log.w(TAG, "onCreate opMode is getSettingName " + opMode.getSettingName());

        viewManager = new ViewManager();
        viewManager.setiGetGetter(this);
        viewManager.setDefaultView();
        viewManager.baseCreate();
        ThreadManager.getInstance().baseCreate();

        listDevices = findViewById(R.id.listDevices);
        viewRecyclerContacts = findViewById(R.id.viewRecycler);
        editTextSearch = findViewById(R.id.editTextSearch);
        editTextContactName = findViewById(R.id.editTextContactName);
        editTextContactIp = findViewById(R.id.editTextContactIp);

        findViewById(R.id.btnChangeDevice).setOnClickListener((v) -> clickBtnChangeDevice());
        findViewById(R.id.baseView).setOnTouchListener(new LinearLayoutTouchListener());
        findViewById(R.id.btnClearContact).setOnClickListener((v) -> clickBtnClearContact());
        findViewById(R.id.btnAddContact).setOnClickListener((v) -> startEditorAddContact());
        findViewById(R.id.btnDelContact).setOnClickListener((v) -> deleteFromEditor());
        findViewById(R.id.btnSaveContact).setOnClickListener((v) -> saveInEditor());
        findViewById(R.id.btnCancelContact).setOnClickListener((v) -> cancelInEditor());
        findViewById(R.id.btnGreen).setOnClickListener((v) -> onClickBtnGreen());
        findViewById(R.id.btnRed).setOnClickListener((v) -> onClickBtnRed());

        setupActionBar();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (debug) Log.w(TAG, "onStart");

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG,"ble not supported");
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null || bluetoothManager.getAdapter() == null) {
            Log.e(TAG,"Bluetooth not supported");
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothManager.getAdapter().isEnabled()) {
            Log.e(TAG,"Bluetooth is disable");
            bluetoothManager.getAdapter().enable();
            Toast.makeText(getApplicationContext(), "Bluetooth now enabling", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth already enabled", Toast.LENGTH_LONG).show();
        }

        chosenContactHelper = new ChosenContactHelper(viewManager);
        activeContactHelper = new ActiveContactHelper(chosenContactHelper, viewManager);
        deviceListAdapter = new LeDeviceListAdapter(this.getLayoutInflater());

        Caller.getInstance()
                .setiCallToUiListener(viewManager)
                .setiDebugCtrl(viewManager)
                .setiCallNetListener(viewManager)
                .setiNetInfoGetter(this)
                .setiBluetoothListener(this)
                .setiBroadcastReceiver(this)
                .setiService(this)
                .setiBtToUiCtrl(this)
                .setiMsgToUi(this)
                .setiList(deviceListAdapter);

        IUiToBtListener = ConnectorBluetooth.getInstance().getUiBtListener();

        dialogProcessor = new DialogProcessor(this);

        setupViewRecyclerContacts();
        setupContactEditor();
        setupContactor();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Contactor.getInstance().baseStart();
        Caller.getInstance().baseStart();
        ContactEditorHelper.getInstance().baseStart();
        getAllContacts();
        IBtToUiListener = ConnectorBluetooth.getInstance().getIbtToUiListener();
    }

    //-------------------------- base

    @Override
    protected void onResume() {
        super.onResume();
        if (debug) Log.w(TAG,"onResume");
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
        addRunnable(() -> ResourceManager.getInstance().stop());
        deviceListAdapter = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addRunnable(() -> ResourceManager.getInstance().destroy());
        if (debug) Log.w(TAG, "onDestroy");
    }

    //-------------------------- menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!viewManager.isMainViewHidden())
            onCreateConnectMenu(menu);
        else
            onCreateScanMenu(menu);
        return true;
    }

    private void onCreateConnectMenu(Menu menu){
        if (debug) Log.i(TAG, "onCreateConnectMenu");
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        actionBar.setCustomView(null);
    }

    private void onCreateScanMenu(Menu menu){
        if (debug) Log.i(TAG, "onCreateScanMenu");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (!IBtToUiListener.menuChangeCondition()) {
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
                IUiToBtListener.scanItemSelectedListener();
                break;
            case R.id.menu_stop:
                IUiToBtListener.stopItemSelectedListener();
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
        if (viewManager.isMainViewHidden()) {
            if (debug) Log.i(TAG, "onBackPressed showMainView");
            viewManager.showMainView();
            actionBar.setCustomView(null);
            if (getEditorState() != EditorState.Inactive)
                goToEditorState(EditorState.Inactive);
            IUiToBtListener.stopItemSelectedListener();
            invalidateOptionsMenu();
        } else {
            super.onBackPressed();
        }
    }

    //-------------------------- permissons

    private void enPermissions(){
        if (debug) Log.i(TAG, "enPermissions");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION))
            enPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (checkPermission(Manifest.permission.RECORD_AUDIO, REQUEST_MICROPHONE))
            enPermission(Manifest.permission.RECORD_AUDIO);
    }

    private boolean checkPermission(String permission, int requestPermission){
        if (debug) Log.i(TAG, "checkPermission()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{permission}, requestPermission);
                return false;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestPermission);
                return false;
            }
        }
        return true;
    }

    private void enPermission(String permission){
        if (debug) Log.i(TAG, "enPermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                if (provider != null)
                    locationManager.requestLocationUpdates(provider, 400, 1, this);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                if (provider != null)
                    locationManager.requestLocationUpdates(provider, 400, 1, this);
            }
        }
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
                .setListener(ContactEditorHelper.getInstance());
    }

    private void setupContactEditor() {
        if (debug) Log.i(TAG, "setupContactEditor");
        ContactEditorHelper.getInstance()
                .setViewManager(viewManager)
                .setSwipeCrutch(swipeCrutch)
                .setActiveContactHelper(activeContactHelper)
                .setiMsgToUi(this)
                .setiContact(Contactor.getInstance())
                .setContactsAdapter(contactsAdapter);
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void afterTextChanged(Editable arg0) {}
            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {ContactEditorHelper.getInstance().contactFieldChanged();}
        };
        editTextContactIp.addTextChangedListener(textWatcher);
        editTextContactName.addTextChangedListener(textWatcher);
    }

    private void setupViewListDevices() {
        if (debug) Log.i(TAG, "setupViewListDevices");
        listDevices.setAdapter(deviceListAdapter);
        listDevices.setOnTouchListener(new LinearLayoutTouchListener());
        listDevices.setOnItemClickListener((parent, view1, position, id) -> IUiToBtListener.clickItemListListener(position));
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
                        setEditorSwipedIn();
                        startEditorEditContact(contactsAdapter.getItem(position), position);
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
                    getIpAddr(Settings.isIpv4Used),
                    Settings.serverLocalPortNumber,
                    opMode.getSettingName()
            );
            SpannableString s = new SpannableString(title);
            if (title != null) {
                s.setSpan(new ForegroundColorSpan(Colors.WHITE), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//              s.setSpan(new AbsoluteSizeSpan(56), 0, title.length(), SPAN_INCLUSIVE_INCLUSIVE);
                s.setSpan(new RelativeSizeSpan(0.5f), 7, title.length(), 0);
            }
            actionBar.setTitle(s);
        }
    }

    //--------------------- actions

    void clickBtnClearContact() {
        if (debug) Log.i(TAG, "clickBtnClearContact");
        if (chosenContactHelper.isChosen()) {
            chosenContactHelper.clear();
            activeContactHelper.goToState(ActiveContactState.Default);
        } else {
            viewManager.clearSearch();
        }
    }

    private void clickContactItem(Contact contact, int position) {
        if (debug) Log.i(TAG, "clickContactItem");
        chosenContactHelper.choose(contact, position);
        activeContactHelper.goToState(ActiveContactState.FromChosen);
    }

    public void clickBtnChangeDevice() {
        setVisibleList();
        IUiToBtListener.clickBtnChangeDeviceListenerOne();
        setupViewListDevices();
        if (debug) Log.i(TAG,"before caller getBluetoothAdapter");
        // При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
        // останавливаем сканирование и запускаем новое Activity
        IUiToBtListener.clickBtnChangeDeviceListenerTwo();
    }

    //--------------------- INetInfoGetter

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
    public BluetoothManager getBluetoothManager() {
        return (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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
        viewManager.setMainNoDevice();
        invalidateOptionsMenu();
    }

    @Override
    public void withDeviceView() {
        viewManager.setMainDeviceConnected();
        invalidateOptionsMenu();
   }

    @Override
    public String getUnknownServiceString() {
        return getResources().getString(R.string.unknown_service);
    }

    @Override
    public String unknownCharaString() {
        return getResources().getString(R.string.unknown_characteristic);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (debug) Log.i(TAG, "onLocationChanged");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        if (debug) Log.i(TAG, "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String s) {
        if (debug) Log.i(TAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        if (debug) Log.i(TAG, "onProviderDisabled");
    }

    private void swipeScanStart(){
        if (!viewManager.isScanViewHidden()) {
            IUiToBtListener.swipeScanStartListener();
        }
    }

    private void swipeScanStop(){
        if (!viewManager.isScanViewHidden()) {
            IUiToBtListener.swipeScanStopListener();
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

             LinearLayoutTouchListener() {}
             void onRightToLeftSwipe() {Log.i(logTag, "RightToLeftSwipe!");}
             void onLeftToRightSwipe() {Log.i(logTag, "LeftToRightSwipe!");}

             void onTopToBottomSwipe() {
                Log.i(logTag, "onTopToBottomSwipe!");
                swipeScanStart();
            }

             void onBottomToTopSwipe() {
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
                    }
                    return false; // no swipe horizontally and no swipe vertically
                } // case MotionEvent.ACTION_UP:
            }
            return false;
        }

    }

    //--------------------- ViewManager

    public void setVisibleMain() {
        runOnUiThread(() -> {
            invalidateOptionsMenu();
            viewManager.showMainView();
        });
    }

    public void setVisibleList() {
        runOnUiThread(() -> {
            invalidateOptionsMenu();
            viewManager.showScaner();
        });
    }

    //--------------------- IGetViewGetter, IGetView

    @Override
    public IGetView getViewGetter() {
        if (debug) Log.i(TAG, "getViewGetter");
        return this;
    }

    @Nullable
    @Override
    public <T extends View> T getView(int id) {
        if (debug) Log.i(TAG, "getView");
        return findViewById(id);
    }

    @Nullable
    @Override
    public Animation getAnimation(int id) {
        if (debug) Log.i(TAG, "getAnimation");
        return AnimationUtils.loadAnimation(this, id);
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
        if (!isFinishing()) {
            sendToUiRunnable(isFromUiThread, () -> dialogProcessor.runDialog(toRun, toDoMap, messages));
        } else {
            Log.e(TAG, "sendToUiDialog isFinishing, not sending");
        }
    }

    @Override
    public void recallFromUiDialog(boolean isFromUiThread, DialogType toDeny, DialogState onDeny) {
        if (debug) Log.i(TAG, "recallFromUiDialog");
        sendToUiRunnable(isFromUiThread, () -> dialogProcessor.denyDialog(toDeny, onDeny));
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
