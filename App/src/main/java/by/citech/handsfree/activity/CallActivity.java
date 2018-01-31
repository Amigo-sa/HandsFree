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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;

import by.citech.handsfree.R;
import by.citech.handsfree.bluetoothlegatt.ui.BluetoothUi;
import by.citech.handsfree.bluetoothlegatt.ui.IMenuListener;
import by.citech.handsfree.application.ThisAppBuilder;
import by.citech.handsfree.call.CallUi;
import by.citech.handsfree.statistic.NumberedTrafficAnalyzer;
import by.citech.handsfree.statistic.RssiReporter;
import by.citech.handsfree.ui.IBtToUiCtrl;
import by.citech.handsfree.bluetoothlegatt.ui.LeDeviceListAdapter;
import by.citech.handsfree.bluetoothlegatt.IScanListener;
import by.citech.handsfree.ui.ISwipeListener;
import by.citech.handsfree.ui.LinearLayoutTouchListener;
import by.citech.handsfree.ui.helpers.EActiveContactState;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.contact.Contactor;
import by.citech.handsfree.contact.ContactsAdapter;
import by.citech.handsfree.ui.helpers.EEditorState;
import by.citech.handsfree.dialog.DialogProcessor;
import by.citech.handsfree.dialog.EDialogState;
import by.citech.handsfree.dialog.EDialogType;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.ui.helpers.ActiveContactHelper;
import by.citech.handsfree.ui.helpers.ChosenContactHelper;
import by.citech.handsfree.ui.helpers.ContactEditorHelper;
import by.citech.handsfree.ui.IGetView;
import by.citech.handsfree.bluetoothlegatt.ui.IUiToBtListener;
import by.citech.handsfree.bluetoothlegatt.IBluetoothListener;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.parameters.Colors;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.PreferencesProcessor;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.threading.IThreading;
import by.citech.handsfree.util.Keyboard;
import timber.log.Timber;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;
import static by.citech.handsfree.util.Network.getIpAddr;

