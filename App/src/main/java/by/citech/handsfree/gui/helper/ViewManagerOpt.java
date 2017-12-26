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
import by.citech.handsfree.debug.IDebugCtrl;
import by.citech.handsfree.logic.CallerState;
import by.citech.handsfree.logic.ECallReport;
import by.citech.handsfree.logic.ICallToUiListener;
import by.citech.handsfree.gui.IGetView;
import by.citech.handsfree.gui.IGetViewGetter;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.logic.ICallNetListener;
import by.citech.handsfree.logic.ICaller;
import by.citech.handsfree.logic.ICallerFsmListener;
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

public class ViewManagerOpt
        implements IBase, ISettingsCtrl, ICaller, IPrepareObject, IViewKeeper, ICallerFsmListener {

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
        return iGetGetter != null;
    }

    @Override
    public boolean takeSettings() {
        opMode = Settings.getInstance().getCommon().getOpMode();
        return true;
    }

    //--------------------- non-settings

    private IGetViewGetter iGetGetter;
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

    public ViewManagerOpt setiGetGetter(IGetViewGetter iGetGetter) {
        this.iGetGetter = iGetGetter;
        return this;
    }

    //--------------------- base

    @Override
    public boolean baseCreate() {
        IBase.super.baseCreate();
        if (debug) Log.i(TAG, "baseCreate");
        return true;
    }

    @Override
    public boolean baseDestroy() {
        if (debug) Log.i(TAG, "baseDestroy");
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
        iGetGetter = null;
        isCallAnim = false;
        IBase.super.baseDestroy();
        return true;
    }

    //--------------------- main

    public void setDefaultView() {
        if (debug) Log.i(TAG, "setDefaultView");
        prepareObject();
//      takeViews();

        setColorAndText(getBtnChangeDevice(), R.string.connect_device, DARKCYAN);

        switch (opMode) {
            case Bt2AudOut:
                enableBtnCall(getBtnGreen(), "RECEIVING");
                disableGray(getBtnRed(), "STOP");
                getBtnChangeDevice().setVisibility(View.VISIBLE);
                break;
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
                disableGray(getBtnGreen(), "IDLE");
                disableGray(getBtnRed(), "IDLE");
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

    @Override
    public void onCallerStateChange(CallerState from, CallerState to, ECallReport why) {
        switch (why) {
            case CallFailedExternal:
            case CallFailedInternal:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "FAIL");
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
                disableGray(getBtnRed(), "INVALID");
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
            case ExternalConnectorFail:
                disableGray(getBtnGreen(), "NET ERROR");
                disableGray(getBtnRed(), "NET ERROR");
                break;
            case ExternalConnectorReady:
                enableBtnCall(getBtnGreen(), "CALL");
                disableGray(getBtnRed(), "IDLE");
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
            case StartDebug:
                switch (opMode) {
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
                    case Normal:
                    default:
                        break;
                }
            case StopDebug:
                switch (opMode) {
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
                    case Normal:
                    default:
                        break;
                }
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

    private IGetView getGetter(IGetViewGetter iGetGetter) {
        if (iGetter == null) {
            if (debug) Log.w(TAG, "getGetter iGetter is null, get");
            if (iGetGetter == null) {
                Log.e(TAG, "getGetter iGetGetter is null, return");
            } else {
                iGetter = iGetGetter.getViewGetter();
                if (iGetter == null) {
                    Log.e(TAG, "getGetter iGetter is still null, return");
                }
            }
        }
        return iGetter;
    }

    private Animation getAnim(Animation a, int id) {
        if (a == null) {
            if (debug) Log.w(TAG, "getAnim anim is null, get");
            a = getAnimFromGetter(getGetter(iGetGetter), id);
        }
        return a;
    }

    private Animation getAnimFromGetter(IGetView iGetter, int id) {
        Animation a = null;
        if (iGetter != null) {
            a = iGetter.getAnimation(id);
            if (a == null) {
                if (debug) Log.w(TAG, "getAnimFromGetter anim is still null, return");
            } else {
                if (debug) Log.i(TAG, "getAnimFromGetter anim is " + a);
            }
        } else {
            Log.e(TAG, "getAnimFromGetter iGetter is null, return");
        }
        return a;
    }

    private <T extends View> T getView(T t, @IdRes int id) {
        if (t == null) {
            if (debug) Log.w(TAG, "getView view is null, get");
            t = getViewFromGetter(getGetter(iGetGetter), id);
        }
        return t;
    }

    private <T extends View> T getViewFromGetter(IGetView iGetter, @IdRes int id) {
        T t = null;
        if (iGetter != null) {
            t = iGetter.getView(id);
            if (t == null) {
                if (debug) Log.w(TAG, "getViewFromGetter view is still null, return");
            } else {
                if (debug) Log.i(TAG, "getViewFromGetter view is " + t);
            }
        } else {
            Log.e(TAG, "getViewFromGetter iGetter is null, return");
        }
        return t;
    }

    private void takeViews() {
        if (debug) Log.w(TAG, "takeViews");
        getBtnChangeDevice();
        getBtnCancelContact();
        getBtnSaveContact();
        getBtnDelContact();
        getBtnGreen();
        getBtnRed();
        getTextViewContactChosenIp();
        getTextViewContactChosenName();
        getViewContactChosen();
        getViewContactEditor();
        getEditTextContactIp();
        getEditTextContactName();
        getEditTextSearch();
        getMainView();
        getScanView();
    }

    //--------------------- getters

    private Animation getAnimCall() {
        if (animCall == null) animCall = getAnim(animCall, R.anim.anim_call);
        return animCall;
    }

    private View getScanView() {
        if (scanView == null) scanView = getView(scanView, R.id.scanView);
        return scanView;
    }

    private View getMainView() {
        if (mainView == null) mainView = getView(mainView, R.id.mainView);
        return mainView;
    }

    private View getViewContactEditor() {
        if (viewContactEditor == null) viewContactEditor = getView(viewContactEditor, R.id.viewContactEditor);
        return viewContactEditor;
    }

    private View getViewContactChosen() {
        if (viewContactChosen == null) viewContactChosen = getView(viewContactChosen, R.id.viewContactChosen);
        return viewContactChosen;
    }

    private EditText getEditTextSearch() {
        if (editTextSearch == null) editTextSearch = getView(editTextSearch, R.id.editTextSearch);
        return editTextSearch;
    }

    private TextView getTextViewContactChosenName() {
        if (textViewContactChosenName == null) textViewContactChosenName = getView(textViewContactChosenName, R.id.textViewContactChosenName);
        return textViewContactChosenName;
    }

    private TextView getTextViewContactChosenIp() {
        if (textViewContactChosenIp == null) textViewContactChosenIp = getView(textViewContactChosenIp, R.id.textViewContactChosenIp);
        return textViewContactChosenIp;
    }

    private EditText getEditTextContactName() {
        if (editTextContactName == null) editTextContactName = getView(editTextContactName, R.id.editTextContactName);
        return editTextContactName;
    }

    private EditText getEditTextContactIp() {
        if (editTextContactIp == null) editTextContactIp = getView(editTextContactIp, R.id.editTextContactIp);
        return editTextContactIp;
    }

    private Button getBtnSaveContact() {
        if (btnSaveContact == null) btnSaveContact = getView(btnSaveContact, R.id.btnSaveContact);
        return btnSaveContact;
    }

    private Button getBtnDelContact() {
        if (btnDelContact == null) btnDelContact = getView(btnDelContact, R.id.btnDelContact);
        return btnDelContact;
    }

    private Button getBtnCancelContact() {
        if (btnCancelContact == null) btnCancelContact = getView(btnCancelContact, R.id.btnCancelContact);
        return btnCancelContact;
    }

    private Button getBtnGreen() {
        if (btnGreen == null) btnGreen = getView(btnGreen, R.id.btnGreen);
        return btnGreen;
    }

    private Button getBtnRed() {
        if (btnRed == null) btnRed = getView(btnRed, R.id.btnRed);
        return btnRed;
    }

    private Button getBtnChangeDevice() {
        if (btnChangeDevice == null) btnChangeDevice = getView(btnChangeDevice, R.id.btnChangeDevice);
        return btnChangeDevice;
    }

}