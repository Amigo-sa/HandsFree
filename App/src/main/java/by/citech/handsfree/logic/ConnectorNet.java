package by.citech.handsfree.logic;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.IReceiverCtrl;
import by.citech.handsfree.exchange.RedirectFromNet;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.gui.ICallToUiListener;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.network.INetListener;
import by.citech.handsfree.network.client.ClientConn;
import by.citech.handsfree.network.client.IClientCtrl;
import by.citech.handsfree.network.client.IClientCtrlReg;
import by.citech.handsfree.network.control.IConnCtrl;
import by.citech.handsfree.network.control.IDisc;
import by.citech.handsfree.exchange.IMessage;
import by.citech.handsfree.network.control.Disc;
import by.citech.handsfree.exchange.SendMessage;
import by.citech.handsfree.exchange.IReceiverCtrlReg;
import by.citech.handsfree.exchange.ITransmitterCtrlReg;
import by.citech.handsfree.exchange.RedirectToNet;
import by.citech.handsfree.network.server.ServerOff;
import by.citech.handsfree.network.server.ServerOn;
import by.citech.handsfree.network.server.IServerCtrl;
import by.citech.handsfree.network.server.IServerCtrlReg;
import by.citech.handsfree.network.server.IServerOff;
import by.citech.handsfree.param.Messages;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.util.InetAddress;

import static by.citech.handsfree.util.Network.getIpAddr;

