package by.citech.gui;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import by.citech.R;
import by.citech.contact.Contact;
import by.citech.debug.IDebugListener;
import by.citech.logic.Caller;
import by.citech.logic.CallerState;
import by.citech.logic.IBase;
import by.citech.logic.IBaseAdder;
import by.citech.logic.ICallNetListener;
import by.citech.param.Colors;
import by.citech.param.ISettings;
import by.citech.param.OpMode;
import by.citech.param.Settings;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;
import by.citech.util.Contacts;

public class ViewHelper
        implements ICallUiListener, ICallNetListener, IDebugListener, IBase, ISettings {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.VIEW_HELPER;

    //--------------------- debug

    private static int objCount;

    static {
        if (debug) Log.i(TAG,"static initiate");
        objCount = 0;
    }

    //--------------------- settings

    private OpMode opMode;

    {
        if (debug) Log.i(TAG,"non-static initiate");
        objCount++;
        initiate();
    }

    @Override
    public void initiate() {
        if (debug) Log.i(TAG,"initiate");
        takeSettings();
        isInitiated = true;
    }

    @Override
    public void takeSettings() {
        opMode = Settings.opMode;
    }

    //--------------------- non-settings

    private final int objNumber;
    private IGetViewGetter iGetGetter;
    private IGetViewById iGetter;
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
    private ButtonHelper buttonHelper;
    private Animation animCall;
    private boolean isCallAnim;
    private boolean isInitiated;

    public ViewHelper(@NonNull IGetViewGetter iGetGetter,
                      @NonNull Context context) throws Exception {
        if (debug) Log.i(TAG, "object count is " + objCount);
        if (iGetGetter == null || context == null) {
            throw new Exception(StatusMessages.ERR_PARAMETERS);
        }
        objNumber = objCount;
        if (debug) Log.w(TAG, "this object's number is " + objNumber);
        this.iGetGetter = iGetGetter;
        buttonHelper = ButtonHelper.getInstance();
        animCall = AnimationUtils.loadAnimation(context, R.anim.anim_call);
    }

    @Override
    public void baseStart(IBaseAdder iBaseAdder) {
        if (debug) Log.i(TAG, "baseStart");
        if (iBaseAdder == null) {
            Log.e(TAG, "baseStart iBaseAdder is null");
            return;
        } else {
            iBaseAdder.addBase(this);
        }
        if (!isInitiated) {
            initiate();
        }
        setDefaultView();
    }

    @Override
    public void baseStop() {
        if (debug) Log.i(TAG, "baseStop");
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
        buttonHelper = null;
        animCall = null;
        iGetter = null;
        iGetGetter = null;
        isCallAnim = false;
        isInitiated = false;
    }

    private void setDefaultView() {
        if (debug) Log.i(TAG, "setDefaultView");
        if (!isInitiated) {
            initiate();
            if (debug) Log.w(TAG, "setDefaultView opMode is " + opMode.getSettingName());
        }
        switch (opMode) {
            case Bt2AudOut:
                enableBtnCall(getBtnGreen(), "RECEIVING");
                ButtonHelper.disableGray(getBtnRed(), "STOP");
                getBtnChangeDevice().setVisibility(View.VISIBLE);
                break;
            case AudIn2Bt:
                enableBtnCall(getBtnGreen(), "TRANSMITTING");
                ButtonHelper.disableGray(getBtnRed(), "STOP");
                getBtnChangeDevice().setVisibility(View.VISIBLE);
                break;
            case AudIn2AudOut:
                enableBtnCall(getBtnGreen(), "PLAY");
                ButtonHelper.disableGray(getBtnRed(), "STOP");
                getBtnChangeDevice().setVisibility(View.INVISIBLE);
                break;
            case Bt2Bt:
                enableBtnCall(getBtnGreen(), "LBACK ON");
                ButtonHelper.disableGray(getBtnRed(), "LBACK OFF");
                getBtnChangeDevice().setVisibility(View.VISIBLE);
                break;
            case Net2Net:
                enableBtnCall(getBtnGreen(), "LBACK ON");
                ButtonHelper.disableGray(getBtnRed(), "LBACK OFF");
                getBtnChangeDevice().setVisibility(View.INVISIBLE);
                break;
            case Record:
                enableBtnCall(getBtnGreen(), "RECORD");
                ButtonHelper.disableGray(getBtnRed(), "PLAY");
                getBtnChangeDevice().setVisibility(View.VISIBLE);
                break;
            case Normal:
            default:
                ButtonHelper.disableGray(getBtnGreen(), "IDLE");
                ButtonHelper.disableGray(getBtnRed(), "IDLE");
                getBtnChangeDevice().setVisibility(View.VISIBLE);
                break;
        }
        prepare();
    }

    private void prepare() {
        animCall.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {if (isCallAnim) {startCallAnim();}}
            @Override public void onAnimationRepeat(Animation animation) {}});
    }

    public boolean isMainViewHidden() {
        return (getMainView().getVisibility() != View.VISIBLE);
    }

    public boolean isScanViewHidden() {
        return (getScanView().getVisibility() != View.VISIBLE);
    }

    public void showMainView() {
        getMainView().setVisibility(View.VISIBLE);
        getViewContactEditor().setVisibility(View.GONE);
        getScanView().setVisibility(View.GONE);
    }

    public void showScaner() {
        getMainView().setVisibility(View.GONE);
        getScanView().setVisibility(View.VISIBLE);
    }

    //--------------------- chosen

    public void showChosen() {
        getViewContactChosen().setVisibility(View.VISIBLE);
        getEditTextSearch().setVisibility(View.GONE);
    }

    public void hideChosen() {
        getViewContactChosen().setVisibility(View.GONE);
        getEditTextSearch().setVisibility(View.VISIBLE);
    }

    public void setChosenContactInfo(Contact chosenContact) {
        Contacts.setContactInfo(chosenContact, getTextViewContactChosenName(), getTextViewContactChosenIp());
    }

    public void clearChosenContactInfo() {
        Contacts.setContactInfo(getTextViewContactChosenName(), getTextViewContactChosenIp());
    }

    public String getSearchText() {
        return getEditTextSearch().getText().toString();
    }

    //--------------------- editor

    public void showEditor() {
        getMainView().setVisibility(View.GONE);
        getViewContactEditor().setVisibility(View.VISIBLE);
    }

    public void hideEditor() {
        getMainView().setVisibility(View.VISIBLE);
        getViewContactEditor().setVisibility(View.GONE);
    }

    public String getEditorContactNameText() {
        return getEditTextContactName().getText().toString();
    }

    public String getEditorContactIpText() {
        return getEditTextContactIp().getText().toString();
    }

    public void setEditorAdd() {
        getBtnDelContact().setVisibility(View.GONE);
        getBtnSaveContact().setText("ADD");
        Contacts.setContactInfo(getEditTextContactName(), getEditTextContactIp());
        ButtonHelper.disableGray(getBtnDelContact(), getBtnSaveContact(), getBtnCancelContact());
    }

    public void setEditorEdit(Contact contactToEdit) {
        getBtnSaveContact().setText("SAVE");
        Contacts.setContactInfo(contactToEdit, getEditTextContactName(), getEditTextContactIp());
        ButtonHelper.enableGreen(getBtnDelContact());
        ButtonHelper.disableGray(getBtnSaveContact(), getBtnCancelContact());
        getBtnDelContact().setVisibility(View.VISIBLE);
    }

    public void setEditorButtonsFreeze() {
        buttonHelper.freezeState(Tags.EDITOR_HELPER, getBtnDelContact(), getBtnSaveContact(), getBtnCancelContact());
    }

    public void setEditorButtonsRelease() {
        buttonHelper.releaseState(Tags.EDITOR_HELPER);
    }

    public void setEditorFieldChanged() {
        ButtonHelper.enableGreen(getBtnSaveContact(), getBtnCancelContact());
    }

    //--------------------- ICallNetListener

    @Override
    public void callFailed() {
        if (debug) Log.i(TAG, "callFailed");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "FAIL");
    }

    @Override
    public void callEndedExternally() {
        if (debug) Log.i(TAG, "callEndedExternally");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "ENDED");
    }

    @Override
    public void callOutcomingConnected() {
        if (debug) Log.i(TAG, "callOutcomingConnected");
        enableBtnCall(getBtnGreen(), "CALLING...");
        enableBtnCall(getBtnRed(), "CANCEL");
    }

    @Override
    public void callOutcomingAccepted() {
        if (debug) Log.i(TAG, "callOutcomingAccepted");
        ButtonHelper.disableGray(getBtnGreen(), "ON CALL");
        enableBtnCall(getBtnRed(), "END CALL");
        stopCallAnim();
    }

    @Override
    public void callOutcomingRejected() {
        if (debug) Log.i(TAG, "callOutcomingRejected");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "BUSY");
        stopCallAnim();
    }

    @Override
    public void callOutcomingFailed() {
        if (debug) Log.i(TAG, "callOutcomingFailed");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "OFFLINE");
        stopCallAnim();
    }

    @Override
    public void callOutcomingInvalid() {
        if (debug) Log.i(TAG, "callOutcomingInvalid");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "INVALID");
        stopCallAnim();
    }

    @Override
    public void callIncomingDetected() {
        if (debug) Log.i(TAG, "callIncomingDetected");
        enableBtnCall(getBtnGreen(), "INCOMING...");
        enableBtnCall(getBtnRed(), "REJECT");
        startCallAnim();
    }

    @Override
    public void callIncomingCanceled() {
        if (debug) Log.i(TAG, "callIncomingCanceled");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "CANCELED");
        stopCallAnim();
    }

    @Override
    public void callIncomingFailed() {
        if (debug) Log.i(TAG, "callIncomingFailed");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "INCOME FAIL");
        stopCallAnim();
    }

    @Override
    public void connectorFailure() {
        if (debug) Log.e(TAG, "connectorFailure");
        ButtonHelper.disableGray(getBtnGreen(), "ERROR");
        ButtonHelper.disableGray(getBtnRed(), "ERROR");
    }

    @Override
    public void connectorReady() {
        if (debug) Log.i(TAG, "connectorReady");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "IDLE");
    }

    //--------------------- ICallUiListener

    @Override
    public void callOutcomingStarted() {
        if (debug) Log.i(TAG, "callOutcomingStarted");
        ButtonHelper.disableGray(getBtnGreen(), "CALLING...");
        enableBtnCall(getBtnRed(), "CANCEL");
        startCallAnim();
    }

    @Override
    public void callEndedInternally() {
        if (debug) Log.i(TAG, "callEndedInternally");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "ENDED");
    }

    @Override
    public void callOutcomingCanceled() {
        if (debug) Log.i(TAG, "callOutcomingCanceled");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "CANCELED");
        stopCallAnim();
    }

    @Override
    public void callIncomingRejected() {
        if (debug) Log.i(TAG, "callIncomingRejected");
        enableBtnCall(getBtnGreen(), "CALL");
        ButtonHelper.disableGray(getBtnRed(), "REJECTED");
        stopCallAnim();
    }

    @Override
    public void callIncomingAccepted() {
        if (debug) Log.i(TAG, "callIncomingAccepted");
        ButtonHelper.disableGray(getBtnGreen(), "ON CALL");
        enableBtnCall(getBtnRed(), "END CALL");
        stopCallAnim();
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
        ButtonHelper.enable(button, getColorBtnCall(button));
    }

    private void enableBtnCall(Button button, String label) {
        ButtonHelper.enable(button, getColorBtnCall(button), label);
    }

    private void startCallAnim() {
        if (debug && !isCallAnim) Log.i(TAG, "startCallAnim");
        getBtnGreen().startAnimation(animCall);
        isCallAnim = true;
    }

    private void stopCallAnim() {
        if (debug) Log.i(TAG, "stopCallAnim");
        getBtnGreen().clearAnimation();
        isCallAnim = false;
    }

    //--------------------- IDebugListener

    private CallerState getCallerState() {
        return Caller.getInstance().getCallerState();
    }

    private String getCallerStateName() {
        return Caller.getInstance().getCallerState().getName();
    }

    @Override
    public void startDebug() {
        switch (opMode) {
            case Bt2AudOut:
            case AudIn2Bt:
            case AudIn2AudOut:
            case Bt2Bt:
            case Net2Net:
                ButtonHelper.disableGray(getBtnGreen());
                enableBtnCall(getBtnRed());
                break;
            case Record:
                switch (getCallerState()) {
                    case DebugPlay:
                        ButtonHelper.disableGray(getBtnGreen(), "PLAYING");
                        enableBtnCall(getBtnRed(), "STOP");
                        break;
                    case DebugRecord:
                        ButtonHelper.disableGray(getBtnGreen(), "RECORDING");
                        enableBtnCall(getBtnRed(), "STOP");
                        break;
                    default:
                        if (debug) Log.e(TAG, "startDebug " + getCallerStateName());
                        break;
                }
                break;
            case Normal:
            default:
                break;
        }
    }

    @Override
    public void stopDebug() {
        switch (opMode) {
            case Bt2AudOut:
            case AudIn2Bt:
            case AudIn2AudOut:
            case Bt2Bt:
            case Net2Net:
                ButtonHelper.enableGreen(getBtnGreen());
                ButtonHelper.disableGray(getBtnRed());
                break;
            case Record:
                enableBtnCall(getBtnGreen(), "PLAY");
                ButtonHelper.disableGray(getBtnRed(), "RECORDED");
                break;
            case Normal:
            default:
                break;
        }
    }

    //--------------------- view getters help

    private <T extends View> T getView(T t, @IdRes int id) {
        if (t == null) {
            Log.e(TAG, "getView view is null, get");
            t = getViewFromGetter(getGetter(iGetGetter), id);
        }
        return t;
    }

    private IGetViewById getGetter(IGetViewGetter iGetGetter) {
        if (iGetter == null) {
            Log.e(TAG, "getGetter iGetter is null, get");
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

    private <T extends View> T getViewFromGetter(IGetViewById iGetter, @IdRes int id) {
        T t = null;
        if (iGetter != null) {
            t = iGetter.findViewById(id);
            if (t == null) {
                Log.e(TAG, "getViewFromGetter view is still null, return");
            } else {
                if (debug) Log.i(TAG, "getViewFromGetter view is " + t);
            }
        } else {
            Log.e(TAG, "getViewFromGetter iGetter is null, return");
        }
        return t;
    }

    //--------------------- view getters

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