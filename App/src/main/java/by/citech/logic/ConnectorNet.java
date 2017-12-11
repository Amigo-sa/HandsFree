package by.citech.logic;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import by.citech.data.StorageData;
import by.citech.exchange.IReceiverCtrl;
import by.citech.exchange.RedirectFromNet;
import by.citech.exchange.ITransmitterCtrl;
import by.citech.gui.ICallUiListener;
import by.citech.network.INetInfoListener;
import by.citech.network.INetListener;
import by.citech.network.client.ClientConn;
import by.citech.network.client.IClientCtrl;
import by.citech.network.client.IClientCtrlReg;
import by.citech.network.control.IConnCtrl;
import by.citech.network.control.IDisc;
import by.citech.exchange.IMessage;
import by.citech.network.control.Disc;
import by.citech.exchange.SendMessage;
import by.citech.exchange.IReceiverCtrlReg;
import by.citech.exchange.ITransmitterCtrlReg;
import by.citech.exchange.RedirectToNet;
import by.citech.network.server.ServerOff;
import by.citech.network.server.ServerOn;
import by.citech.network.server.IServerCtrl;
import by.citech.network.server.IServerCtrlReg;
import by.citech.network.server.IServerOff;
import by.citech.param.Messages;
import by.citech.param.Settings;
import by.citech.param.Tags;
import by.citech.util.InetAddress;

import static by.citech.util.Network.getIpAddr;