public class ConnectorNet
        implements IServerCtrlReg, IReceiverCtrlReg, ITransmitterCtrlReg, IClientCtrlReg,
        IMessage, IServerOff, IDisc, INetListener, ICallToUiListener, IBase, ICaller {

    private static final String STAG = Tags.NET_CONNECTOR;
    private static final boolean debug = Settings.debug;

    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++; TAG = STAG + " " + objCount;}

    private IServerCtrl iServerCtrl;
    private IClientCtrl iClientCtrl;
    private IReceiverCtrl iReceiverCtrl;
    private ITransmitterCtrl iTransmitterCtrl;
    private IConnCtrl iConnCtrl;
    private Handler handler;
    private INetInfoGetter iNetInfoGetter;
    private ArrayList<ICallNetListener> iCalls;
    private ArrayList<ICallNetExchangeListener> iCallExs;
    private StorageData<byte[]> storageToNet;
    private StorageData<byte[][]> storageToBt;
    private boolean isBaseStop;

    //--------------------- singleton

    private static volatile ConnectorNet instance = null;

    private ConnectorNet() {
        iCalls = new ArrayList<>();
        iCallExs = new ArrayList<>();
    }

    public static ConnectorNet getInstance() {
        if (instance == null) {
            synchronized (ConnectorNet.class) {
                if (instance == null) {
                    instance = new ConnectorNet();
                }
            }
        }
        return instance;
    }

    //--------------------- getters and setters

    public ConnectorNet setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    public ConnectorNet setiNetInfoGetter(INetInfoGetter iNetInfoGetter) {
        this.iNetInfoGetter = iNetInfoGetter;
        return this;
    }

    public ConnectorNet addiCallNetworkExchangeListener(ICallNetExchangeListener iCallNetExchangeListener) {
        iCallExs.add(iCallNetExchangeListener);
        return this;
    }

    public ConnectorNet addiCallNetworkListener(ICallNetListener iCallNetworkListener) {
        iCalls.add(iCallNetworkListener);
        iCallExs.add(iCallNetworkListener);
        return this;
    }

    public ConnectorNet setStorageToNet(StorageData<byte[]> storageBtToNet) {
        this.storageToNet = storageBtToNet;
        return this;
    }

    public ConnectorNet setStorageFromNet(StorageData<byte[][]> storageNetToBt) {
        this.storageToBt = storageNetToBt;
        return this;
    }

    private void setiConnCtrl(IConnCtrl iConnCtrl) {
        this.iConnCtrl = iConnCtrl;
    }

    //--------------------- main

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        new ServerOn(this, handler).execute(iNetInfoGetter.getLocPort());
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        isBaseStop = true;
        new ThreadNetStop().start();
        return true;
    }

    private void finishBaseStop() {
        if (debug) Log.i(TAG, "finishBaseStop");
        iNetInfoGetter = null;
        if (iCallExs != null) {
            iCallExs.clear();
        }
        if (iCalls != null) {
            iCalls.clear();
        }
        handler = null;
        storageToNet = null;
        storageToBt = null;
        iServerCtrl = null;
        iClientCtrl = null;
        iReceiverCtrl = null;
        iTransmitterCtrl = null;
        iConnCtrl = null;
        isBaseStop = false;
        IBase.super.baseStop();
    }

    private void processReport(Report report) {
        if (debug) Log.i(TAG, "processReport");
        if (report == null || !isBaseStop) return;
        switch (report) {
            case ServerStopped:
                finishBaseStop();
                break;
            default:
                break;
        }
    }

    private enum Report {
        ServerStopped
    }

    //--------------------- ICallToUiListener

    @Override
    public void callEndedInternally() {
        if (debug) Log.i(TAG, "callEndedInternally");
        if (debug) Log.i(TAG, "callEndedInternally iConnCtrl is instance of iServerCtrl: " + (iConnCtrl == iServerCtrl));
        if (debug) Log.i(TAG, "callEndedInternally iConnCtrl is instance of iClientCtrl: " + (iConnCtrl == iClientCtrl));
        exchangeStop();
        disconnect(iConnCtrl);
    }

    @Override
    public void callOutcomingCanceled() {
        if (debug) Log.i(TAG, "callOutcomingCanceled");
        disconnect(iClientCtrl);
    }

    @Override
    public void callOutcomingStarted() {
        if (debug) Log.i(TAG, "callOutcomingStarted");
        if (!isValidIp()) {
            if (debug) Log.w(TAG, "callOutcomingStarted isValidIp()");
            if (setCallerState(CallerState.OutcomingStarted, CallerState.Idle)) for (ICallNetListener l : iCalls) l.callOutcomingInvalid();
            return;
        }
        connect();
    }

    @Override
    public void callIncomingRejected() {
        if (debug) Log.i(TAG, "callIncomingRejected");
        disconnect(iServerCtrl);
    }

    @Override
    public void callIncomingAccepted() {
        if (debug) Log.i(TAG, "callIncomingAccepted");
        setiConnCtrl(iServerCtrl);
        responseAccept();
        exchangeStart();
    }

    //--------------------- INetListener

    @Override
    public void srvOnOpen() {
        if (debug) Log.i(TAG, "srvOnOpen");
        switch (getCallerState()) {
            case Idle:
                if (debug) Log.i(TAG, "srvOnOpen Idle");
                if (setCallerState(CallerState.Idle, CallerState.IncomingDetected)) for (ICallNetListener l : iCalls) l.callIncomingDetected();
                break;
            default:
                if (debug) Log.e(TAG, "srvOnOpen " + getCallerStateName());
                disconnect(iServerCtrl); // TODO: обрываем, если не ждём звонка?
        }
    }

    @Override
    public void srvOnFailure() {
        if (debug) Log.i(TAG, "srvOnFailure");
        switch (getCallerState()) {
            case IncomingDetected:
                if (debug) Log.i(TAG, "srvOnFailure IncomingDetected");
                if (setCallerState(CallerState.IncomingDetected, CallerState.Error)) for (ICallNetListener l : iCalls) l.callIncomingFailed();
                break;
            case Call:
                if (debug) Log.i(TAG, "srvOnFailure Call");
                if (setCallerState(CallerState.Call, CallerState.Error)) for (ICallNetExchangeListener l : iCallExs) l.callFailed();
                exchangeStop();
                break;
            default:
                if (debug) Log.e(TAG, "srvOnFailure " + getCallerStateName());
        }
    }

    @Override
    public void srvOnClose() {
        if (debug) Log.i(TAG, "srvOnClose");
        switch (getCallerState()) {
            case IncomingDetected:
                if (debug) Log.i(TAG, "srvOnClose IncomingDetected");
                if (setCallerState(CallerState.IncomingDetected, CallerState.Idle)) for (ICallNetListener listener : iCalls) listener.callIncomingCanceled();
                break;
            case Call:
                if (debug) Log.i(TAG, "srvOnClose Call");
                if (setCallerState(CallerState.Call, CallerState.Idle)) for (ICallNetExchangeListener listener : iCallExs) listener.callEndedExternally();
                exchangeStop();
                break;
            default:
                if (debug) Log.e(TAG, "srvOnClose " + getCallerStateName());
        }
    }

    @Override
    public void cltOnOpen() {
        if (debug) Log.i(TAG, "cltOnOpen");
        switch (getCallerState()) {
            case OutcomingStarted:
                if (debug) Log.i(TAG, "cltOnOpen Call");
                if (setCallerState(CallerState.OutcomingStarted, CallerState.OutcomingConnected)) for (ICallNetListener l : iCalls) l.callOutcomingConnected();
                break;
            default:
                Log.e(TAG, "cltOnOpen " + getCallerStateName());
                disconnect(iClientCtrl); // TODO: обрываем, если не звонили?
                break;
        }
    }

    @Override
    public void cltOnFailure() {
        if (debug) Log.i(TAG, "cltOnFailure");
        switch (getCallerState()) {
            case OutcomingConnected:
                if (debug) Log.i(TAG, "cltOnFailure OutcomingConnected");
                if (setCallerState(CallerState.OutcomingConnected, CallerState.Idle)) for (ICallNetListener l : iCalls) l.callOutcomingRejected();
                break;
            case OutcomingStarted:
                if (debug) Log.i(TAG, "cltOnFailure OutcomingStarted");
                if (setCallerState(CallerState.OutcomingStarted, CallerState.Error)) for (ICallNetListener l : iCalls) l.callOutcomingFailed();
                break;
            case Call:
                if (debug) Log.i(TAG, "cltOnFailure Call");
                if (setCallerState(CallerState.Call, CallerState.Error)) for (ICallNetExchangeListener l : iCallExs) l.callFailed();
                exchangeStop();
                break;
            default:
                Log.e(TAG, "cltOnFailure " + getCallerStateName());
                break;
        }
    }

    @Override
    public void cltOnMessageText(String message) {
        switch (getCallerState()) {
            case OutcomingConnected:
                if (debug) Log.i(TAG, "cltOnMessageText OutcomingConnected");
                if (message.equals(Messages.RESPONSE_ACCEPT)) {
                    if (debug) Log.i(TAG, "cltOnMessageText ACCEPT");
                    if (setCallerState(CallerState.OutcomingConnected, CallerState.Call)) for (ICallNetExchangeListener l : iCallExs) l.callOutcomingAccepted();
                    setiConnCtrl(iClientCtrl);
                    exchangeStart();
                } else if (message.equals(Messages.RESPONSE_REJECT)) {
                    if (debug) Log.i(TAG, "cltOnMessageText REJECT");
                    if (setCallerState(CallerState.OutcomingConnected, CallerState.Idle)) for (ICallNetListener l : iCalls) l.callOutcomingRejected();
                    disconnect(iClientCtrl);
                }
        }
    }

    @Override
    public void cltOnClose() {
        if (debug) Log.i(TAG, "cltOnClose");
        switch (getCallerState()) {
            case OutcomingConnected:
                if (debug) Log.i(TAG, "cltOnClose OutcomingConnected");
                if (setCallerState(CallerState.OutcomingConnected, CallerState.Idle)) for (ICallNetListener l : iCalls) l.callOutcomingRejected();
                break;
            case Call:
                if (debug) Log.i(TAG, "cltOnClose Call");
                if (setCallerState(CallerState.Call, CallerState.Idle)) for (ICallNetExchangeListener l : iCallExs) l.callEndedExternally();
                break;
            default:
                if (debug) Log.e(TAG, "cltOnClose " + getCallerStateName());
        }
    }

    //--------------------- network

    private void responseAccept() {
        if (debug) Log.i(TAG, "responseAccept");
        new SendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.RESPONSE_ACCEPT);
    }

    private boolean isValidIp() {
        if (debug) Log.i(TAG, "isValidIp");
        String remAddr = iNetInfoGetter.getRemAddr();
        return !(remAddr.matches(getIpAddr(Settings.isIpv4Used))
                || remAddr.matches("127.0.0.1")
                || !InetAddress.checkForValidityIpAddress(remAddr));
    }

    private void connect() {
        if (debug) Log.i(TAG, "connect");
        new ClientConn(this, handler).execute(String.format("ws://%s:%s",
                iNetInfoGetter.getRemAddr(),
                iNetInfoGetter.getRemPort()));
    }

    private void disconnect(IConnCtrl iConnCtrl) {
        if (debug) Log.i(TAG, "disconnect");
        if (iConnCtrl != null) {
            new Disc(this).execute(iConnCtrl);
        } else {
            Log.e(TAG, "disconnect iConnCtrl is null");
        }
    }

    private void exchangeStart() {
        if (debug) Log.i(TAG, "exchangeStart");
        if (debug) Log.i(TAG, "exchangeStart iConnCtrl is instance of iServerCtrl: " + (iConnCtrl == iServerCtrl));
        if (debug) Log.i(TAG, "exchangeStart iConnCtrl is instance of iClientCtrl: " + (iConnCtrl == iClientCtrl));
        new RedirectToNet(this, iConnCtrl.getTransmitter(), storageToNet).execute();
        new RedirectFromNet(this, iConnCtrl.getReceiverReg(), storageToBt).execute();
    }

    private void exchangeStop() {
        if (debug) Log.i(TAG, "exchangeStop");
        new ThreadExchangeStop().start();
    }

    //--------------------- network low level

    private class ThreadExchangeStop
            extends Thread {
        @Override
        public void run() {
            if (debug) Log.i(TAG, "ThreadExchangeStop");
            streamOff();
            redirectOff();
        }
    }

    private class ThreadNetStop
            extends Thread {
        @Override
        public void run() {
            if (debug) Log.i(TAG, "ThreadNetStop");
            streamOff();
            redirectOff();
            disconnect(iClientCtrl);
            disconnect(iServerCtrl);
            serverOff();
        }
    }

    private void serverOff() {
        if (debug) Log.i(TAG, "serverOff");
        if (iServerCtrl != null) {
            new ServerOff(this).execute(iServerCtrl);
            iServerCtrl = null;
        }
    }

    private void redirectOff() {
        if (debug) Log.i(TAG, "redirectOff");
        if (iReceiverCtrl != null) {
            iReceiverCtrl.redirectOff();
            iReceiverCtrl = null;
        }
    }

    private void streamOff() {
        if (debug) Log.i(TAG, "streamOff");
        if (iTransmitterCtrl != null) {
            iTransmitterCtrl.streamOff();
            iTransmitterCtrl = null;
        }
        if (debug) Log.i(TAG, "streamOff done");
    }

    @Override
    public void serverStopped() {
        if (debug) Log.i(TAG, "serverStopped");
        processReport(Report.ServerStopped);
    }

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        if (debug) Log.i(TAG, "serverStarted");
        switch (getCallerState()) {
            case Null:
                if (iServerCtrl == null) {
                    if (setCallerState(CallerState.Null, CallerState.GeneralFailure)) for (ICallNetListener l : iCalls) l.connectorFailure();
                } else {
                    if (setCallerState(CallerState.Null, CallerState.Idle)) for (ICallNetListener l : iCalls) l.connectorReady();
                    this.iServerCtrl = iServerCtrl;
                }
                break;
            default:
                Log.e(TAG, "serverStarted state default");
                break;
        }
    }

    @Override
    public void registerReceiverCtrl(IReceiverCtrl iReceiverCtrl) {
        if (debug) Log.i(TAG, "registerReceiverCtrl");
        if (iReceiverCtrl == null) {
            if (debug) Log.e(TAG, "registerReceiverCtrl iReceiverCtrl is null");
        } else {
            this.iReceiverCtrl = iReceiverCtrl;
        }
    }

    @Override
    public void registerTransmitterCtrl(ITransmitterCtrl iTransmitterCtrl) {
        if (debug) Log.i(TAG, "registerTransmitterCtrl");
        if (iTransmitterCtrl == null) {
            if (debug) Log.e(TAG, "registerTransmitterCtrl iTransmitterCtrl is null");
        } else {
            this.iTransmitterCtrl = iTransmitterCtrl;
        }
    }

    @Override
    public void registerClientCtrl(IClientCtrl iClientCtrl) {
        if (debug) Log.i(TAG, "registerClientCtrl");
        if (iClientCtrl == null) {
            if (debug) Log.i(TAG, "registerClientCtrl iClientCtrl is null");
        } else {
            this.iClientCtrl = iClientCtrl;
        }
    }

}
