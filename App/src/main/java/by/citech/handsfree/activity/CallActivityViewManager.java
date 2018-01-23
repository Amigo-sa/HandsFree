package by.citech.handsfree.activity;

import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import by.citech.handsfree.R;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.logic.ECallerState;
import by.citech.handsfree.logic.ECallReport;
import by.citech.handsfree.traffic.NumberedTrafficAnalyzer.IOnInfoUpdateListener;
import by.citech.handsfree.traffic.NumberedTrafficInfo;
import by.citech.handsfree.ui.IGetView;
import by.citech.handsfree.management.IBase;
import by.citech.handsfree.logic.ICallerFsmListener;
import by.citech.handsfree.logic.ICallerFsmRegisterListener;
import by.citech.handsfree.parameters.Colors;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.ui.helpers.IViewKeeper;

import static by.citech.handsfree.ui.helpers.ViewHelper.clearAnimation;
import static by.citech.handsfree.ui.helpers.ViewHelper.disableGray;
import static by.citech.handsfree.ui.helpers.ViewHelper.enable;
import static by.citech.handsfree.ui.helpers.ViewHelper.enableGreen;
import static by.citech.handsfree.ui.helpers.ViewHelper.getText;
import static by.citech.handsfree.ui.helpers.ViewHelper.getVisibility;
import static by.citech.handsfree.ui.helpers.ViewHelper.setColorAndText;
import static by.citech.handsfree.ui.helpers.ViewHelper.setText;
import static by.citech.handsfree.ui.helpers.ViewHelper.setVisibility;
import static by.citech.handsfree.ui.helpers.ContactHelper.setContactInfo;
import static by.citech.handsfree.ui.helpers.ViewHelper.startAnimation;
import static by.citech.handsfree.logic.ECallerState.ReadyToWork;
import static by.citech.handsfree.settings.EOpMode.Normal;

