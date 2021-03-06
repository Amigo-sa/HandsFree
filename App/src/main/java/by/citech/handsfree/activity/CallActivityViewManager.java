package by.citech.handsfree.activity;

import android.support.annotation.IdRes;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import by.citech.handsfree.R;
import by.citech.handsfree.bluetoothlegatt.fsm.BtFsm;
import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.debug.fsm.DebugFsm;
import by.citech.handsfree.network.fsm.NetFsm;
import by.citech.handsfree.statistic.NumberedTrafficAnalyzer.IOnInfoUpdateListener;
import by.citech.handsfree.statistic.NumberedTrafficInfo;
import by.citech.handsfree.statistic.RssiReporter;
import by.citech.handsfree.ui.IGetView;
import by.citech.handsfree.parameters.Colors;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.ui.ViewKeeper;
import timber.log.Timber;

import static by.citech.handsfree.ui.ViewHelper.clearAnimation;
import static by.citech.handsfree.ui.ViewHelper.disableGray;
import static by.citech.handsfree.ui.ViewHelper.enable;
import static by.citech.handsfree.ui.ViewHelper.enableGreen;
import static by.citech.handsfree.ui.ViewHelper.getText;
import static by.citech.handsfree.ui.ViewHelper.getVisibility;
import static by.citech.handsfree.ui.ViewHelper.setColorAndText;
import static by.citech.handsfree.ui.ViewHelper.setText;
import static by.citech.handsfree.ui.ViewHelper.setVisibility;
import static by.citech.handsfree.contact.ContactHelper.setContactInfo;
import static by.citech.handsfree.ui.ViewHelper.startAnimation;
import static by.citech.handsfree.call.fsm.ECallState.ST_Ready;

