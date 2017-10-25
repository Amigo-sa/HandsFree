package by.citech.logic;

import android.util.Log;

import by.citech.network.control.IConnCtrl;
import by.citech.network.control.TaskSendMessage;
import by.citech.param.Messages;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Caller {

    //--------------------- Call

    private <T extends IConnCtrl> void call(T ctrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "call");
        if (Settings.debug) Log.i(Tags.ACT_DPL, "call ctrl is instance of iServerCtrl: " + (ctrl == iServerCtrl));
        if (Settings.debug) Log.i(Tags.ACT_DPL, "call ctrl is instance of iClientCtrl: " + (ctrl == iClientCtrl));
        isOnCall = true;
        isIncomingCall = false;
        isOutcomingCall = false;
        isOutcomingConnection = false;
        btnSetEnabled(btnRed, "END CALL", RED);
        btnSetDisabled(btnGreen, "ON CALL", GRAY);
        callAnimStop();
        iConnCtrl = ctrl;
        enableTransmitData();
        exchangeStart(ctrl);
    }

    private void callEnd(IConnCtrl iConnCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callEnd");
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callEnd iConnCtrl is instance of iServerCtrl: " + (iConnCtrl == iServerCtrl));
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callEnd iConnCtrl is instance of iClientCtrl: " + (iConnCtrl == iClientCtrl));
        isOnCall = false;
        btnSetDisabled(btnRed, "ENDED", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        disableTransmitData();
        exchangeStop();
        disconnect(iConnCtrl);
    }

    private void callFailure(IConnCtrl iConnCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callFailure");
        isOnCall = false;
        btnSetDisabled(btnRed, "FAIL", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        disableTransmitData();
        exchangeStop();
        disconnect(iConnCtrl);
    }

    private void callOutcoming() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callOutcoming");
        if (isLocalIp()) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "callOutcoming isLocalIp");
            return;
        }
        isOutcomingCall = true;
        btnSetEnabled(btnRed, "CANCEL", RED);
        btnSetDisabled(btnGreen, "CALLING...", GRAY);
        callAnimStart();
        connect();
    }

    private void callOutcomingOnline() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callOutcomingOnline");
        isOutcomingCall = false;
        isOutcomingConnection = true;
        btnSetEnabled(btnRed, "CANCEL", RED);
        btnSetEnabled(btnGreen, "ONLINE...", GREEN);
    }

    private void callOutcomingRejected() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callOutcomingRejected");
        isOutcomingCall = false;
        isOutcomingConnection = false;
        btnSetDisabled(btnRed, "BUSY", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
    }

    private void callOutcomingCancel() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callOutcomingCancel");
        isOutcomingCall = false;
        isOutcomingConnection = false;
        btnSetDisabled(btnRed, "CANCELED", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
        disconnect(iClientCtrl);
    }

    private void callOutcomingFailure() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callOutcomingFailure");
        isOutcomingCall = false;
        btnSetDisabled(btnRed, "OFFLINE", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
    }

    private void callIncoming() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callIncoming");
        isIncomingCall = true;
        btnSetEnabled(btnRed, "REJECT", RED);
        btnSetEnabled(btnGreen, "ACCEPT", GREEN);
        callAnimStart();
    }

    private void callIncomingAccept() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callIncomingAccept");
        callIncomingAcceptSignal();
        call(iServerCtrl);
    }

    private void callIncomingReject() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callIncomingReject");
        isIncomingCall = false;
        btnSetDisabled(btnRed, "REJECTED", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
        serverDisc();
    }

    private void callIncomingCanceled() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callIncomingCanceled");
        isIncomingCall = false;
        btnSetDisabled(btnRed, "OFFLINE", GRAY);
        btnSetEnabled(btnGreen, "CALL", GREEN);
        callAnimStop();
    }

    private void callIncomingOnFailure() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callIncomingOnFailure");
        isIncomingCall = false;
        btnSetDisabled(btnRed, "INCOME FAIL", GRAY);
        btnSetEnabled(btnGreen, "CALL", RED);
        callAnimStop();
    }

    private void callIncomingAcceptSignal() {
        new TaskSendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.PASSWORD);
    }
}