public class CallActivityViewManager
        implements IBase, ISettingsCtrl, IPrepareObject, IOnInfoUpdateListener,
        IViewKeeper, ICallerFsmListener, ICallerFsmRegisterListener {

    private static final String STAG = Tags.ViewManager;
    private static final boolean debug = Settings.debug;

    private static int objCount;
    private final String TAG;

    private static final int DARKCYAN = Colors.DARKCYAN;
    private static final int DARKKHAKI = Colors.DARKKHAKI;

    static {
        if (debug) Log.i(STAG,"static initiate");
        objCount = 0;
    }

    //--------------------- preparation

    private EOpMode opMode;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return iGetter != null;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        opMode = Settings.getInstance().getCommon().getOpMode();
        return true;
    }

    //--------------------- non-settings

    private IGetView iGetter;

    private TextView textViewPacketSize;
    private TextView textViewLastLostPacketsAmount;
    private TextView textViewMaxLostPacketsAmount;
    private TextView textViewTotalPacketsCount;
    private TextView textViewTotalReceivedPacketsCount;
    private TextView textViewTotalLostPacketsCount;
    private TextView textViewTotalBytesPerSec;
    private TextView textViewTotalBytesCount;
    private TextView textViewTotalLostPercent;
    private TextView textViewDeltaLostPacketsCount;
    private TextView textViewDeltaLostPercent;
    private TextView textViewDeltaBytesPerSec;

    private View viewContacts;
    private View viewTraffic;
    private View scanView;
    private View mainView;
    private View viewContactEditor;
    private View viewContactChosen;
    private EditText editTextSearch;
    private TextView textViewContactChosenName;
    private TextView textViewContactChosenIp;
    private EditText editTextContactName;
    private EditText editTextContactIp;
    private Button btnSaveContact;
    private Button btnDelContact;
    private Button btnCancelContact;
    private Button btnGreen;
    private Button btnRed;
    private Button btnChangeDevice;
    private Animation animCall;
    private boolean isCallAnim;

    //--------------------- getters and setters

    public void setiGetter(IGetView iGetter) {
        this.iGetter = iGetter;
    }

    //--------------------- IBase

    @Override
    public boolean baseCreate() {
        IBase.super.baseCreate();
        if (debug) Log.i(TAG, "baseCreate");
        registerCallerFsmListener(this, TAG);
        return true;
    }

    @Override
    public boolean baseDestroy() {
        if (debug) Log.i(TAG, "baseDestroy");
        unregisterCallerFsmListener(this, TAG);
        scanView = null;
        mainView = null;
        viewContactEditor = null;
        viewContactChosen = null;
        editTextSearch = null;
        textViewContactChosenName = null;
        textViewContactChosenIp = null;
        editTextContactName = null;
        editTextContactIp = null;
        btnSaveContact = null;
        btnDelContact = null;
        btnCancelContact = null;
        btnGreen = null;
        btnRed = null;
        btnChangeDevice = null;
        animCall = null;
        iGetter = null;
        isCallAnim = false;
        IBase.super.baseDestroy();
        return true;
    }

    //--------------------- main

    void setDefaultView() {
        if (debug) Log.i(TAG, "setDefaultView");
        prepareObject();

        setVisibility(getViewTraffic(), View.GONE);
        setColorAndText(getBtnChangeDevice(), R.string.connect_device, DARKCYAN);

        switch (opMode) {
            case Bt2AudOut:
                enableBtnCall(getBtnGreen(), "RECEIVING");
                disableGray(getBtnRed(), "STOP");
                getBtnChangeDevice().setVisibility(View.VISIBLE);
                break;
            case DataGen2Bt:
            case AudIn2Bt:
                enableBtnCall(getBtnGreen(), "TRANSMITTING");
                disableGray(getBtnRed(), "STOP");
                setVisibility(getBtnChangeDevice(), View.VISIBLE);
                break;
            case AudIn2AudOut:
                enableBtnCall(getBtnGreen(), "PLAY");
                disableGray(getBtnRed(), "STOP");
                setVisibility(getBtnChangeDevice(), View.INVISIBLE);
                break;
            case Bt2Bt:
                enableBtnCall(getBtnGreen(), "LBACK ON");
                disableGray(getBtnRed(), "LBACK OFF");
                setVisibility(getBtnChangeDevice(), View.VISIBLE);
                setVisibility(getViewContacts(), View.GONE);
                setVisibility(getViewTraffic(), View.VISIBLE);
                break;
            case Net2Net:
                enableBtnCall(getBtnGreen(), "LBACK ON");
                disableGray(getBtnRed(), "LBACK OFF");
                setVisibility(getBtnChangeDevice(), View.INVISIBLE);
                break;
            case Record:
                enableBtnCall(getBtnGreen(), "RECORD");
                disableGray(getBtnRed(), "PLAY");
                setVisibility(getBtnChangeDevice(), View.VISIBLE);
                break;
            case Normal:
            default:
                disableGray(getBtnGreen(), "NOT READY");
                disableGray(getBtnRed(), "NOT READY");
                setVisibility(getBtnChangeDevice(), View.VISIBLE);
                break;
        }

        getAnimCall().setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {if (isCallAnim) {startCallAnim();}}
            @Override public void onAnimationRepeat(Animation animation) {}});
    }

    boolean isMainViewHidden() {
        return getVisibility(getMainView());
    }

    boolean isScanViewHidden() {
        return getVisibility(getScanView());
    }

    void showMainView() {
        setVisibility(getMainView(), View.VISIBLE);
        setVisibility(getViewContactEditor(), View.GONE);
        setVisibility(getScanView(), View.GONE);
    }

    void showScaner() {
        setVisibility(getMainView(), View.GONE);
        setVisibility(getScanView(), View.VISIBLE);
    }

    //--------------------- device connected/disconnected

    void setMainNoDevice() {
        setColorAndText(getBtnChangeDevice(), R.string.connect_device, DARKCYAN);
    }

    void setMainDeviceConnected() {
        setColorAndText(getBtnChangeDevice(), R.string.change_device, DARKKHAKI);
    }

    //--------------------- search

    void clearSearch() {
        setText(getEditTextSearch(), "");
    }

    //--------------------- chosen

    public void showChosen() {
        setVisibility(getViewContactChosen(), View.VISIBLE);
        setVisibility(getEditTextSearch(), View.GONE);
    }

    public void hideChosen() {
        setVisibility(getViewContactChosen(), View.GONE);
        setVisibility(getEditTextSearch(), View.VISIBLE);
    }

    public void setChosenContactInfo(Contact chosenContact) {
        setContactInfo(chosenContact, getTextViewContactChosenName(), getTextViewContactChosenIp());
    }

    public void clearChosenContactInfo() {
        setContactInfo(getTextViewContactChosenName(), getTextViewContactChosenIp());
    }

    public String getSearchText() {
        return getText(getEditTextSearch());
    }

    //--------------------- editor

    public void showEditor() {
        setVisibility(getMainView(), View.GONE);
        setVisibility(getViewContactEditor(), View.VISIBLE);
    }

    public void hideEditor() {
        setVisibility(getMainView(), View.VISIBLE);
        setVisibility(getViewContactEditor(), View.GONE);
    }

    public String getEditorContactNameText() {
        return getText(getEditTextContactName());
    }

    public String getEditorContactIpText() {
        return getText(getEditTextContactIp());
    }

    public void setEditorAdd() {
        setVisibility(getBtnDelContact(), View.GONE);
        setText(getBtnSaveContact(), "ADD");
        setContactInfo(getEditTextContactName(), getEditTextContactIp());
        disableGray(getBtnDelContact(), getBtnSaveContact(), getBtnCancelContact());
    }

    public void setEditorEdit(Contact contactToEdit) {
        setText(getBtnSaveContact(), "SAVE");
        setContactInfo(contactToEdit, getEditTextContactName(), getEditTextContactIp());
        enableGreen(getBtnDelContact());
        disableGray(getBtnSaveContact(), getBtnCancelContact());
        setVisibility(getBtnDelContact(), View.VISIBLE);
    }

    public void setEditorButtonsFreeze() {
        freezeState(TAG, getBtnDelContact(), getBtnSaveContact(), getBtnCancelContact());
    }

    public void setEditorButtonsRelease() {
        releaseState(TAG);
    }

    public void setEditorFieldChanged() {
        enableGreen(getBtnSaveContact(), getBtnCancelContact());
    }

    //--------------------- ICallNetListener

    private void processNormal(ECallerState from, ECallerState to, ECallReport why) {
        if (debug) Log.i(TAG, "processNormal");
        switch (why) {
            case SysExtError:
            case SysIntError:
            case SysIntConnectedIncompatible:
                disableGray(getBtnGreen(), "ERROR");
                disableGray(getBtnRed(), "ERROR");
                break;
            case SysExtReady:
            case SysIntReady:
                if (to == ReadyToWork) {
                    enableBtnCall(getBtnGreen(), "CALL");
                    disableGray(getBtnRed(), "IDLE");
                }
                break;
            case CallFailedExt:
            case CallFailedInt:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "FAILED");
                break;
            case CallEndedByRemoteUser:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "ENDED");
                break;
            case OutConnectionConnected:
                enableBtnCall(getBtnGreen(), "CALLING...");
                enableBtnCall(getBtnRed(), "CANCEL");
                break;
            case OutCallAcceptedByRemoteUser:
                disableGray(getBtnGreen(), "ON CALL");
                enableBtnCall(getBtnRed(), "END CALL");
                stopCallAnim();
                break;
            case OutCallRejectedByRemoteUser:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "BUSY");
                stopCallAnim();
                break;
            case OutConnectionFailed:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "OFFLINE");
                stopCallAnim();
                break;
            case OutCallInvalidCoordinates:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "BAD IP");
                stopCallAnim();
                break;
            case InCallDetected:
                enableBtnCall(getBtnGreen(), "INCOMING...");
                enableBtnCall(getBtnRed(), "REJECT");
                startCallAnim();
                break;
            case InCallCanceledByRemoteUser:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "CANCELED");
                stopCallAnim();
                break;
            case InCallFailed:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "INCOME FAIL");
                stopCallAnim();
                break;
            case OutConnectionStartedByLocalUser:
                disableGray(getBtnGreen(), "CALLING...");
                enableBtnCall(getBtnRed(), "CANCEL");
                startCallAnim();
                break;
            case CallEndedByLocalUser:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "ENDED");
                break;
            case OutCallCanceledByLocalUser:
            case OutConnectionCanceledByLocalUser:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "CANCELED");
                stopCallAnim();
                break;
            case InCallRejectedByLocalUser:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "REJECTED");
                stopCallAnim();
                break;
            case InCallAcceptedByLocalUser:
                disableGray(getBtnGreen(), "ON CALL");
                enableBtnCall(getBtnRed(), "END CALL");
                stopCallAnim();
                break;
            default:
                break;
        }
    }

    private void processAbnormal(ECallerState from, ECallerState to, ECallReport why) {
        if (debug) Log.i(TAG, "processAbnormal");
        switch (why) {
            case StartDebug:
                switch (opMode) {
                    case DataGen2Bt:
                    case Bt2AudOut:
                    case AudIn2Bt:
                    case AudIn2AudOut:
                    case Bt2Bt:
                        disableGray(getBtnGreen());
                        enableBtnCall(getBtnRed());
                        break;
                    case Record:
                        switch (to) {
                            case DebugPlay:
                                disableGray(getBtnGreen(), "PLAYING");
                                enableBtnCall(getBtnRed(), "STOP");
                                break;
                            case DebugRecord:
                                disableGray(getBtnGreen(), "RECORDING");
                                enableBtnCall(getBtnRed(), "STOP");
                                break;
                            default:
                                if (debug) Log.e(TAG, "startDebug " + to);
                                break;
                        }
                        break;
                    case Net2Net:
                    default:
                        break;
                }
                break;
            case StopDebug:
                switch (opMode) {
                    case DataGen2Bt:
                    case Bt2AudOut:
                    case AudIn2Bt:
                    case AudIn2AudOut:
                    case Bt2Bt:
                        enableGreen(getBtnGreen());
                        disableGray(getBtnRed());
                        break;
                    case Record:
                        enableBtnCall(getBtnGreen(), "PLAY");
                        disableGray(getBtnRed(), "RECORDED");
                        break;
                    case Net2Net:
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCallerStateChange(ECallerState from, ECallerState to, ECallReport why) {
        if (debug) Log.i(TAG, "onCallerStateChange");
        if (opMode == Normal) {
            processNormal(from, to, why);
        } else {
            processAbnormal(from, to, why);
        }
    }

    //--------------------- call buttons

    private int getColorBtnCall(Button button) {
        if (button == getBtnGreen()) {
            return Colors.GREEN;
        } else if (button == getBtnRed()) {
            return Colors.RED;
        } else {
            Log.e(TAG, "enableBtnCall color not defined");
            return Colors.GRAY;
        }
    }

    private void enableBtnCall(Button button) {
        enable(button, getColorBtnCall(button));
    }

    private void enableBtnCall(Button button, String label) {
        enable(button, getColorBtnCall(button), label);
    }

    private void startCallAnim() {
        if (debug && !isCallAnim) Log.i(TAG, "startCallAnim");
        startAnimation(getBtnGreen(), getAnimCall());
        isCallAnim = true;
    }

    private void stopCallAnim() {
        if (debug) Log.i(TAG, "stopCallAnim");
        clearAnimation(getBtnGreen());
        isCallAnim = false;
    }

    //--------------------- getters help

    private Animation getAnim(Animation a, int id) {
        if (a == null) {a = getAnimFromGetter(iGetter, id);}
        return a;
    }

    private <T extends View> T getView(T t, @IdRes int id) {
        if (t == null) {t = getViewFromGetter(iGetter, id);}
        return t;
    }

    private Animation getAnimFromGetter(IGetView iGetter, int id) {
        Animation a = null;
        if (iGetter != null) {
            a = iGetter.getAnimation(id);
            if (a == null) {if (debug) Log.w(TAG, "getAnimFromGetter anim is still null, return");}
        } else {if (debug) Log.e(TAG, "getAnimFromGetter iGetter is null, return");}
        return a;
    }

    private <T extends View> T getViewFromGetter(IGetView iGetter, @IdRes int id) {
        T t = null;
        if (iGetter != null) {
            t = iGetter.getView(id);
            if (t == null) {if (debug) Log.w(TAG, "getViewFromGetter view is still null, return");}
        } else {if (debug) Log.e(TAG, "getViewFromGetter iGetter is null, return");}
        return t;
    }

    //--------------------- getters

    private Animation getAnimCall()                 {return animCall                  = getAnim(animCall,                  R.anim.anim_call);}
    private View getScanView()                      {return scanView                  = getView(scanView,                  R.id.scanView);}
    private View getMainView()                      {return mainView                  = getView(mainView,                  R.id.mainView);}
    private View getViewContactEditor()             {return viewContactEditor         = getView(viewContactEditor,         R.id.viewContactEditor);}
    private View getViewContactChosen()             {return viewContactChosen         = getView(viewContactChosen,         R.id.viewContactChosen);}
    private EditText getEditTextSearch()            {return editTextSearch            = getView(editTextSearch,            R.id.editTextSearch);}
    private TextView getTextViewContactChosenName() {return textViewContactChosenName = getView(textViewContactChosenName, R.id.textViewContactChosenName);}
    private TextView getTextViewContactChosenIp()   {return textViewContactChosenIp   = getView(textViewContactChosenIp,   R.id.textViewContactChosenIp);}
    private EditText getEditTextContactName()       {return editTextContactName       = getView(editTextContactName,       R.id.editTextContactName);}
    private EditText getEditTextContactIp()         {return editTextContactIp         = getView(editTextContactIp,         R.id.editTextContactIp);}
    private Button getBtnSaveContact()              {return btnSaveContact            = getView(btnSaveContact,            R.id.btnSaveContact);}
    private Button getBtnDelContact()               {return btnDelContact             = getView(btnDelContact,             R.id.btnDelContact);}
    private Button getBtnCancelContact()            {return btnCancelContact          = getView(btnCancelContact,          R.id.btnCancelContact);}
    private Button getBtnGreen()                    {return btnGreen                  = getView(btnGreen,                  R.id.btnGreen);}
    private Button getBtnRed()                      {return btnRed                    = getView(btnRed,                    R.id.btnRed);}
    private Button getBtnChangeDevice()             {return btnChangeDevice           = getView(btnChangeDevice,           R.id.btnChangeDevice);}

    private View getViewContacts() {return viewContacts = getView(viewContacts, R.id.contacts_list);}
    private View getViewTraffic()  {return viewTraffic  = getView(viewTraffic,  R.id.traffic_info);}

    private TextView getTextViewPacketSize()                {return textViewPacketSize                = getView(textViewPacketSize,                R.id.textViewPacketSize);}
    private TextView getTextViewLastLostPacketsAmount()     {return textViewLastLostPacketsAmount     = getView(textViewLastLostPacketsAmount,     R.id.textViewLastLostPacketsAmount);}
    private TextView getTextViewMaxLostPacketsAmount()      {return textViewMaxLostPacketsAmount      = getView(textViewMaxLostPacketsAmount,      R.id.textViewMaxLostPacketsAmount);}
    private TextView getTextViewTotalPacketsCount()         {return textViewTotalPacketsCount         = getView(textViewTotalPacketsCount,         R.id.textViewTotalPacketsCount);}
    private TextView getTextViewTotalReceivedPacketsCount() {return textViewTotalReceivedPacketsCount = getView(textViewTotalReceivedPacketsCount, R.id.textViewTotalReceivedPacketsCount);}
    private TextView getTextViewTotalLostPacketsCount()     {return textViewTotalLostPacketsCount     = getView(textViewTotalLostPacketsCount,     R.id.textViewTotalLostPacketsCount);}
    private TextView getTextViewTotalBytesPerSec()          {return textViewTotalBytesPerSec          = getView(textViewTotalBytesPerSec,          R.id.textViewTotalBytesPerSec);}
    private TextView getTextViewTotalBytesCount()           {return textViewTotalBytesCount           = getView(textViewTotalBytesCount,           R.id.textViewTotalBytesCount);}
    private TextView getTextViewTotalLostPercent()          {return textViewTotalLostPercent          = getView(textViewTotalLostPercent,          R.id.textViewTotalLostPercent);}
    private TextView getTextViewDeltaLostPacketsCount()     {return textViewDeltaLostPacketsCount     = getView(textViewDeltaLostPacketsCount,     R.id.textViewDeltaLostPacketsCount);}
    private TextView getTextViewDeltaLostPercent()          {return textViewDeltaLostPercent          = getView(textViewDeltaLostPercent,          R.id.textViewDeltaLostPercent);}
    private TextView getTextViewDeltaBytesPerSec()          {return textViewDeltaBytesPerSec          = getView(textViewDeltaBytesPerSec,          R.id.textViewDeltaBytesPerSec);}

    //--------------------- IOnInfoUpdateListener

    @Override
    public void onNumberedTrafficInfoUpdated(NumberedTrafficInfo info) {
        setText(getTextViewPacketSize()               , String.format(Locale.US, "  Размер пакета:             %010d", info.getPacketSize()));
        setText(getTextViewLastLostPacketsAmount()    , String.format(Locale.US, "  Последняя потеря, пакетов: %010d", info.getLastLostPacketsAmount()));
        setText(getTextViewMaxLostPacketsAmount()     , String.format(Locale.US, "  Макс. потеря, пакетов:     %010d", info.getMaxLostPacketsAmount()));
        setText(getTextViewTotalPacketsCount()        , String.format(Locale.US, "  Всего, пакетов:            %010d", info.getTotalPacketsCount()));
        setText(getTextViewTotalBytesCount()          , String.format(Locale.US, "  Всего, байт                %010d", info.getTotalBytesCount()));
        setText(getTextViewTotalReceivedPacketsCount(), String.format(Locale.US, "  Всего принято, пакетов:    %010d", info.getTotalReceivedPacketsCount()));
        setText(getTextViewTotalLostPacketsCount()    , String.format(Locale.US, "  Всего утеряно, пакетов:    %010d", info.getTotalLostPacketsCount()));
        setText(getTextViewTotalLostPercent()         , String.format(Locale.US, "  Всего утеряно, процент:    %010f", info.getTotalLostPercent()));
        setText(getTextViewTotalBytesPerSec()         , String.format(Locale.US, "  Байт/сек, среднее:         %010f", info.getTotalBytesPerSec()));
        setText(getTextViewDeltaLostPacketsCount()    , String.format(Locale.US, "  Текущие потери, пакетов:   %010d", info.getDeltaLostPacketsCount()));
        setText(getTextViewDeltaLostPercent()         , String.format(Locale.US, "  Текущий потери, процент:   %010f", info.getDeltaLostPercent()));
        setText(getTextViewDeltaBytesPerSec()         , String.format(Locale.US, "  Байт/сек, текущее:         %010f", info.getDeltaBytesPerSec()));
    }

}