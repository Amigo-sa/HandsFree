package by.citech.handsfree.gui.helper;

import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import by.citech.handsfree.R;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.logic.CallerState;
import by.citech.handsfree.logic.ECallReport;
import by.citech.handsfree.gui.IGetView;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.logic.ICallerFsmListener;
import by.citech.handsfree.logic.ICallerFsmRegister;
import by.citech.handsfree.param.Colors;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.enumeration.OpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

import static by.citech.handsfree.gui.helper.ViewHelper.clearAnimation;
import static by.citech.handsfree.gui.helper.ViewHelper.disableGray;
import static by.citech.handsfree.gui.helper.ViewHelper.enable;
import static by.citech.handsfree.gui.helper.ViewHelper.enableGreen;
import static by.citech.handsfree.gui.helper.ViewHelper.getText;
import static by.citech.handsfree.gui.helper.ViewHelper.getVisibility;
import static by.citech.handsfree.gui.helper.ViewHelper.setColorAndText;
import static by.citech.handsfree.gui.helper.ViewHelper.setText;
import static by.citech.handsfree.gui.helper.ViewHelper.setVisibility;
import static by.citech.handsfree.gui.helper.ContactHelper.setContactInfo;
import static by.citech.handsfree.gui.helper.ViewHelper.startAnimation;
import static by.citech.handsfree.logic.CallerState.ReadyToWork;
import static by.citech.handsfree.settings.enumeration.OpMode.Normal;

public class ViewManager
        implements IBase, ISettingsCtrl, IPrepareObject,
        IViewKeeper, ICallerFsmListener, ICallerFsmRegister {

    private static final String STAG = Tags.VIEW_MANAGER;
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

    private OpMode opMode;

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

    public void setDefaultView() {
        if (debug) Log.i(TAG, "setDefaultView");
        prepareObject();

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

    public boolean isMainViewHidden() {
        return getVisibility(getMainView());
    }

    public boolean isScanViewHidden() {
        return getVisibility(getScanView());
    }

    public void showMainView() {
        setVisibility(getMainView(), View.VISIBLE);
        setVisibility(getViewContactEditor(), View.GONE);
        setVisibility(getScanView(), View.GONE);
    }

    public void showScaner() {
        setVisibility(getMainView(), View.GONE);
        setVisibility(getScanView(), View.VISIBLE);
    }

    //--------------------- device connected/disconnected

    public void setMainNoDevice() {
        setColorAndText(getBtnChangeDevice(), R.string.connect_device, DARKCYAN);
    }

    public void setMainDeviceConnected() {
        setColorAndText(getBtnChangeDevice(), R.string.change_device, DARKKHAKI);
    }

    //--------------------- search

    public void clearSearch() {
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
        freezeState(Tags.VIEW_MANAGER, getBtnDelContact(), getBtnSaveContact(), getBtnCancelContact());
    }

    public void setEditorButtonsRelease() {
        releaseState(Tags.VIEW_MANAGER);
    }

    public void setEditorFieldChanged() {
        enableGreen(getBtnSaveContact(), getBtnCancelContact());
    }

    //--------------------- ICallNetListener

    private void processNormal(CallerState from, CallerState to, ECallReport why) {
        if (debug) Log.i(TAG, "processNormal");
        switch (why) {
            case ExternalConnectorFail:
            case InternalConnectorFail:
            case InternalConnectorDisconnected:
            case InternalConnectorConnectedIncompatible:
            case InternalConnectorError:
                disableGray(getBtnGreen(), "ERROR");
                disableGray(getBtnRed(), "ERROR");
                break;
            case ExternalConnectorReady:
            case InternalConnectorReady:
            case InternalConnectorConnected:
            case InternalConnectorConnectedCompatible:
                if (to == ReadyToWork) {
                    enableBtnCall(getBtnGreen(), "CALL");
                    disableGray(getBtnRed(), "IDLE");
                }
                break;
            case CallFailedExternal:
            case CallFailedInternal:
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

    private void processAbnormal(CallerState from, CallerState to, ECallReport why) {
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
                                if (debug) Log.e(TAG, "startDebug " + to.getName());
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
    public void onCallerStateChange(CallerState from, CallerState to, ECallReport why) {
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
        if (a == null) {
            a = getAnimFromGetter(iGetter, id);
        }
        return a;
    }

    private <T extends View> T getView(T t, @IdRes int id) {
        if (t == null) {
            t = getViewFromGetter(iGetter, id);
        }
        return t;
    }

    private Animation getAnimFromGetter(IGetView iGetter, int id) {
        Animation a = null;
        if (iGetter != null) {
            a = iGetter.getAnimation(id);
            if (a == null) {
                if (debug) Log.w(TAG, "getAnimFromGetter anim is still null, return");
            }
        } else {
            Log.e(TAG, "getAnimFromGetter iGetter is null, return");
        }
        return a;
    }

    private <T extends View> T getViewFromGetter(IGetView iGetter, @IdRes int id) {
        T t = null;
        if (iGetter != null) {
            t = iGetter.getView(id);
            if (t == null) {
                if (debug) Log.w(TAG, "getViewFromGetter view is still null, return");
            }
        } else {
            Log.e(TAG, "getViewFromGetter iGetter is null, return");
        }
        return t;
    }

    //--------------------- getters

    private Animation getAnimCall() {
        return animCall = getAnim(animCall, R.anim.anim_call);
    }

    private View getScanView() {
        return scanView = getView(scanView, R.id.scanView);
    }

    private View getMainView() {
        return mainView = getView(mainView, R.id.mainView);
    }

    private View getViewContactEditor() {
        return viewContactEditor = getView(viewContactEditor, R.id.viewContactEditor);
    }

    private View getViewContactChosen() {
        return viewContactChosen = getView(viewContactChosen, R.id.viewContactChosen);
    }

    private EditText getEditTextSearch() {
        return editTextSearch = getView(editTextSearch, R.id.editTextSearch);
    }

    private TextView getTextViewContactChosenName() {
        return textViewContactChosenName = getView(textViewContactChosenName, R.id.textViewContactChosenName);
    }

    private TextView getTextViewContactChosenIp() {
        return textViewContactChosenIp = getView(textViewContactChosenIp, R.id.textViewContactChosenIp);
    }

    private EditText getEditTextContactName() {
        return editTextContactName = getView(editTextContactName, R.id.editTextContactName);
    }

    private EditText getEditTextContactIp() {
        return editTextContactIp = getView(editTextContactIp, R.id.editTextContactIp);
    }

    private Button getBtnSaveContact() {
        return btnSaveContact = getView(btnSaveContact, R.id.btnSaveContact);
    }

    private Button getBtnDelContact() {
        return btnDelContact = getView(btnDelContact, R.id.btnDelContact);
    }

    private Button getBtnCancelContact() {
        return btnCancelContact = getView(btnCancelContact, R.id.btnCancelContact);
    }

    private Button getBtnGreen() {
        return btnGreen = getView(btnGreen, R.id.btnGreen);
    }

    private Button getBtnRed() {
        return btnRed = getView(btnRed, R.id.btnRed);
    }

    private Button getBtnChangeDevice() {
        return btnChangeDevice = getView(btnChangeDevice, R.id.btnChangeDevice);
    }

}