package by.citech.gui;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import by.citech.contact.Contact;
import by.citech.logic.ICallNetListener;
import by.citech.param.Colors;
import by.citech.param.OpMode;
import by.citech.param.Settings;
import by.citech.param.Tags;
import by.citech.util.Contacts;

public class ViewHelper implements ICallUiListener, ICallNetListener {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.VIEW_HELPER;

    //--------------------- settings

    private OpMode opMode;

    {
        initiate();
    }

    private void initiate() {
        takeSettings();
        applySettings();
    }

    private void takeSettings() {
        opMode = Settings.opMode;
    }

    private void applySettings() {
    }

    //--------------------- non-settings

    private View ScanView;
    private View MainView;
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
    private ButtonHelper buttonHelper;
    private Button btnGreen;
    private Button btnRed;
    private Animation animCall;
    private boolean isCallAnim;

    public ViewHelper(View scanView, View mainView, View viewContactEditor, View viewChosenContact,
                      EditText editTextSearch, TextView textViewChosenContactName, TextView textViewChosenContactIp,
                      EditText editTextContactName, EditText editTextContactIp, Button btnSaveContact,
                      Button btnDelContact, Button btnCancelContact, ButtonHelper buttonHelper, Button btnGreen,
                      Button btnRed, Animation animCall) {
        ScanView = scanView;
        MainView = mainView;
        this.viewContactEditor = viewContactEditor;
        this.viewChosenContact = viewChosenContact;
        this.editTextSearch = editTextSearch;
        this.textViewChosenContactName = textViewChosenContactName;
        this.textViewChosenContactIp = textViewChosenContactIp;
        this.editTextContactName = editTextContactName;
        this.editTextContactIp = editTextContactIp;
        this.btnSaveContact = btnSaveContact;
        this.btnDelContact = btnDelContact;
        this.btnCancelContact = btnCancelContact;
        this.buttonHelper = buttonHelper;
        this.btnGreen = btnGreen;
        this.btnRed = btnRed;
        this.animCall = animCall;
    }

    public void setDefaultView() {
        switch (opMode) {
            case Bt2AudOut:
            case AudIn2Bt:
            case AudIn2AudOut:
                btnCallSetEnabled(btnGreen, "PLAY");
                ButtonHelper.disable(btnRed, "STOP");
                break;
            case Bt2Bt:
            case Net2Net:
                btnCallSetEnabled(btnGreen, "LBACK ON");
                ButtonHelper.disable(btnRed, "LBACK OFF");
                break;
            case Record:
                btnCallSetEnabled(btnGreen, "RECORD");
                ButtonHelper.disable(btnRed, "PLAY");
                break;
            case Normal:
            default:
                ButtonHelper.disable(btnGreen, "IDLE");
                ButtonHelper.disable(btnRed, "IDLE");
                break;
        }
    }

    public boolean isMainViewHidden() {
        return (MainView.getVisibility() != View.VISIBLE);
    }

    public boolean isScanViewHidden() {
        return (ScanView.getVisibility() != View.VISIBLE);
    }

    public void showMainView() {
        MainView.setVisibility(View.VISIBLE);
        viewContactEditor.setVisibility(View.GONE);
        ScanView.setVisibility(View.GONE);
    }

    public void showEditor() {
        MainView.setVisibility(View.GONE);
        viewContactEditor.setVisibility(View.VISIBLE);
    }

    public void showScaner() {
        MainView.setVisibility(View.GONE);
        ScanView.setVisibility(View.VISIBLE);
    }