public class CallActivityViewManager
        implements IOnInfoUpdateListener, ViewKeeper.IViewKeeper,
        RssiReporter.IOnRssiUpdateListener,
        CallFsm.ICallFsmListenerRegister,
        DebugFsm.IDebugFsmListenerRegister {

    private static final String STAG = Tags.CallActivityViewManager;
    private static final boolean debug = Settings.debug;

    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private static final int DARKCYAN = Colors.DARKCYAN;
    private static final int DARKKHAKI = Colors.DARKKHAKI;

    //--------------------- preparation

    private IGetView iGetter;
    private EOpMode opMode;
    private TextView textViewRssi;
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

    //--------------------- listeners

    private CallFsm.ICallFsmListener callFsmListener = (from, to, report) -> {
        switch (report) {
            case RP_NetError:
            case RP_BtError:
                disableGray(getBtnGreen(), "ERROR");
                disableGray(getBtnRed(), "ERROR");
                break;
            case RP_NetReady:
            case RP_BtReady:
                if (to == ST_Ready) {
                    enableBtnCall(getBtnGreen(), "CALL");
                    disableGray(getBtnRed(), "IDLE");
                }
                break;
            case RP_CallFailedExternally:
            case RP_CallFailedInternally:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "FAILED");
                break;
            case RP_CallEndedRemote:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "ENDED");
                break;
            case RP_OutConnected:
                enableBtnCall(getBtnGreen(), "CALLING...");
                enableBtnCall(getBtnRed(), "CANCEL");
                break;
            case RP_OutAcceptedRemote:
                disableGray(getBtnGreen(), "ON CALL");
                enableBtnCall(getBtnRed(), "END CALL");
                stopCallAnim();
                break;
            case RP_OutRejectedRemote:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "BUSY");
                stopCallAnim();
                break;
            case RP_OutFailed:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "OFFLINE");
                stopCallAnim();
                break;
            case RP_OutInvalidCoordinates:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "BAD IP");
                stopCallAnim();
                break;
            case RP_InConnected:
                enableBtnCall(getBtnGreen(), "INCOMING...");
                enableBtnCall(getBtnRed(), "REJECT");
                startCallAnim();
                break;
            case RP_InCanceledRemote:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "CANCELED");
                stopCallAnim();
                break;
            case RP_OutStartedLocal:
                disableGray(getBtnGreen(), "CALLING...");
                enableBtnCall(getBtnRed(), "CANCEL");
                startCallAnim();
                break;
            case RP_CallEndedLocal:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "ENDED");
                break;
            case RP_OutCanceledLocal:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "CANCELED");
                stopCallAnim();
                break;
            case RP_InRejectedLocal:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "REJECTED");
                stopCallAnim();
                break;
            case RP_InAcceptedLocal:
                disableGray(getBtnGreen(), "ON CALL");
                enableBtnCall(getBtnRed(), "END CALL");
                stopCallAnim();
                break;
            default:
                break;
        }
    };

    private BtFsm.IBtFsmListener btFsmListener = (from, to, report) -> {
        //TODO: прикрутить отображение
    };

    private NetFsm.INetFsmListener netFsmListener = (from, to, report) -> {
        //TODO: прикрутить отображение
    };

    private DebugFsm.IDebugFsmListener debugFsmListener = (from, to, report) -> {
        switch (report) {
            case RP_StartDebug:
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
                            case ST_DebugPlay:
                                disableGray(getBtnGreen(), "PLAYING");
                                enableBtnCall(getBtnRed(), "STOP");
                                break;
                            case ST_DebugRecord:
                                disableGray(getBtnGreen(), "RECORDING");
                                enableBtnCall(getBtnRed(), "STOP");
                                break;
                            default:
                                break;
                        }
                        break;
                    case Net2Net:
                    default:
                        break;
                }
                break;
            case RP_StopDebug:
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
    };

    //--------------------- constructor

    CallActivityViewManager(EOpMode opMode, IGetView iGetter) {
        this.iGetter = iGetter;
        this.opMode = opMode;
    }

    //--------------------- main

    void setDefaultView() {
        Timber.tag(TAG).i("setDefaultView");

        setVisibility(getViewTraffic(), View.GONE);
        setColorAndText(getBtnChangeDevice(), R.string.connect_device, DARKCYAN);

        if (opMode == EOpMode.Normal) {
            registerCallFsmListener(callFsmListener, Tags.CallActivityViewManager);
        } else {
            registerDebugFsmListener(debugFsmListener, Tags.CallActivityViewManager);
        }

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

    //--------------------- call buttons

    private int getColorBtnCall(Button button) {
        if (button == getBtnGreen()) {
            return Colors.GREEN;
        } else if (button == getBtnRed()) {
            return Colors.RED;
        } else {
            Timber.tag(TAG).e("enableBtnCall color not defined");
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
        if (debug && !isCallAnim) Timber.tag(TAG).i("startCallAnim");
        startAnimation(getBtnGreen(), getAnimCall());
        isCallAnim = true;
    }

    private void stopCallAnim() {
        Timber.tag(TAG).i("stopCallAnim");
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
            if (a == null) {Timber.tag(TAG).w("getAnimFromGetter anim is still null, return");}
        } else {Timber.tag(TAG).e("getAnimFromGetter iGetter is null, return");}
        return a;
    }

    private <T extends View> T getViewFromGetter(IGetView iGetter, @IdRes int id) {
        T t = null;
        if (iGetter != null) {
            t = iGetter.getView(id);
            if (t == null) {Timber.tag(TAG).w("getViewFromGetter view is still null, return");}
        } else {Timber.tag(TAG).e("getViewFromGetter iGetter is null, return");}
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

    private TextView getTextViewRssi()                      {return textViewRssi                      = getView(textViewRssi,                      R.id.textViewRssi);}
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

    //--------------------- IOnRssiUpdateListener

    @Override
    public void onRssiUpdated(int rssi) {
        setText(getTextViewRssi()                     , String.format(Locale.US, " RSSI:                      %08d", rssi));
    }

    //--------------------- IOnInfoUpdateListener

    @Override
    public void onNumberedTrafficInfoUpdated(NumberedTrafficInfo info) {
        setText(getTextViewPacketSize()               , String.format(Locale.US, " Размер пакета:             %08d", info.getPacketSize()));
        setText(getTextViewLastLostPacketsAmount()    , String.format(Locale.US, " Последняя потеря, пакетов: %08d", info.getLastLostPacketsAmount()));
        setText(getTextViewMaxLostPacketsAmount()     , String.format(Locale.US, " Макс. потеря, пакетов:     %08d", info.getMaxLostPacketsAmount()));
        setText(getTextViewTotalPacketsCount()        , String.format(Locale.US, " Всего, пакетов:            %08d", info.getTotalPacketsCount()));
        setText(getTextViewTotalBytesCount()          , String.format(Locale.US, " Всего, байт                %08d", info.getTotalBytesCount()));
        setText(getTextViewTotalReceivedPacketsCount(), String.format(Locale.US, " Всего принято, пакетов:    %08d", info.getTotalReceivedPacketsCount()));
        setText(getTextViewTotalLostPacketsCount()    , String.format(Locale.US, " Всего утеряно, пакетов:    %08d", info.getTotalLostPacketsCount()));
        setText(getTextViewTotalLostPercent()         , String.format(Locale.US, " Всего утеряно, процент:    %08f", info.getTotalLostPercent()));
        setText(getTextViewTotalBytesPerSec()         , String.format(Locale.US, " Байт/сек, среднее:         %08f", info.getTotalBytesPerSec()));
        setText(getTextViewDeltaLostPacketsCount()    , String.format(Locale.US, " Текущие потери, пакетов:   %08d", info.getDeltaLostPacketsCount()));
        setText(getTextViewDeltaLostPercent()         , String.format(Locale.US, " Текущий потери, процент:   %08f", info.getDeltaLostPercent()));
        setText(getTextViewDeltaBytesPerSec()         , String.format(Locale.US, " Байт/сек, текущее:         %08f", info.getDeltaBytesPerSec()));
    }

}