public class CallActivity
        extends AppCompatActivity
        implements INetInfoGetter,
                   IBluetoothListener,
                   LocationListener,
                   IGetView,
                   IThreading,
                   IBtToUiCtrl,
        CallUi.ICallUi,
                   IMsgToUi,
                   IScanListener {

    private static final String STAG = Tags.DeviceControlActivity;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private EOpMode opMode;

    public static final int REQUEST_LOCATION = 99;
    public static final int REQUEST_MICROPHONE = 98;

    // TODO: отображение траффика для дебага
    private TextView textViewBtInTraffic, textViewBtOutTraffic, textViewNetInTraffic, textViewNetOutTraffic;

    private CallActivityViewManager viewManager;
    private ActionBar actionBar;

    // список найденных устройств
    private ListView listDevices;
    private LeDeviceListAdapter deviceListAdapter;

    // ддя списка контактов
    private DialogProcessor dialogProcessor;
    private RecyclerView viewRecyclerContacts;
    private EditText editTextSearch, editTextContactName, editTextContactIp;
    private ContactsAdapter contactsAdapter;
    private ContactsAdapter.SwipeCrutch swipeCrutch;
    private ActiveContactHelper activeContactHelper;
    private ChosenContactHelper chosenContactHelper;
    private ContactEditorHelper contactEditorHelper;
    private LinearLayoutTouchListener linearLayoutTouchListener;

    // для включения разрешения местоположения
    private LocationManager locationManager;
    private String provider;

    // интерфейсы для работы gui с bt
    private IUiToBtListener IUiToBtListener;
    private IMenuListener iMenuListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        PreferencesProcessor.applyPrefsToSettings(this);
        opMode = Settings.Common.opMode;
        if (debug) Timber.tag(TAG).w("onCreate opMode is %s", opMode);

        viewManager = new CallActivityViewManager();
        enPermissions();

        //---------------- TEST START
        Handler handler = new Handler();
        NumberedTrafficAnalyzer.getInstance()
                .setListener(viewManager)
                .setHandler(handler)
                .setInterval(1000);
        RssiReporter.getInstance()
                .setListener(viewManager)
                .setHandler(handler)
                .setInterval(1000);
        //---------------- TEST END

        viewManager.setiGetter(this);
        viewManager.setDefaultView();

        deviceListAdapter = new LeDeviceListAdapter(this.getLayoutInflater());
        dialogProcessor = new DialogProcessor(this);
        contactEditorHelper = new ContactEditorHelper();

        listDevices = findViewById(R.id.listDevices);
        viewRecyclerContacts = findViewById(R.id.viewRecyclerContacts);
        editTextSearch = findViewById(R.id.editTextSearch);
        editTextContactName = findViewById(R.id.editTextContactName);
        editTextContactIp = findViewById(R.id.editTextContactIp);

        findViewById(R.id.btnChangeDevice).setOnClickListener((v) -> clickBtnChangeDevice());
        findViewById(R.id.btnClearContact).setOnClickListener((v) -> clickBtnClearContact());
        findViewById(R.id.btnAddContact).setOnClickListener((v) -> clickBtnStartEditorAdd());
        findViewById(R.id.btnDelContact).setOnClickListener((v) -> clickBtnDeleteFromEditor());
        findViewById(R.id.btnSaveContact).setOnClickListener((v) -> clickBtnSaveInEditor());
        findViewById(R.id.btnCancelContact).setOnClickListener((v) -> clickBtnCancelInEditor());
        findViewById(R.id.btnGreen).setOnClickListener((v) -> onClickBtnGreen());
        findViewById(R.id.btnRed).setOnClickListener((v) -> onClickBtnRed());

        setupToolbar();
        setupContactsAdapter();
        setupContactEditor();
        setupContactor();

        ThisAppBuilder.getInstance()
                .setOpMode(opMode)
                .setiNetInfoGetter(this)
                .setiBluetoothListener(this)
                .setiScanListener(this)
                .setiBtToUiCtrl(this)
                .setiMsgToUi(this)
                .setiBtList(deviceListAdapter)
                .build();

        BluetoothUi.getInstance()
                .setmIBluetoothListener(this)
                .setiBtToUiCtrl(this)
                .setiMsgToUi(this)
                .registerListenerBroadcast()
                .build();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        IUiToBtListener = BluetoothUi.getInstance();
        iMenuListener = (IMenuListener) IUiToBtListener;
        linearLayoutTouchListener = new LinearLayoutTouchListener((ISwipeListener) IUiToBtListener);
        findViewById(R.id.btnScanDevice).setOnClickListener((v) -> IUiToBtListener.clickBtnScanListener());
        findViewById(R.id.baseView).setOnTouchListener(linearLayoutTouchListener);

        //IScanListener = ConnectorBluetooth.getInstance().getIbtToUiListener();
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) { //может быть и не вызван, ефективен при повороте экрана
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
        if (!IUiToBtListener.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                iMenuListener.menuScanStartListener();
                break;
            case R.id.menu_stop:
                iMenuListener.menuScanStopListener();
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
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
            contactEditorHelper.goToState(EEditorState.Inactive);
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
        chosenContactHelper = new ChosenContactHelper(viewManager);
        activeContactHelper = new ActiveContactHelper(chosenContactHelper, viewManager);
        contactEditorHelper
                .setViewManager(viewManager)
                .setSwipeCrutch(swipeCrutch)
                .setActiveContactHelper(activeContactHelper)
                .setiMsgToUi(this)
                .setiContact(Contactor.getInstance())
                .setContactsAdapter(contactsAdapter);
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void afterTextChanged(Editable arg0) {}
            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {contactEditorHelper.contactFieldChanged();}
        };
        editTextContactIp.addTextChangedListener(textWatcher);
        editTextContactName.addTextChangedListener(textWatcher);
    }

    private void setupContactor() {
        if (debug) Timber.tag(TAG).i("setupContactor");
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
        contactEditorHelper.getAllContacts();
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
                        contactEditorHelper.setEditorSwipedIn();
                        contactEditorHelper.startEditorEdit(contactsAdapter.getItem(position), position);
                        break;
                    default:
                        if (debug) Timber.tag(TAG).i("swipe swipeDir is %s", swipeDir);
                        break;
                }
            }
        };
    }

    //--------------------- actions

    private void clickBtnCancelInEditor() {contactEditorHelper.cancelContact();}
    private void clickBtnSaveInEditor() {contactEditorHelper.saveContact();}
    private void clickBtnStartEditorAdd() {contactEditorHelper.startEditorAdd();}
    private void clickBtnDeleteFromEditor() {contactEditorHelper.deleteContact();}

    void clickBtnClearContact() {
        if (debug) Timber.tag(TAG).i("clickBtnClearContact");
        if (chosenContactHelper.isChosen()) {
            chosenContactHelper.clear();
            activeContactHelper.goToState(EActiveContactState.Default);
        } else {
            viewManager.clearSearch();
        }
    }

    private void clickContactItem(Contact contact, int position) {
        if (debug) Timber.tag(TAG).i("clickContactItem");
        chosenContactHelper.choose(contact, position);
        activeContactHelper.goToState(EActiveContactState.FromChosen);
    }

    public void clickBtnChangeDevice() {
        setVisibleList();
        listDevices.setAdapter(deviceListAdapter);
        listDevices.setOnTouchListener(linearLayoutTouchListener);
        listDevices.setOnItemClickListener((parent, view1, position, id) -> {
                    final BluetoothDevice device = deviceListAdapter.getDevice(position);
                    if (device == null) return;
                    IUiToBtListener.clickItemList(device);
                }
        );
        if (debug) Timber.tag(TAG).i("before caller getBluetoothAdapter");
        // При выборе конкретного устройства в списке устройств получаем адрес и имя устройства,
        // останавливаем сканирование и запускаем новое Activity
        IUiToBtListener.clickBtnListener();
    }

    //--------------------- INetInfoGetter

    @Override
    public String getRemAddr() {
        return activeContactHelper.getIp();
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
        if (IUiToBtListener.isConnecting())
            deviceListAdapter.addDevice(IUiToBtListener.getConnectDevice(), 200);
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
            deviceListAdapter.addDevice(device, rssi);
    }

       //--------------------- LocationListener

    @Override public void onLocationChanged(Location location) {}
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}

    //--------------------- CallActivityViewManager

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