    public void hideEditor() {
        MainView.setVisibility(View.VISIBLE);
        viewContactEditor.setVisibility(View.GONE);
    }

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
        ButtonHelper.disable(btnDelContact, btnSaveContact, btnCancelContact);
    }

    public void setEditorEdit(Contact contactToEdit) {
        btnSaveContact.setText("SAVE");
        Contacts.setContactInfo(contactToEdit, editTextContactName, editTextContactIp);
        ButtonHelper.enable(btnDelContact);
        ButtonHelper.disable(btnSaveContact, btnCancelContact);
        btnDelContact.setVisibility(View.VISIBLE);
    }

    public void setEditorButtonsFreeze() {
        buttonHelper.freezeState(Tags.EDITOR_HELPER, btnDelContact, btnSaveContact, btnCancelContact);
    }

    public void setEditorButtonsRelease() {
        buttonHelper.releaseState(Tags.EDITOR_HELPER);
    }

    public void setEditorFieldChanged() {
        ButtonHelper.enable(btnSaveContact, btnCancelContact);
    }


    //--------------------- ICallNetListener

    @Override
    public void callFailed() {
        if (debug) Log.i(TAG, "callFailed");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "FAIL");
    }

    @Override
    public void callEndedExternally() {
        if (debug) Log.i(TAG, "callEndedExternally");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "ENDED");
    }

    @Override
    public void callOutcomingConnected() {
        if (debug) Log.i(TAG, "callOutcomingConnected");
        btnCallSetEnabled(btnGreen, "CALLING...");
        btnCallSetEnabled(btnRed, "CANCEL");
    }

    @Override
    public void callOutcomingAccepted() {
        if (debug) Log.i(TAG, "callOutcomingAccepted");
        ButtonHelper.disable(btnGreen, "ON CALL");
        btnCallSetEnabled(btnRed, "END CALL");
        stopCallAnim();
    }

    @Override
    public void callOutcomingRejected() {
        if (debug) Log.i(TAG, "callOutcomingRejected");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "BUSY");
        stopCallAnim();
    }

    @Override
    public void callOutcomingFailed() {
        if (debug) Log.i(TAG, "callOutcomingFailed");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "OFFLINE");
        stopCallAnim();
    }

    @Override
    public void callOutcomingInvalid() {
        if (debug) Log.i(TAG, "callOutcomingInvalid");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "INVALID");
        stopCallAnim();
    }

    @Override
    public void callIncomingDetected() {
        if (debug) Log.i(TAG, "callIncomingDetected");
        btnCallSetEnabled(btnGreen, "INCOMING...");
        btnCallSetEnabled(btnRed, "REJECT");
        startCallAnim();
    }

    @Override
    public void callIncomingCanceled() {
        if (debug) Log.i(TAG, "callIncomingCanceled");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "CANCELED");
        stopCallAnim();
    }

    @Override
    public void callIncomingFailed() {
        if (debug) Log.i(TAG, "callIncomingFailed");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "INCOME FAIL");
        stopCallAnim();
    }

    @Override
    public void connectorFailure() {
        if (debug) Log.e(TAG, "connectorFailure");
        ButtonHelper.disable(btnGreen, "ERROR");
        ButtonHelper.disable(btnRed, "ERROR");
    }

    @Override
    public void connectorReady() {
        if (debug) Log.i(TAG, "connectorReady");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "IDLE");
    }

    //--------------------- ICallUiListener

    @Override
    public void callOutcomingStarted() {
        if (debug) Log.i(TAG, "callOutcomingStarted");
        ButtonHelper.disable(btnGreen, "CALLING...");
        btnCallSetEnabled(btnRed, "CANCEL");
        startCallAnim();
    }

    @Override
    public void callEndedInternally() {
        if (debug) Log.i(TAG, "callEndedInternally");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "ENDED");
    }

    @Override
    public void callOutcomingCanceled() {
        if (debug) Log.i(TAG, "callOutcomingCanceled");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "CANCELED");
        stopCallAnim();
    }

    @Override
    public void callIncomingRejected() {
        if (debug) Log.i(TAG, "callIncomingRejected");
        btnCallSetEnabled(btnGreen, "CALL");
        ButtonHelper.disable(btnRed, "REJECTED");
        stopCallAnim();
    }

    @Override
    public void callIncomingAccepted() {
        if (debug) Log.i(TAG, "callIncomingAccepted");
        ButtonHelper.disable(btnGreen, "ON CALL");
        btnCallSetEnabled(btnRed, "END CALL");
        stopCallAnim();
    }

    //--------------------- stopDebug


    public void setStopDebug() {
        ButtonHelper.enable(btnGreen);
        ButtonHelper.disable(btnRed);
    }

    public void setStartDebug() {
        ButtonHelper.disable(btnGreen);
        ButtonHelper.enable(btnRed);
    }

    //--------------------- startDebug

    //--------------------- record

    public void setRecordPlaying() {
        ButtonHelper.disable(btnGreen, "PLAYING");
        btnCallSetEnabled(btnRed, "STOP");
    }


    public void setRecordRecording() {
        ButtonHelper.disable(btnGreen, "RECORDING");
        btnCallSetEnabled(btnRed, "STOP");
    }

    public void setRecordStop() {
        btnCallSetEnabled(btnGreen, "PLAY");
        ButtonHelper.disable(btnRed, "RECORDED");
    }

    //--------------------- call buttons

    public void btnCallSetEnabled(Button button, String label) {
        int color;
        if (button == btnGreen) {
            color = Colors.GREEN;
        } else if (button == btnRed) {
            color = Colors.RED;
        } else {
            color = Colors.GRAY;
            Log.e(TAG, "btnCallSetEnabled color not defined");
        }
        ButtonHelper.enable(button, label, color);
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

}