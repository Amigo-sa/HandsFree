package by.citech.gui;

import android.content.Context;
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
import by.citech.logic.ICallNetListener;
import by.citech.param.Colors;
import by.citech.param.OpMode;
import by.citech.param.Settings;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;
import by.citech.util.Contacts;

public class ViewHelper
        implements ICallUiListener, ICallNetListener, IDebugListener, IBase {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.VIEW_HELPER;

    //--------------------- settings

    private OpMode opMode;

    {
        initiate();
    }

    private void initiate() {
        if (Settings.debug) Log.w(TAG,"INITIATIATE");
        takeSettings();
        applySettings();
        isInitiated = true;
    }

    private void takeSettings() {
        opMode = Settings.opMode;
    }

    private void applySettings() {
    }

    //--------------------- non-settings

    private IGetViewById iGetViewById;
    private View scanView;
    private View mainView;
    private View viewContactEditor;
    private View viewChosenContact;
    private EditText editTextSearch;
    private TextView textViewChosenContactName;
    private TextView textViewChosenContactIp;
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

    public ViewHelper(@NonNull IGetViewById iGetViewById,
                      @NonNull Context context) throws Exception {
        if (iGetViewById == null || context == null) {
            throw new Exception(StatusMessages.ERR_PARAMETERS);
        }
        this.iGetViewById = iGetViewById;
        buttonHelper = ButtonHelper.getInstance();
        animCall = AnimationUtils.loadAnimation(context, R.anim.anim_call);
        takeViews();
    }

    private void takeViews() {
        this.scanView = iGetViewById.findViewById(R.id.scanView);
        this.mainView = iGetViewById.findViewById(R.id.mainView);
        this.viewContactEditor = iGetViewById.findViewById(R.id.viewContactEditor);
        this.viewChosenContact = iGetViewById.findViewById(R.id.viewContactChosen);
        this.editTextSearch = iGetViewById.findViewById(R.id.editTextSearch);
        this.textViewChosenContactName = iGetViewById.findViewById(R.id.textViewChosenContactName);
        this.textViewChosenContactIp = iGetViewById.findViewById(R.id.textViewChosenContactIp);
        this.editTextContactName = iGetViewById.findViewById(R.id.editTextContactName);
        this.editTextContactIp = iGetViewById.findViewById(R.id.editTextContactIp);
        this.btnSaveContact = iGetViewById.findViewById(R.id.btnSaveContact);
        this.btnDelContact = iGetViewById.findViewById(R.id.btnDelContact);
        this.btnCancelContact = iGetViewById.findViewById(R.id.btnCancelContact);
        this.btnGreen = iGetViewById.findViewById(R.id.btnGreen);
        this.btnRed = iGetViewById.findViewById(R.id.btnRed);
        this.btnChangeDevice = iGetViewById.findViewById(R.id.btnChangeDevice);
    }

    public void setDefaultView() {
        if (Settings.debug) Log.i(TAG,"setDefaultView");
        if (!isInitiated) {
            initiate();
            if (Settings.debug) Log.w(TAG,"setDefaultView opMode is " + opMode.getSettingName());
        }
        switch (opMode) {
            case Bt2AudOut:
                enableBtnCall(btnGreen, "RECEIVING");
                ButtonHelper.disableGray(btnRed, "STOP");
                btnChangeDevice.setVisibility(View.VISIBLE);
                break;
            case AudIn2Bt:
                enableBtnCall(btnGreen, "TRANSMITTING");
                ButtonHelper.disableGray(btnRed, "STOP");
                btnChangeDevice.setVisibility(View.VISIBLE);
                break;
            case AudIn2AudOut:
                enableBtnCall(btnGreen, "PLAY");
                ButtonHelper.disableGray(btnRed, "STOP");
                btnChangeDevice.setVisibility(View.INVISIBLE);
                break;
            case Bt2Bt:
                enableBtnCall(btnGreen, "LBACK ON");
                ButtonHelper.disableGray(btnRed, "LBACK OFF");
                btnChangeDevice.setVisibility(View.VISIBLE);
                break;
            case Net2Net:
                enableBtnCall(btnGreen, "LBACK ON");
                ButtonHelper.disableGray(btnRed, "LBACK OFF");
                btnChangeDevice.setVisibility(View.INVISIBLE);
                break;
            case Record:
                enableBtnCall(btnGreen, "RECORD");
                ButtonHelper.disableGray(btnRed, "PLAY");
                btnChangeDevice.setVisibility(View.VISIBLE);
                break;
            case Normal:
            default:
                ButtonHelper.disableGray(btnGreen, "IDLE");
                ButtonHelper.disableGray(btnRed, "IDLE");
                btnChangeDevice.setVisibility(View.VISIBLE);
                break;
        }
        prepare();
    }

    @Override
    public void baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        scanView = null;
        mainView = null;
        viewContactEditor = null;
        viewChosenContact = null;
        editTextSearch = null;
        textViewChosenContactName = null;
        textViewChosenContactIp = null;
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
        iGetViewById = null;
        isCallAnim = false;
        isInitiated = false;
    }

    private void prepare() {
        animCall.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {if (isCallAnim) {startCallAnim();}}
            @Override public void onAnimationRepeat(Animation animation) {}});
    }

    public boolean isMainViewHidden() {
        return (mainView.getVisibility() != View.VISIBLE);
    }

    public boolean isScanViewHidden() {
        return (scanView.getVisibility() != View.VISIBLE);
    }

    public void showMainView() {
        mainView.setVisibility(View.VISIBLE);
        viewContactEditor.setVisibility(View.GONE);
        scanView.setVisibility(View.GONE);
    }

    public void showScaner() {
        mainView.setVisibility(View.GONE);
        scanView.setVisibility(View.VISIBLE);
    }

    //--------------------- chosen

    public void showChosen() {
        viewChosenContact.setVisibility(View.VISIBLE);
        editTextSearch.setVisibility(View.GONE);
    }

    public void hideChosen() {
        viewChosenContact.setVisibility(View.GONE);
        editTextSearch.setVisibility(View.VISIBLE);
    }

    public void setChosenContactInfo(Contact chosenContact) {
        Contacts.setContactInfo(chosenContact, textViewChosenContactName, textViewChosenContactIp);
    }

    public void clearChosenContactInfo() {
        Contacts.setContactInfo(textViewChosenContactName, textViewChosenContactIp);
    }


    public String getSearchText() {
        return editTextSearch.getText().toString();
    }

    //--------------------- editor

    public void showEditor() {
        mainView.setVisibility(View.GONE);
        viewContactEditor.setVisibility(View.VISIBLE);
    }

    public void hideEditor() {
        mainView.setVisibility(View.VISIBLE);
        viewContactEditor.setVisibility(View.GONE);
    }

    public String getEditorContactNameText() {
        return editTextContactName.getText().toString();
    }

    public String getEditorContactIpText() {
        return editTextContactIp.getText().toString();
    }

    public void setEditorAdd() {
        btnDelContact.setVisibility(View.GONE);
        btnSaveContact.setText("ADD");
        Contacts.setContactInfo(editTextContactName, editTextContactIp);
        ButtonHelper.disableGray(btnDelContact, btnSaveContact, btnCancelContact);
    }

    public void setEditorEdit(Contact contactToEdit) {
        btnSaveContact.setText("SAVE");
        Contacts.setContactInfo(contactToEdit, editTextContactName, editTextContactIp);
        ButtonHelper.enableGreen(btnDelContact);
        ButtonHelper.disableGray(btnSaveContact, btnCancelContact);
        btnDelContact.setVisibility(View.VISIBLE);
    }

    public void setEditorButtonsFreeze() {
        buttonHelper.freezeState(Tags.EDITOR_HELPER, btnDelContact, btnSaveContact, btnCancelContact);
    }

    public void setEditorButtonsRelease() {
        buttonHelper.releaseState(Tags.EDITOR_HELPER);
    }

    public void setEditorFieldChanged() {
        ButtonHelper.enableGreen(btnSaveContact, btnCancelContact);
    }

    //--------------------- ICallNetListener

    @Override
    public void callFailed() {
        if (debug) Log.i(TAG, "callFailed");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "FAIL");
    }

    @Override
    public void callEndedExternally() {
        if (debug) Log.i(TAG, "callEndedExternally");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "ENDED");
    }

    @Override
    public void callOutcomingConnected() {
        if (debug) Log.i(TAG, "callOutcomingConnected");
        enableBtnCall(btnGreen, "CALLING...");
        enableBtnCall(btnRed, "CANCEL");
    }

    @Override
    public void callOutcomingAccepted() {
        if (debug) Log.i(TAG, "callOutcomingAccepted");
        ButtonHelper.disableGray(btnGreen, "ON CALL");
        enableBtnCall(btnRed, "END CALL");
        stopCallAnim();
    }

    @Override
    public void callOutcomingRejected() {
        if (debug) Log.i(TAG, "callOutcomingRejected");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "BUSY");
        stopCallAnim();
    }

    @Override
    public void callOutcomingFailed() {
        if (debug) Log.i(TAG, "callOutcomingFailed");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "OFFLINE");
        stopCallAnim();
    }

    @Override
    public void callOutcomingInvalid() {
        if (debug) Log.i(TAG, "callOutcomingInvalid");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "INVALID");
        stopCallAnim();
    }

    @Override
    public void callIncomingDetected() {
        if (debug) Log.i(TAG, "callIncomingDetected");
        enableBtnCall(btnGreen, "INCOMING...");
        enableBtnCall(btnRed, "REJECT");
        startCallAnim();
    }

    @Override
    public void callIncomingCanceled() {
        if (debug) Log.i(TAG, "callIncomingCanceled");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "CANCELED");
        stopCallAnim();
    }

    @Override
    public void callIncomingFailed() {
        if (debug) Log.i(TAG, "callIncomingFailed");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "INCOME FAIL");
        stopCallAnim();
    }

    @Override
    public void connectorFailure() {
        if (debug) Log.e(TAG, "connectorFailure");
        ButtonHelper.disableGray(btnGreen, "ERROR");
        ButtonHelper.disableGray(btnRed, "ERROR");
    }

    @Override
    public void connectorReady() {
        if (debug) Log.i(TAG, "connectorReady");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "IDLE");
    }

    //--------------------- ICallUiListener

    @Override
    public void callOutcomingStarted() {
        if (debug) Log.i(TAG, "callOutcomingStarted");
        ButtonHelper.disableGray(btnGreen, "CALLING...");
        enableBtnCall(btnRed, "CANCEL");
        startCallAnim();
    }

    @Override
    public void callEndedInternally() {
        if (debug) Log.i(TAG, "callEndedInternally");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "ENDED");
    }

    @Override
    public void callOutcomingCanceled() {
        if (debug) Log.i(TAG, "callOutcomingCanceled");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "CANCELED");
        stopCallAnim();
    }

    @Override
    public void callIncomingRejected() {
        if (debug) Log.i(TAG, "callIncomingRejected");
        enableBtnCall(btnGreen, "CALL");
        ButtonHelper.disableGray(btnRed, "REJECTED");
        stopCallAnim();
    }

    @Override
    public void callIncomingAccepted() {
        if (debug) Log.i(TAG, "callIncomingAccepted");
        ButtonHelper.disableGray(btnGreen, "ON CALL");
        enableBtnCall(btnRed, "END CALL");
        stopCallAnim();
    }

    //--------------------- call buttons

    private int getColorBtnCall(Button button) {
        if (button == btnGreen) {
            return Colors.GREEN;
        } else if (button == btnRed) {
            return Colors.RED;
        } else {
            Log.e(TAG, "enableBtnCall color not defined");
            return Colors.GRAY;
        }
    }

    public void enableBtnCall(Button button) {
        ButtonHelper.enable(button, getColorBtnCall(button));
    }

    public void enableBtnCall(Button button, String label) {
        ButtonHelper.enable(button, getColorBtnCall(button), label);
    }

    public void startCallAnim() {
        if (debug && !isCallAnim) Log.i(TAG, "startCallAnim");
        btnGreen.startAnimation(animCall);
        isCallAnim = true;
    }

    private void stopCallAnim() {
        if (debug) Log.i(TAG, "stopCallAnim");
        btnGreen.clearAnimation();
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
                ButtonHelper.disableGray(btnGreen);
                enableBtnCall(btnRed);
                break;
            case Record:
                switch (getCallerState()) {
                    case DebugPlay:
                        ButtonHelper.disableGray(btnGreen, "PLAYING");
                        enableBtnCall(btnRed, "STOP");
                        break;
                    case DebugRecord:
                        ButtonHelper.disableGray(btnGreen, "RECORDING");
                        enableBtnCall(btnRed, "STOP");
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
                ButtonHelper.enableGreen(btnGreen);
                ButtonHelper.disableGray(btnRed);
                break;
            case Record:
                enableBtnCall(btnGreen, "PLAY");
                ButtonHelper.disableGray(btnRed, "RECORDED");
                break;
            case Normal:
            default:
                break;
        }
    }

}