public class ConnectorNet
        implements IServerCtrlReg, IReceiverCtrlReg, ITransmitterCtrlReg, IClientCtrlReg,
        IMessage, IServerOff, IDisc, INetListener, ICallUiListener, IBase {

    private static final String TAG = Tags.NET_CONNECTOR;
    private static final boolean debug = Settings.debug;

    private IServerCtrl iServerCtrl;
    private IClientCtrl iClientCtrl;
    private IReceiverCtrl iReceiverCtrl;
    private ITransmitterCtrl iTransmitterCtrl;
    private IConnCtrl iConnCtrl;
    private Handler handler;
    private INetInfoListener iNetInfoListener;
    private ArrayList<ICallNetListener> iCallNetListeners;
    private ArrayList<ICallNetExchangeListener> iCallNetExchangeListeners;
    private StorageData<byte[]> storageBtToNet;
    private StorageData<byte[][]> storageNetToBt;

    //--------------------- singleton

    private static volatile ConnectorNet instance = null;

    private ConnectorNet() {
        iCallNetListeners = new ArrayList<>();
        iCallNetExchangeListeners = new ArrayList<>();
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

    public ConnectorNet setiNetInfoListener(INetInfoListener iNetInfoListener) {
        this.iNetInfoListener = iNetInfoListener;
        return this;
    }

    public ConnectorNet addiCallNetworkExchangeListener(ICallNetExchangeListener iCallNetExchangeListener) {
        iCallNetExchangeListeners.add(iCallNetExchangeListener);
        return this;
    }

    public ConnectorNet addiCallNetworkListener(ICallNetListener iCallNetworkListener) {
        iCallNetListeners.add(iCallNetworkListener);
        iCallNetExchangeListeners.add(iCallNetworkListener);
        return this;
    }

    public ConnectorNet setStorageBtToNet(StorageData<byte[]> storageBtToNet) {
        this.storageBtToNet = storageBtToNet;
        return this;
    }

    public ConnectorNet setStorageNetToBt(StorageData<byte[][]> storageNetToBt) {
        this.storageNetToBt = storageNetToBt;
        return this;
    }

    private void setiConnCtrl(IConnCtrl iConnCtrl) {
        this.iConnCtrl = iConnCtrl;
    }

    //--------------------- main

    @Override
    public void baseStart(IBaseAdder iBaseAdder) {
        if (debug) Log.i(TAG, "baseStart");
        if (iBaseAdder == null) {
            Log.e(TAG, "baseStart iBaseAdder is null");
            return;
        } else {
            iBaseAdder.addBase(this);
        }
        new ServerOn(this, handler).execute(iNetInfoListener.getLocPort());
    }

    @Override
    public void baseStop() {
        new ThreadNetStop().start();
    }

    //--------------------- common

    private boolean setState(CallerState fromCallerState, CallerState toCallerState) {
        return Caller.getInstance().setState(fromCallerState, toCallerState);
    }

    private String getStateName() {
        return Caller.getInstance().getCallerState().getName();
    }

    private CallerState getState() {
        return Caller.getInstance().getCallerState();
    }

    //--------------------- ICallUiListener

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
            if (setState(CallerState.OutcomingStarted, CallerState.Idle))
                for (ICallNetListener listener : iCallNetListeners) listener.callOutcomingInvalid();
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
        switch (getState()) {
            case Idle:
                if (debug) Log.i(TAG, "srvOnOpen Idle");
                if (setState(CallerState.Idle, CallerState.IncomingDetected))
                    for (ICallNetListener listener : iCallNetListeners) listener.callIncomingDetected();
                break;
            default:
                if (debug) Log.e(TAG, "srvOnOpen " + getStateName());
                disconnect(iServerCtrl); // TODO: обрываем, если не ждём звонка?
        }
    }

    @Override
    public void srvOnFailure() {
        if (debug) Log.i(TAG, "srvOnFailure");
        switch (getState()) {
            case IncomingDetected:
                if (debug) Log.i(TAG, "srvOnFailure IncomingDetected");
                if (setState(CallerState.IncomingDetected, CallerState.Error))
                    for (ICallNetListener listener : iCallNetListeners) listener.callIncomingFailed();
                break;
            case Call:
                if (debug) Log.i(TAG, "srvOnFailure Call");
                if (setState(CallerState.Call, CallerState.Error))
                    for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callFailed();
                break;
            default:
                if (debug) Log.e(TAG, "srvOnFailure " + getStateName());
        }
    }

    @Override
    public void srvOnClose() {
        if (debug) Log.i(TAG, "srvOnClose");
        switch (getState()) {
            case IncomingDetected:
                if (debug) Log.i(TAG, "srvOnClose IncomingDetected");
                if (setState(CallerState.IncomingDetected, CallerState.Idle))
                    for (ICallNetListener listener : iCallNetListeners) listener.callIncomingCanceled();
                break;
            case Call:
                if (debug) Log.i(TAG, "srvOnClose Call");
                if (setState(CallerState.Call, CallerState.Idle))
                    for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callEndedExternally();
                exchangeStop();
                break;
            default:
                if (debug) Log.e(TAG, "srvOnClose " + getStateName());
        }
    }

    @Override
    public void cltOnOpen() {
        if (debug) Log.i(TAG, "cltOnOpen");
        switch (getState()) {
            case OutcomingStarted:
                if (debug) Log.i(TAG, "cltOnOpen Call");
                if (setState(CallerState.OutcomingStarted, CallerState.OutcomingConnected))
                    for (ICallNetListener listener : iCallNetListeners) listener.callOutcomingConnected();
                break;
            default:
                if (debug) Log.e(TAG, "cltOnOpen " + getStateName());
                disconnect(iClientCtrl); // TODO: обрываем, если не звонили?
        }
    }

    @Override
    public void cltOnFailure() {
        if (debug) Log.i(TAG, "cltOnFailure");
        switch (getState()) {
            case OutcomingConnected:
                if (debug) Log.i(TAG, "cltOnFailure OutcomingConnected");
                if (setState(CallerState.OutcomingConnected, CallerState.Idle))
                    for (ICallNetListener listener : iCallNetListeners) listener.callOutcomingRejected();
                break;
            case OutcomingStarted:
                if (debug) Log.i(TAG, "cltOnFailure OutcomingStarted");
                if (setState(CallerState.OutcomingStarted, CallerState.Error))
                    for (ICallNetListener listener : iCallNetListeners) listener.callOutcomingFailed();
                break;
            case Call:
                if (debug) Log.i(TAG, "cltOnFailure Call");
                if (setState(CallerState.Call, CallerState.Error))
                    for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callFailed();
                break;
            default:
                if (debug) Log.e(TAG, "cltOnFailure " + getStateName());
        }
    }

    @Override
    public void cltOnMessageText(String message) {
        switch (getState()) {
            case OutcomingConnected:
                if (debug) Log.i(TAG, "cltOnMessageText OutcomingConnected");
                if (message.equals(Messages.RESPONSE_ACCEPT)) {
                    if (debug) Log.i(TAG, "cltOnMessageText ACCEPT");
                    if (setState(CallerState.OutcomingConnected, CallerState.Call))
                        for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callOutcomingAccepted();
                    setiConnCtrl(iClientCtrl);
                    exchangeStart();
                } else if (message.equals(Messages.RESPONSE_REJECT)) {
                    if (debug) Log.i(TAG, "cltOnMessageText REJECT");
                    if (setState(CallerState.OutcomingConnected, CallerState.Idle))
                        for (ICallNetListener listener : iCallNetListeners) listener.callOutcomingRejected();
                    disconnect(iClientCtrl);
                }
        }
    }

    @Override
    public void cltOnClose() {
        if (debug) Log.i(TAG, "cltOnClose");
        switch (getState()) {
            case OutcomingConnected:
                if (debug) Log.i(TAG, "cltOnClose OutcomingConnected");
                if (setState(CallerState.OutcomingConnected, CallerState.Idle))
                    for (ICallNetListener listener : iCallNetListeners) listener.callOutcomingRejected();
                break;
            case Call:
                if (debug) Log.i(TAG, "cltOnClose Call");
                if (setState(CallerState.Call, CallerState.Idle))
                    for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callEndedExternally();
                break;
            default:
                if (debug) Log.e(TAG, "cltOnClose " + getStateName());
        }
    }

    //--------------------- network

    private void responseAccept() {
        if (debug) Log.i(TAG, "responseAccept");
        new SendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.RESPONSE_ACCEPT);
    }

    private boolean isValidIp() {
        if (debug) Log.i(TAG, "isValidIp");
        String remAddr = iNetInfoListener.getRemAddr();
        return !(remAddr.matches(getIpAddr(Settings.ipv4))
                || remAddr.matches("127.0.0.1")
                || !InetAddress.checkForValidityIpAddress(remAddr));
    }

    private void connect() {
        if (debug) Log.i(TAG, "connect");
        new ClientConn(this, handler).execute(String.format("ws://%s:%s",
                iNetInfoListener.getRemAddr(),
                iNetInfoListener.getRemPort()));
    }

    private void disconnect(IConnCtrl iConnCtrl) {
        if (debug) Log.i(TAG, "disconnect");
        if (iConnCtrl != null) {
            new Disc(this).execute(iConnCtrl);
        } else {
            if (debug) Log.e(TAG, "disconnect iConnCtrl is null");
        }
    }

    private void exchangeStart() {
        if (debug) Log.i(TAG, "exchangeStart");
        if (debug) Log.i(TAG, "exchangeStart iConnCtrl is instance of iServerCtrl: " + (iConnCtrl == iServerCtrl));
        if (debug) Log.i(TAG, "exchangeStart iConnCtrl is instance of iClientCtrl: " + (iConnCtrl == iClientCtrl));
        new RedirectToNet(this, iConnCtrl.getTransmitter(), storageBtToNet).execute();
        new RedirectFromNet(this, iConnCtrl.getReceiverReg(), storageNetToBt).execute();
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
        if (debug) Log.i(TAG, "ThreadNetStop iTransmitterCtrl.streamOff() done");
    }

    @Override
    public void serverStopped() {
        if (debug) Log.i(TAG, "serverStopped");
    }

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        if (debug) Log.i(TAG, "serverStarted");
        switch (getState()) {
            case Null:
                if (iServerCtrl == null) {
                    if (setState(CallerState.Null, CallerState.GeneralFailure))
                        for (ICallNetListener listener : iCallNetListeners)
                            listener.connectorFailure();
                } else {
                    if (setState(CallerState.Null, CallerState.Idle))
                        for (ICallNetListener listener : iCallNetListeners)
                            listener.connectorReady();
                    this.iServerCtrl = iServerCtrl;
                }
                break;
            default:
                Log.e(TAG, "serverStarted on state default");
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

    //TODO: нафиг это здесь?
    @Override
    public void messageSended() {
        if (debug) Log.i(TAG, "messageSended");
    }

    @Override
    public void messageCantSend() {
        if (debug) Log.i(TAG, "messageCantSend");
    }

    @Override
    public void disconnected() {
        if (debug) Log.i(TAG, "disconnected");
    }

}
