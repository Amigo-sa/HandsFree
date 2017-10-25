package by.citech.logic;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import by.citech.DeviceControlActivity;
import by.citech.network.client.asynctask.TaskClientConn;
import by.citech.network.client.connection.IClientCtrl;
import by.citech.network.control.IConnCtrl;
import by.citech.network.control.IExchangeCtrl;
import by.citech.network.control.TaskDisc;
import by.citech.network.control.redirect.IRedirectCtrl;
import by.citech.network.control.redirect.TaskRedirect;
import by.citech.network.control.stream.IStreamCtrl;
import by.citech.network.control.stream.TaskStream;
import by.citech.network.server.asynctask.TaskServerOff;
import by.citech.network.server.connection.IServerCtrl;
import by.citech.param.Messages;
import by.citech.param.Settings;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

import static by.citech.util.NetworkInfo.getIPAddress;

public class Connector {

    //--------------------- Network

    boolean isLocalIp() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "isLocalIp");
        return (editTextSrvRemAddr.getText().toString().equals(getIPAddress(Settings.ipv4)) ||
                editTextSrvRemAddr.getText().toString().equals("127.0.0.1") ||
                editTextSrvRemAddr.getText().toString().equals("localhost"));
    }

    private void srvOnOpen() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "srvOnOpen");
        if (!isOutcomingCall && !isIncomingCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "srvOnOpen !isOutcomingCall && !isIncomingCall");
            callIncoming();
        } else {
            if (Settings.debug) Log.e(Tags.ACT_DPL, "srvOnOpen isOutcomingCall || isIncomingCall");
            serverDisc();
        }
    }

    private void srvOnFailure() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "srvOnFailure");
        if (isIncomingCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "srvOnFailure isIncomingCall");
            callIncomingOnFailure();
        } else if (isOnCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "srvOnFailure isOnCall");
            callFailure(iServerCtrl);
        }
    }

    private void srvOnClose() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "srvOnClose");
        if (isIncomingCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "srvOnClose isIncomingCall");
            callIncomingCanceled();
        } else if (isOnCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "srvOnClose isOnCall");
            callEnd(iServerCtrl);
        }
    }

    private void cltOnOpen() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnOpen");
        if (isOutcomingCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnOpen isOutcomingCall");
            callOutcomingOnline();
        } else {
            if (Settings.debug) Log.e(Tags.ACT_DPL, "cltOnOpen !isOutcomingCall");
            clientDisc();
        }
    }

    private void cltOnFailure() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnFailure");
        if (isOutcomingCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnFailure isOutcomingCall");
            callOutcomingFailure();
        } else if (isOnCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnFailure isOnCall");
            callFailure(iClientCtrl);
        }
    }

    private void cltOnMessageText(String message) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnMessageText");
        if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnMessageText message is: " + message);
        if (isOutcomingConnection) {
            if (message.equals(Messages.PASSWORD)) {
                call(iClientCtrl);
            }
        }
    }

    private void cltOnClose() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnClose");
        if (isOutcomingCall || isOutcomingConnection) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnClose isOutcomingCall");
            callOutcomingRejected();
        } else if (isOnCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "cltOnClose isOnCall");
            callEnd(iClientCtrl);
        }
    }

    private class HandlerExtended extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StatusMessages.SRV_ONMESSAGE:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONMESSAGE");
                    //if (Settings.debug) Log.i(Tags.ACT_DPL, String.format("handleMessage SRV_ONMESSAGE %s", ((WebSocketFrame) msg.obj).getTextPayload()));
                    break;
                case StatusMessages.SRV_ONCLOSE:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONCLOSE");
                    srvOnClose();
                    break;
                case StatusMessages.SRV_ONOPEN:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONOPEN");
                    srvOnOpen();
                    break;
                case StatusMessages.SRV_ONPONG:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONPONG");
                    break;
                case StatusMessages.SRV_ONFAILURE:
                    if (Settings.debug) Log.e(Tags.ACT_DPL, "handleMessage SRV_ONFAILURE");
                    srvOnFailure();
                    break;
                case StatusMessages.SRV_ONDEBUGFRAMERX:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONDEBUGFRAMERX");
                    break;
                case StatusMessages.SRV_ONDEBUGFRAMETX:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONDEBUGFRAMETX");
                    break;
                case StatusMessages.CLT_ONOPEN:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONOPEN");
                    cltOnOpen();
                    break;
                case StatusMessages.CLT_ONMESSAGE_BYTES:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONMESSAGE_BYTES");
                    break;
                case StatusMessages.CLT_ONMESSAGE_TEXT:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONMESSAGE_TEXT");
                    cltOnMessageText((String) msg.obj);
                    break;
                case StatusMessages.CLT_ONCLOSING:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONCLOSING");
                    break;
                case StatusMessages.CLT_ONCLOSED:
                    cltOnClose();
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_ONCLOSED");
                    break;
                case StatusMessages.CLT_ONFAILURE:
                    cltOnFailure();
                    if (Settings.debug) Log.e(Tags.ACT_DPL, "handleMessage CLT_ONFAILURE");
                    break;
                case StatusMessages.CLT_CANCEL:
                    if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage CLT_CANCEL");
                    break;
                default:
                    if (Settings.debug) Log.e(Tags.ACT_DPL, "handleMessage DEFAULT");
                    break;
            }
        }
    }

    private void connect() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "connect");
        new TaskClientConn(DeviceControlActivity.this, handler).execute(String.format("ws://%s:%s",
                editTextSrvRemAddr.getText().toString(),
                editTextSrvRemPort.getText().toString()));
    }

    private void disconnect(IConnCtrl iConnCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "disconnect");
        new TaskDisc(this).execute(iConnCtrl);
    }

    private void exchangeStart(IExchangeCtrl ctrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "exchangeStart");
        new TaskStream(DeviceControlActivity.this, ctrl.getTransmitter(), Settings.dataSource, storageBtToNet).execute();
        new TaskRedirect(DeviceControlActivity.this, ctrl.getReceiverRegister(), Settings.dataSource, storageNetToBt).execute();
    }

    private void exchangeStop() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "exchangeStop");
        new DeviceControlActivity.ThreadExchangeStop().start();
    }

    private class ThreadExchangeStop extends Thread {
        @Override
        public void run() {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "ThreadExchangeStop");
            streamOff();
            redirectOff();
        }
    }

    private class ThreadDisc extends Thread {
        @Override
        public void run() {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "ThreadDisc");
            clientDisc();
            serverDisc();
        }
    }

    private class ThreadNetStop extends Thread {
        @Override
        public void run() {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "ThreadNetStop");
            streamOff();
            redirectOff();
            clientDisc();
            serverDisc();
            serverOff();
        }
    }

    private void serverDisc() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "serverDisc");
        if (iServerCtrl != null) {
            new TaskDisc(DeviceControlActivity.this).execute(iServerCtrl);
        }
    }

    private void serverOff() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "serverOff");
        if (iServerCtrl != null) {
            new TaskServerOff(DeviceControlActivity.this).execute(iServerCtrl);
            iServerCtrl = null;
        }
    }

    private void clientDisc() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "clientDisc");
        if (iClientCtrl != null) {
            new TaskDisc(DeviceControlActivity.this).execute(iClientCtrl);
            iClientCtrl = null;
        }
    }

    private void redirectOff() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "redirectOff");
        if (iRedirectCtrl != null) {
            iRedirectCtrl.redirectOff();
            iRedirectCtrl = null;
        }
    }

    private void streamOff() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "streamOff");
        if (iStreamCtrl != null) {
            iStreamCtrl.streamOff();
            iStreamCtrl = null;
        }
        if (Settings.debug) Log.i(Tags.ACT_DPL, "ThreadNetStop iStreamCtrl.streamOff() done");
    }

    @Override
    public void serverStopped() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "serverStopped");
    }

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "serverStarted");
        if (iServerCtrl == null) {
            btnSetDisabled(btnGreen, "IDLE", GRAY);
        } else {
            btnSetEnabled(btnGreen, "CALL", GREEN);
            this.iServerCtrl = iServerCtrl;
        }
    }

    @Override
    public void registerRedirectCtrl(IRedirectCtrl iRedirectCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "registerRedirectCtrl");
        if (iRedirectCtrl == null) {
            if (Settings.debug) Log.e(Tags.ACT_DPL, "registerRedirectCtrl iRedirectCtrl is null");
        } else {
            this.iRedirectCtrl = iRedirectCtrl;
        }
    }

    @Override
    public void registerStreamCtrl(IStreamCtrl iStreamCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "registerStreamCtrl");
        if (iStreamCtrl == null) {
            if (Settings.debug) Log.e(Tags.ACT_DPL, "registerStreamCtrl iStreamCtrl is null");
        } else {
            this.iStreamCtrl = iStreamCtrl;
        }
    }

    @Override
    public void registerClientCtrl(IClientCtrl iClientCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "registerClientCtrl");
        if (iClientCtrl == null) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "registerClientCtrl iClientCtrl is null");
        } else {
            this.iClientCtrl = iClientCtrl;
        }
    }

    @Override
    public void messageSended() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "messageSended");
    }

    @Override
    public void messageCantSend() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "messageCantSend");
    }

    @Override
    public void disconnected() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "disconnected");
    }
}
