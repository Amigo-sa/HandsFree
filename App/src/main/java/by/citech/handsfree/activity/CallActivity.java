package by.citech.handsfree.activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
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
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;

import by.citech.handsfree.R;
import by.citech.handsfree.application.ThisApp;
import by.citech.handsfree.bluetoothlegatt.IBluetoothListener;
import by.citech.handsfree.bluetoothlegatt.IScanListener;
import by.citech.handsfree.bluetoothlegatt.ui.BluetoothUi;
import by.citech.handsfree.bluetoothlegatt.ui.IMenuListener;
import by.citech.handsfree.bluetoothlegatt.ui.IUiToBtListener;
import by.citech.handsfree.bluetoothlegatt.ui.LeDeviceListAdapter;
import by.citech.handsfree.input.TwoInputController;
import by.citech.handsfree.contact.ActiveContact;
import by.citech.handsfree.contact.ChosenContact;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.contact.ContactEditor;
import by.citech.handsfree.contact.Contactor;
import by.citech.handsfree.contact.ContactsAdapter;
import by.citech.handsfree.contact.EActiveContactState;
import by.citech.handsfree.contact.EContactEditorState;
import by.citech.handsfree.dialog.DialogProcessor;
import by.citech.handsfree.dialog.EDialogState;
import by.citech.handsfree.dialog.EDialogType;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.parameters.Colors;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.PreferencesProcessor;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.statistic.NumberedTrafficAnalyzer;
import by.citech.handsfree.statistic.RssiReporter;
import by.citech.handsfree.threading.IThreading;
import by.citech.handsfree.ui.IBtToUiCtrl;
import by.citech.handsfree.ui.IGetView;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.ui.TouchListener;
import by.citech.handsfree.util.Keyboard;
import timber.log.Timber;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;
import static by.citech.handsfree.util.Network.getIpAddr;

