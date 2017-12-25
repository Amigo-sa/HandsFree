package by.citech.handsfree.logic;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.IReceiverCtrl;
import by.citech.handsfree.exchange.RedirectFromNet;
import by.citech.handsfree.exchange.ITransmitterCtrl;
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
import by.citech.handsfree.threading.IThreadManager;
import by.citech.handsfree.util.InetAddress;

import static by.citech.handsfree.util.Network.getIpAddr;

public class ConnectorNet
        implements IServerCtrlReg, IReceiverCtrlReg, ITransmitterCtrlReg, IClientCtrlReg,
        IMessage, IServerOff, IDisc, INetListener, ICallToUiListener, IBase, ICaller, IThreadManager {

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
    private StorageData<byte[][]> storageFromNet;
    private boolean isBaseStop;

    //--------------------- runnables

    private Runnable exchangeStop = new Runnable() {
        @Override
        public void run() {
            if (debug) Log.i(TAG, "exchangeStop run");
            streamOff();
            redirectOff();
        }
    };

    private Runnable netStop = new Runnable() {
        @Override
        public void run() {
            if (debug) Log.i(TAG, "netStop run");
            streamOff();
            redirectOff();
            disconnect(iClientCtrl);
            disconnect(iServerCtrl);
            serverOff();
        }
    };

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

    ConnectorNet setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    ConnectorNet setiNetInfoGetter(INetInfoGetter iNetInfoGetter) {
        this.iNetInfoGetter = iNetInfoGetter;
        return this;
    }

    ConnectorNet addiCallNetworkExchangeListener(ICallNetExchangeListener listener) {
        iCallExs.add(listener);
        return this;
    }

    ConnectorNet addiCallNetworkListener(ICallNetListener listener) {
        iCalls.add(listener);
        iCallExs.add(listener);
        return this;
    }

    ConnectorNet setStorageToNet(StorageData<byte[]> storageBtToNet) {
        this.storageToNet = storageBtToNet;
        return this;
    }

    ConnectorNet setStorageFromNet(StorageData<byte[][]> storageNetToBt) {
        this.storageFromNet = storageNetToBt;
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
        addRunnable(netStop);
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
        storageFromNet = null;
        iServerCtrl = null;
        iClientCtrl = null;
        iReceiverCtrl = null;
        iTransmitterCtrl = null;
        iConnCtrl = null;
        isBaseStop = false;
        IBase.super.baseStop();
    }

    private void procReport(Report report) {
        if (debug) Log.i(TAG, "procReport");
        if (report == null) return;
        switch (report) {
            case ServerStopped:
                if (isBaseStop) {
                    finishBaseStop();
                }
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
        if (debug) Log.i(TAG, String.format(Locale.US,
                "callEndedInternally iConnCtrl is instance of %s",
                (iConnCtrl == iServerCtrl) ? "iServerCtrl" :
                (iConnCtrl == iClientCtrl) ? "iClientCtrl" : "unknown"));
        exchangeStop();
        disconnect(iConnCtrl);
    }

    @Override
    public void callOutcomingCanceled() {
        if (debug) Log.i(TAG, "callOutcomingCanceled");
        disconnect(iClientCtrl);
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

    @Override
    public void callOutcomingStarted() {
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "callOutcomingStarted callerState is " + callerState.getName());
        switch (callerState) {
            case OutcomingStarted:
                if (!isValidIp()) {
                    if (debug) Log.w(TAG, "callOutcomingStarted invalid ip");
                    if (setCallerState(CallerState.OutcomingStarted, CallerState.Idle)) {for (ICallNetListener l : iCalls) l.callOutcomingInvalid(); return;}
                    else break;
                } else {
                    connect();
                    return;
                }
            default:
                if (debug) Log.w(TAG, "callOutcomingStarted " + callerState.getName()); return;
        }
        if (debug) Log.w(TAG, "callOutcomingStarted recursive call");
        callOutcomingStarted();
    }

    //--------------------- INetListener

    @Override
    public void srvOnOpen() {
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "srvOnOpen callerState is " + callerState.getName());
        switch (callerState) {
            case Idle:
                if (debug) Log.i(TAG, "srvOnOpen Idle");
                if (setCallerState(CallerState.Idle, CallerState.IncomingDetected)) {for (ICallNetListener l : iCalls) l.callIncomingDetected(); return;}
                else break;
            default:
                if (debug) Log.w(TAG, "srvOnOpen " + callerState.getName());
                disconnect(iServerCtrl); // TODO: обрываем, если не ждём звонка?
                return;
        }
        if (debug) Log.w(TAG, "srvOnOpen recursive call");
        srvOnOpen();
    }

    @Override
    public void srvOnFailure() {
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "srvOnFailure callerState is " + callerState.getName());
        switch (callerState) {
            case IncomingDetected:
                if (debug) Log.i(TAG, "srvOnFailure IncomingDetected");
                if (setCallerState(CallerState.IncomingDetected, CallerState.Error)) {for (ICallNetListener l : iCalls) l.callIncomingFailed(); return;}
                else break;
            case Call:
                if (debug) Log.i(TAG, "srvOnFailure Call");
                if (setCallerState(CallerState.Call, CallerState.Error)) {
                    for (ICallNetExchangeListener l : iCallExs) l.callFailed();
                    exchangeStop();
                    return;
                }
                else break;
            default:
                if (debug) Log.w(TAG, "srvOnFailure " + callerState.getName()); return;
        }
        if (debug) Log.w(TAG, "srvOnFailure recursive call");
        srvOnFailure();
    }

    @Override
    public void srvOnClose() {
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "srvOnClose callerState is " + callerState.getName());
        switch (callerState) {
            case IncomingDetected:
                if (debug) Log.i(TAG, "srvOnClose IncomingDetected");
                if (setCallerState(CallerState.IncomingDetected, CallerState.Idle)) {for (ICallNetListener listener : iCalls) listener.callIncomingCanceled(); return;}
                else break;
            case Call:
                if (debug) Log.i(TAG, "srvOnClose Call");
                if (setCallerState(CallerState.Call, CallerState.Idle)) {
                    for (ICallNetExchangeListener listener : iCallExs) listener.callEndedExternally();
                    exchangeStop();
                    return;
                }
                else break;
            default:
                if (debug) Log.e(TAG, "srvOnClose " + callerState.getName()); return;
        }
        if (debug) Log.w(TAG, "srvOnClose recursive call");
        srvOnClose();
    }

    @Override
    public void cltOnOpen() {
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "cltOnOpen callerState is " + callerState.getName());
        switch (callerState) {
            case OutcomingStarted:
                if (setCallerState(CallerState.OutcomingStarted, CallerState.OutcomingConnected)) {for (ICallNetListener l : iCalls) l.callOutcomingConnected(); return;}
                else break;
            default:
                if (debug) Log.w(TAG, "cltOnOpen " + callerState.getName());
                disconnect(iClientCtrl); // TODO: обрываем, если не звонили?
                return;
        }
        if (debug) Log.w(TAG, "cltOnOpen recursive call");
        cltOnOpen();
    }

    @Override
    public void cltOnFailure() {
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "cltOnFailure callerState is " + callerState.getName());
        switch (callerState) {
            case OutcomingConnected:
                if (debug) Log.i(TAG, "cltOnFailure OutcomingConnected");
                if (setCallerState(CallerState.OutcomingConnected, CallerState.Idle)) {for (ICallNetListener l : iCalls) l.callOutcomingRejected(); return;}
                else break;
            case OutcomingStarted:
                if (debug) Log.i(TAG, "cltOnFailure OutcomingStarted");
                if (setCallerState(CallerState.OutcomingStarted, CallerState.Error)) {for (ICallNetListener l : iCalls) l.callOutcomingFailed(); return;}
                else break;
            case Call:
                if (debug) Log.i(TAG, "cltOnFailure Call");
                if (setCallerState(CallerState.Call, CallerState.Error)) {
                    for (ICallNetExchangeListener l : iCallExs) l.callFailed();
                    exchangeStop();
                    return;
                }
                else break;
            default:
                Log.e(TAG, "cltOnFailure " + callerState.getName()); return;
        }
        if (debug) Log.w(TAG, "cltOnFailure recursive call");
        cltOnFailure();
    }

    @Override
    public void cltOnMessageText(String message) {
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "cltOnMessageText callerState is " + callerState.getName());
        switch (callerState) {
            case OutcomingConnected:
                if (message.equals(Messages.RESPONSE_ACCEPT)) {
                    if (debug) Log.i(TAG, "cltOnMessageText RESPONSE_ACCEPT");
                    if (setCallerState(CallerState.OutcomingConnected, CallerState.Call)) {
                        for (ICallNetExchangeListener l : iCallExs) l.callOutcomingAccepted();
                        setiConnCtrl(iClientCtrl);
                        exchangeStart();
                    } else {
                        break;
                    }
                } else if (message.equals(Messages.RESPONSE_REJECT)) {
                    if (debug) Log.i(TAG, "cltOnMessageText RESPONSE_REJECT");
                    if (setCallerState(CallerState.OutcomingConnected, CallerState.Idle)) {
                        for (ICallNetListener l : iCalls) l.callOutcomingRejected();
                        disconnect(iClientCtrl);
                    } else {
                        break;
                    }
                }
                return;
            default:
                if (debug) Log.w(TAG, "cltOnMessageText " + callerState.getName()); return;
        }
        if (debug) Log.w(TAG, "cltOnMessageText recursive call");
        cltOnMessageText(message);
    }

    @Override
    public void cltOnClose() {
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "cltOnClose callerState is " + callerState.getName());
        switch (callerState) {
            case OutcomingConnected:
                if (setCallerState(CallerState.OutcomingConnected, CallerState.Idle)) {for (ICallNetListener l : iCalls) l.callOutcomingRejected(); return;}
                else break;
            case Call:
                if (setCallerState(CallerState.Call, CallerState.Idle)) {for (ICallNetExchangeListener l : iCallExs) l.callEndedExternally(); return;}
                else break;
            default:
                if (debug) Log.w(TAG, "cltOnClose " + callerState.getName()); return;
        }
        if (debug) Log.w(TAG, "cltOnClose recursive call");
        cltOnClose();
    }

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "serverStarted callerState is " + callerState.getName());
        switch (callerState) {
            case Null:
                if (iServerCtrl == null) {
                    if (setCallerState(CallerState.Null, CallerState.GeneralFailure)) {for (ICallNetListener l : iCalls) l.connectorFailure(); return;}
                } else {
                    if (setCallerState(CallerState.Null, CallerState.Idle)) {for (ICallNetListener l : iCalls) l.connectorReady();
                        this.iServerCtrl = iServerCtrl;
                        return;
                    }
                }
                break;
            default:
                if (debug) Log.w(TAG, "serverStarted " + callerState.getName()); return;
        }
        if (debug) Log.w(TAG, "serverStarted recursive call");
        serverStarted(iServerCtrl);
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
        if (debug) Log.i(TAG, String.format(Locale.US,
                "exchangeStart iConnCtrl is instance of %s",
                (iConnCtrl == iServerCtrl) ? "iServerCtrl" :
                (iConnCtrl == iClientCtrl) ? "iClientCtrl" : "unknown"));
        new RedirectToNet(this, iConnCtrl.getTransmitter(), storageToNet).execute();
        new RedirectFromNet(this, iConnCtrl.getReceiverReg(), storageFromNet).execute();
    }

    private void exchangeStop() {
        if (debug) Log.i(TAG, "exchangeStop");
        addRunnable(exchangeStop);
    }

    //--------------------- network low level

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
        procReport(Report.ServerStopped);
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
