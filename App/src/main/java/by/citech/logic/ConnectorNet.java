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

import static by.citech.util.NetworkInfo.getIpAddr;

public class ConnectorNet
        implements IServerCtrlReg, IReceiverCtrlReg, ITransmitterCtrlReg, IClientCtrlReg,
        IMessage, IServerOff, IDisc, INetListener, ICallUiListener {

    private IServerCtrl iServerCtrl;
    private IClientCtrl iClientCtrl;
    private IReceiverCtrl iReceiverCtrl;
    private ITransmitterCtrl iTransmitterCtrl;
    private IConnCtrl iConnCtrl;
    private Handler handler;
    private INetInfoListener iNetInfoListener;
    private ArrayList<ICallNetListener> iCallNetworkListeners;
    private ArrayList<ICallNetExchangeListener> iCallNetExchangeListeners;
    private StorageData<byte[]> storageBtToNet;
    private StorageData<byte[][]> storageNetToBt;

    //--------------------- singleton

    private static volatile ConnectorNet instance = null;

    private ConnectorNet() {
        iCallNetworkListeners = new ArrayList<>();
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
        iCallNetworkListeners.add(iCallNetworkListener);
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

    public void build() {
        new ServerOn(this, handler).execute(iNetInfoListener.getLocPort());
    }

    public void stop() {
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
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "callEndedInternally");
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "callEndedInternally iConnCtrl is instance of iServerCtrl: " + (iConnCtrl == iServerCtrl));
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "callEndedInternally iConnCtrl is instance of iClientCtrl: " + (iConnCtrl == iClientCtrl));
        exchangeStop();
        disconnect(iConnCtrl);
    }

    @Override
    public void callOutcomingCanceled() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "callOutcomingCanceled");
        disconnect(iClientCtrl);
    }

    @Override
    public void callOutcomingStarted() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "callOutcomingStarted");
        if (!isValidIp()) {
            if (Settings.debug) Log.w(Tags.NET_CONNECTOR, "callOutcomingStarted isValidIp()");
            if (setState(CallerState.OutcomingStarted, CallerState.Idle))
                for (ICallNetListener listener : iCallNetworkListeners) listener.callOutcomingInvalid();
            return;
        }
        connect();
    }

    @Override
    public void callIncomingRejected() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "callIncomingRejected");
        disconnect(iServerCtrl);
    }

    @Override
    public void callIncomingAccepted() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "callIncomingAccepted");
        setiConnCtrl(iServerCtrl);
        responseAccept();
        exchangeStart();
    }

    //--------------------- INetListener

    @Override
    public void srvOnOpen() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnOpen");
        switch (getState()) {
            case Idle:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnOpen Idle");
                if (setState(CallerState.Idle, CallerState.IncomingDetected))
                    for (ICallNetListener listener : iCallNetworkListeners) listener.callIncomingDetected();
                break;
            default:
                if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "srvOnOpen " + getStateName());
                disconnect(iServerCtrl); // TODO: обрываем, если не ждём звонка?
        }
    }

    @Override
    public void srvOnFailure() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnFailure");
        switch (getState()) {
            case IncomingDetected:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnFailure IncomingDetected");
                if (setState(CallerState.IncomingDetected, CallerState.Error))
                    for (ICallNetListener listener : iCallNetworkListeners) listener.callIncomingFailed();
                break;
            case Call:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnFailure Call");
                if (setState(CallerState.Call, CallerState.Error))
                    for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callFailed();
                break;
            default:
                if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "srvOnFailure " + getStateName());
        }
    }

    @Override
    public void srvOnClose() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnClose");
        switch (getState()) {
            case IncomingDetected:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnClose IncomingDetected");
                if (setState(CallerState.IncomingDetected, CallerState.Idle))
                    for (ICallNetListener listener : iCallNetworkListeners) listener.callIncomingCanceled();
                break;
            case Call:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnClose Call");
                if (setState(CallerState.Call, CallerState.Idle))
                    for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callEndedExternally();
                exchangeStop();
                break;
            default:
                if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "srvOnClose " + getStateName());
        }
    }

    @Override
    public void cltOnOpen() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnOpen");
        switch (getState()) {
            case OutcomingStarted:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnOpen Call");
                if (setState(CallerState.OutcomingStarted, CallerState.OutcomingConnected))
                    for (ICallNetListener listener : iCallNetworkListeners) listener.callOutcomingConnected();
                break;
            default:
                if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "cltOnOpen " + getStateName());
                disconnect(iClientCtrl); // TODO: обрываем, если не звонили?
        }
    }

    @Override
    public void cltOnFailure() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnFailure");
        switch (getState()) {
            case OutcomingConnected:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnFailure OutcomingConnected");
                if (setState(CallerState.OutcomingConnected, CallerState.Idle))
                    for (ICallNetListener listener : iCallNetworkListeners) listener.callOutcomingRejected();
                break;
            case OutcomingStarted:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnFailure OutcomingStarted");
                if (setState(CallerState.OutcomingStarted, CallerState.Error))
                    for (ICallNetListener listener : iCallNetworkListeners) listener.callOutcomingFailed();
                break;
            case Call:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnFailure Call");
                if (setState(CallerState.Call, CallerState.Error))
                    for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callFailed();
                break;
            default:
                if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "cltOnFailure " + getStateName());
        }
    }

    @Override
    public void cltOnMessageText(String message) {
        switch (getState()) {
            case OutcomingConnected:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnMessageText OutcomingConnected");
                if (message.equals(Messages.RESPONSE_ACCEPT)) {
                    if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnMessageText ACCEPT");
                    if (setState(CallerState.OutcomingConnected, CallerState.Call))
                        for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callOutcomingAccepted();
                    setiConnCtrl(iClientCtrl);
                    exchangeStart();
                } else if (message.equals(Messages.RESPONSE_REJECT)) {
                    if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnMessageText REJECT");
                    if (setState(CallerState.OutcomingConnected, CallerState.Idle))
                        for (ICallNetListener listener : iCallNetworkListeners) listener.callOutcomingRejected();
                    disconnect(iClientCtrl);
                }
        }
    }

    @Override
    public void cltOnClose() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnClose");
        switch (getState()) {
            case OutcomingConnected:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnClose OutcomingConnected");
                if (setState(CallerState.OutcomingConnected, CallerState.Idle))
                    for (ICallNetListener listener : iCallNetworkListeners) listener.callOutcomingRejected();
                break;
            case Call:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnClose Call");
                if (setState(CallerState.Call, CallerState.Idle))
                    for (ICallNetExchangeListener listener : iCallNetExchangeListeners) listener.callEndedExternally();
                break;
            default:
                if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "cltOnClose " + getStateName());
        }
    }

    //--------------------- network

    private void responseAccept() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "responseAccept");
        new SendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.RESPONSE_ACCEPT);
    }

    private boolean isValidIp() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "isValidIp");
        String remAddr = iNetInfoListener.getRemAddr();
        return !(remAddr.matches(getIpAddr(Settings.ipv4))
                || remAddr.matches("127.0.0.1")
                || !InetAddress.checkForValidityIpAddress(remAddr));
    }

    private void connect() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "connect");
        new ClientConn(this, handler).execute(String.format("ws://%s:%s",
                iNetInfoListener.getRemAddr(),
                iNetInfoListener.getRemPort()));
    }

    private void disconnect(IConnCtrl iConnCtrl) {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "disconnect");
        if (iConnCtrl != null) {
            new Disc(this).execute(iConnCtrl);
        } else {
            if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "disconnect iConnCtrl is null");
        }
    }

    private void exchangeStart() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "exchangeStart");
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "exchangeStart iConnCtrl is instance of iServerCtrl: " + (iConnCtrl == iServerCtrl));
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "exchangeStart iConnCtrl is instance of iClientCtrl: " + (iConnCtrl == iClientCtrl));
        new RedirectToNet(this, iConnCtrl.getTransmitter(), storageBtToNet).execute();
        new RedirectFromNet(this, iConnCtrl.getReceiverReg(), storageNetToBt).execute();
    }

    private void exchangeStop() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "exchangeStop");
        new ThreadExchangeStop().start();
    }

    //--------------------- network low level

    private class ThreadExchangeStop
            extends Thread {
        @Override
        public void run() {
            if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "ThreadExchangeStop");
            streamOff();
            redirectOff();
        }
    }

    private class ThreadNetStop
            extends Thread {
        @Override
        public void run() {
            if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "ThreadNetStop");
            streamOff();
            redirectOff();
            disconnect(iClientCtrl);
            disconnect(iServerCtrl);
            serverOff();
        }
    }

    private void serverOff() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "serverOff");
        if (iServerCtrl != null) {
            new ServerOff(this).execute(iServerCtrl);
            iServerCtrl = null;
        }
    }

    private void redirectOff() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "redirectOff");
        if (iReceiverCtrl != null) {
            iReceiverCtrl.redirectOff();
            iReceiverCtrl = null;
        }
    }

    private void streamOff() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "streamOff");
        if (iTransmitterCtrl != null) {
            iTransmitterCtrl.streamOff();
            iTransmitterCtrl = null;
        }
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "ThreadNetStop iTransmitterCtrl.streamOff() done");
    }

    @Override
    public void serverStopped() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "serverStopped");
    }

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "serverStarted");
        if (iServerCtrl == null) {
            if (setState(CallerState.Null, CallerState.GeneralFailure))
                for (ICallNetListener listener : iCallNetworkListeners) listener.connectorFailure();
        } else {
            if (setState(CallerState.Null, CallerState.Idle))
                for (ICallNetListener listener : iCallNetworkListeners) listener.connectorReady();
            this.iServerCtrl = iServerCtrl;
        }
    }

    @Override
    public void registerReceiverCtrl(IReceiverCtrl iReceiverCtrl) {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "registerReceiverCtrl");
        if (iReceiverCtrl == null) {
            if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "registerReceiverCtrl iReceiverCtrl is null");
        } else {
            this.iReceiverCtrl = iReceiverCtrl;
        }
    }

    @Override
    public void registerTransmitterCtrl(ITransmitterCtrl iTransmitterCtrl) {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "registerTransmitterCtrl");
        if (iTransmitterCtrl == null) {
            if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "registerTransmitterCtrl iTransmitterCtrl is null");
        } else {
            this.iTransmitterCtrl = iTransmitterCtrl;
        }
    }

    @Override
    public void registerClientCtrl(IClientCtrl iClientCtrl) {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "registerClientCtrl");
        if (iClientCtrl == null) {
            if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "registerClientCtrl iClientCtrl is null");
        } else {
            this.iClientCtrl = iClientCtrl;
        }
    }

    //TODO: нафиг это здесь?
    @Override
    public void messageSended() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "messageSended");
    }

    @Override
    public void messageCantSend() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "messageCantSend");
    }

    @Override
    public void disconnected() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "disconnected");
    }

}