public class CallActivity
        extends AppCompatActivity
        implements INetInfoGetter, IBluetoothListener, LocationListener, IGetView,
        IThreading, IBtToUiCtrl, TwoInputController.ITwoInput, IMsgToUi, IScanListener {

    private static final String STAG = Tags.DeviceControlActivity;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private EOpMode opMode;

    public static final int REQUEST_LOCATION = 99;
    public static final int REQUEST_MICROPHONE = 98;

    private CallActivityViewManager viewManager;
    private DialogProcessor dialogProcessor;
    private ActionBar actionBar;

    // список найденных устройств
    private LeDeviceListAdapter deviceListAdapter;
    // список найденных устройств
    private LeDeviceListAdapter connectDeviceListAdapter;

    // ддя списка контактов
    private ContactsAdapter contactsAdapter;
    private ContactsAdapter.SwipeCrutch swipeCrutch;
    private ActiveContact activeContact;
    private ChosenContact chosenContact;
    private ContactEditor contactEditor;

    // для включения разрешения местоположения
    private LocationManager locationManager;
    private String provider;

    // интерфейсы для работы gui с bt
    private IUiToBtListener iUiToBtListener;
    private IMenuListener iMenuListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        PreferencesProcessor.applyPrefsToSettings(this);
        opMode = Settings.Common.opMode;
        if (debug) Timber.tag(TAG).w("onCreate opMode is %s", opMode);

        viewManager = new CallActivityViewManager(opMode,this);

        enPermissions();

        if (opMode != EOpMode.Normal) {
            Handler handler = new Handler();
            NumberedTrafficAnalyzer.getInstance()
                    .setListener(viewManager)
                    .setHandler(handler)
                    .setInterval(1000);
            RssiReporter.getInstance()
                    .setListener(viewManager)
                    .setHandler(handler)
                    .setInterval(1000);
        }

        viewManager.setDefaultView();

        deviceListAdapter = new LeDeviceListAdapter(this.getLayoutInflater());
        connectDeviceListAdapter = new LeDeviceListAdapter(this.getLayoutInflater());

        dialogProcessor = new DialogProcessor(this);
        contactEditor = new ContactEditor();

        findViewById(R.id.btnChangeDevice).setOnClickListener((v) -> clickBtnChangeDevice());
        findViewById(R.id.btnClearContact).setOnClickListener((v) -> clickBtnClearContact());
        findViewById(R.id.btnAddContact).setOnClickListener((v) -> clickBtnStartEditorAdd());
        findViewById(R.id.btnDelContact).setOnClickListener((v) -> clickBtnDeleteFromEditor());
        findViewById(R.id.btnSaveContact).setOnClickListener((v) -> clickBtnSaveInEditor());
        findViewById(R.id.btnCancelContact).setOnClickListener((v) -> clickBtnCancelInEditor());
        findViewById(R.id.btnGreen).setOnClickListener((v) -> onInput1());
        findViewById(R.id.btnRed).setOnClickListener((v) -> onInput2());

        setupToolbar();
        setupContactsAdapter();
        setupContactEditor();
        setupContactor();
        setupListDevices();

        ThisApp.getThisAppBuilder()
                .setiNetInfoGetter(this)
                .setiBluetoothListener(this)
                .setiScanListener(this)
                .setiBtToUiCtrl(this)
                .setiMsgToUi(this)
                .build();

        BluetoothUi bluetoothUi = ThisApp.getBluetoothUi();

        bluetoothUi
                .setmIBluetoothListener(this)
                .setiBtToUiCtrl(this)
                .setiMsgToUi(this)
                .setiBtConnectList(connectDeviceListAdapter)
                .setiBtList(deviceListAdapter)
                .registerListenerBroadcast()
                .build();

        iUiToBtListener = bluetoothUi;
        iMenuListener = bluetoothUi;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (debug) Timber.tag(TAG).w("onStart");
    }

    //-------------------------- base

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (debug) Timber.tag(TAG).w("onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) { //может быть и не вызван, эффективен при повороте экрана
        super.onRestoreInstanceState(savedInstanceState);
        if (debug) Timber.tag(TAG).w("onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (debug) Timber.tag(TAG).w("onResume");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (debug) Timber.tag(TAG).w("onPostCreate");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (debug) Timber.tag(TAG).w("onPostResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (debug) Timber.tag(TAG).w("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (debug) Timber.tag(TAG).w("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (debug) Timber.tag(TAG).w("onDestroy");
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

    private void onCreateConnectMenu(Menu menu) {
        if (debug) Timber.tag(TAG).i("onCreateConnectMenu");
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        actionBar.setCustomView(null);
    }

    private void onCreateScanMenu(Menu menu) {
        if (debug) Timber.tag(TAG).i("onCreateScanMenu");
        getMenuInflater().inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (debug) Timber.tag(TAG).i("onBackPressed");
        Keyboard.hideSoftKeyboard(this);
        if (viewManager.isMainViewHidden()) {
            if (debug) Timber.tag(TAG).i("onBackPressed showMainView");
            viewManager.showMainView();
            actionBar.setCustomView(null);
            contactEditor.goToState(EContactEditorState.Inactive);
            iMenuListener.menuScanStopListener();
            invalidateOptionsMenu();
        } else {
            finish();
        }
    }

    //-------------------------- permissons

    private void enPermissions() {
        if (debug) Timber.tag(TAG).i("enPermissions");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION))
            enPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (checkPermission(Manifest.permission.RECORD_AUDIO, REQUEST_MICROPHONE))
            enPermission(Manifest.permission.RECORD_AUDIO);
    }

    private boolean checkPermission(String permission, int requestPermission) {
        if (debug) Timber.tag(TAG).i("checkPermission()");
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

    private void enPermission(String permission) {
        if (debug) Timber.tag(TAG).i("enPermission");
        if (provider == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(provider, 400, 1, this);
        } else {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    enPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                break;
            case REQUEST_MICROPHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    enPermission(Manifest.permission.RECORD_AUDIO);
                break;
            default:
                break;
        }
    }

    //--------------------- setup

    private void setupToolbar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            String title = String.format(Locale.US, "%s %s:%d %s",
                    getTitle().toString(),
                    getIpAddr(Settings.Network.isIpv4Used),
                    Settings.Network.serverLocalPortNumber,
                    opMode.getSettingName()
            );
            SpannableString s = new SpannableString(title);
            if (title != null) {
                s.setSpan(new ForegroundColorSpan(Colors.WHITE), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                s.setSpan(new AbsoluteSizeSpan(40), 0, title.length(), SPAN_INCLUSIVE_INCLUSIVE);
                s.setSpan(new RelativeSizeSpan(0.5f), 7, title.length(), 0);
            }
            actionBar.setTitle(s);
        }
    }

    private void setupContactsAdapter() {
        if (debug) Timber.tag(TAG).i("setupContactsAdapter");
        RecyclerView viewRecyclerContacts = findViewById(R.id.viewRecyclerContacts);
        contactsAdapter = new ContactsAdapter(Contactor.getInstance().getContacts());
        contactsAdapter.setOnClickViewListener(this::clickContactItem);
        swipeCrutch = contactsAdapter.new SwipeCrutch();
        viewRecyclerContacts.setHasFixedSize(false);
        viewRecyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        viewRecyclerContacts.setAdapter(contactsAdapter);
        viewRecyclerContacts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
        itemTouchHelper.attachToRecyclerView(viewRecyclerContacts);
    }

    private void setupContactEditor() {
        if (debug) Timber.tag(TAG).i("setupContactEditor");
        chosenContact = new ChosenContact(viewManager);
        activeContact = new ActiveContact(chosenContact, viewManager);
        contactEditor
                .setViewManager(viewManager)
                .setSwipeCrutch(swipeCrutch)
                .setActiveContactHelper(activeContact)
                .setiMsgToUi(this)
                .setiContact(Contactor.getInstance())
                .setContactsAdapter(contactsAdapter);
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void afterTextChanged(Editable arg0) {}
            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                contactEditor.contactFieldChanged();}
        };
        EditText editTextContactIp = findViewById(R.id.editTextContactIp);
        EditText editTextContactName = findViewById(R.id.editTextContactName);
        editTextContactIp.addTextChangedListener(textWatcher);
        editTextContactName.addTextChangedListener(textWatcher);
    }

    private void setupContactor() {
        if (debug) Timber.tag(TAG).i("setupContactor");
        EditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.setHintTextColor(Colors.GRAY);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable arg0) {contactsAdapter.filter(editTextSearch.getText().toString());}
            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });
        Contactor.getInstance()
                .setiMsgToUi(this)
                .setListener(contactEditor);
        contactEditor.getAllContacts();
    }

    private void setupListDevices() {
        TouchListener touchListener = new TouchListener(ThisApp.getBluetoothUi());
        findViewById(R.id.baseView).setOnTouchListener(touchListener);

        ListView listDevices = findViewById(R.id.listDevices);
        ListView connectDevices = findViewById(R.id.listConnectDevices);

        listDevices.setAdapter(deviceListAdapter);
        connectDevices.setAdapter(connectDeviceListAdapter);

        listDevices.setOnTouchListener(touchListener);

        listDevices.setOnItemClickListener((parent, view, position, id) -> {
                    BluetoothDevice device = deviceListAdapter.getDevice(position);
                    if (device == null) return;
                    iUiToBtListener.clickItemList(device);
                }
        );

        connectDevices.setOnItemClickListener((parent, view1, position, id) -> {
            final BluetoothDevice device = connectDeviceListAdapter.getDevice(position);
            if (device == null) return;
            iUiToBtListener.clickItemList(device);
        });


    }

    private ItemTouchHelper.Callback createHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {return false;}
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (debug) Timber.tag(TAG).i("onSwiped");
                int position = viewHolder.getAdapterPosition();
                swipeCrutch.designateSwipe(viewHolder.itemView, position);
                switch (swipeDir) {
                    case ItemTouchHelper.RIGHT:
                        contactEditor.setEditorSwipedIn();
                        contactEditor.startEditorEdit(contactsAdapter.getItem(position), position);
                        break;
                    default:
                        if (debug) Timber.tag(TAG).i("swipe swipeDir is %s", swipeDir);
                        break;
                }
            }
        };
    }

    //--------------------- actions

    private void clickBtnCancelInEditor() {contactEditor.cancelContact();}
    private void clickBtnSaveInEditor() {contactEditor.saveContact();}
    private void clickBtnStartEditorAdd() {contactEditor.startEditorAdd();}
    private void clickBtnDeleteFromEditor() {contactEditor.deleteContact();}

    void clickBtnClearContact() {
        if (debug) Timber.tag(TAG).i("clickBtnClearContact");
        if (chosenContact.isChosen()) {
            chosenContact.clear();
            activeContact.goToState(EActiveContactState.Default);
        } else {
            viewManager.clearSearch();
        }
    }

    private void clickContactItem(Contact contact, int position) {
        if (debug) Timber.tag(TAG).i("clickContactItem");
        chosenContact.choose(contact, position);
        activeContact.goToState(EActiveContactState.FromChosen);
    }

    public void clickBtnChangeDevice() {
        setVisibleList();
        if (debug) Timber.tag(TAG).i("before caller getBluetoothAdapter");
        iUiToBtListener.clickBtnListener();
    }

    //--------------------- INetInfoGetter

    @Override
    public String getRemAddr() {
        return activeContact.getIp();
    }

    @Override
    public String getRemPort() {
        return Integer.toString(Settings.Network.serverRemotePortNumber);
    }

    @Override
    public String getLocPort() {
        return Integer.toString(Settings.Network.serverLocalPortNumber);
    }

    //--------------------- IBluetoothListener

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

    //------------------ IScanListener ---------------------

    @Override
    public void onStartScan() {
        // initialize list device
        if (deviceListAdapter != null)
            deviceListAdapter.clear();
        if (iUiToBtListener.isConnected())
            connectDeviceListAdapter.addDevice(iUiToBtListener.getConnectedDevice(), false, true);
        actionBar.setCustomView(R.layout.actionbar);
        invalidateOptionsMenu();
    }

    @Override
    public void onStopScan() {
        actionBar.setCustomView(null);
        invalidateOptionsMenu();
    }

    @Override
    public void scanCallback(BluetoothDevice device, int rssi) {
        if (deviceListAdapter != null)
            deviceListAdapter.addDevice(device, false, false);
    }

    //--------------------- LocationListener

    @Override public void onLocationChanged(Location location) {}
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}

    //--------------------- IBtToUiCtrl

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

    //--------------------- IGetView

    @Nullable
    @Override
    public <T extends View> T getView(int id) {
        return findViewById(id);
    }

    @Nullable
    @Override
    public Animation getAnimation(int id) {
        return AnimationUtils.loadAnimation(this, id);
    }

    //--------------------- IMsgToUi

    @Override
    public void sendToUiToast(boolean isFromUiThread, String message) {
        if (debug) Timber.i("sendToUiToast");
        sendToUiRunnable(isFromUiThread, () -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void sendToUiDialog(boolean isFromUiThread, EDialogType toRun, Map<EDialogState, Runnable> toDoMap, String... messages) {
        if (debug) Timber.i("sendToUiDialog");
        if (!isFinishing()) {
            sendToUiRunnable(isFromUiThread, () -> dialogProcessor.runDialog(toRun, toDoMap, messages));
        } else {
            if (debug) Timber.e("sendToUiDialog isFinishing, not sending");
        }
    }

    @Override
    public void recallFromUiDialog(boolean isFromUiThread, EDialogType toDeny, EDialogState onDeny) {
        if (debug) Timber.i("recallFromUiDialog");
        sendToUiRunnable(isFromUiThread, () -> dialogProcessor.denyDialog(toDeny, onDeny));
    }

    @Override
    public void sendToUiRunnable(boolean isFromUiThread, Runnable toDo) {
        if (debug) Timber.i("sendToUiRunnable");
        if (isFromUiThread) {
            toDo.run();
        } else {
            runOnUiThread(toDo);
        }
    }

}