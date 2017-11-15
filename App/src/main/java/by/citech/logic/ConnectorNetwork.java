package by.citech.logic;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import by.citech.data.StorageData;
import by.citech.network.client.asynctask.TaskClientConn;
import by.citech.network.client.connection.IClientCtrl;
import by.citech.network.client.connection.IClientCtrlReg;
import by.citech.network.control.IConnCtrl;
import by.citech.network.control.IDisc;
import by.citech.network.control.IMessage;
import by.citech.network.control.TaskDisc;
import by.citech.network.control.TaskSendMessage;
import by.citech.network.control.redirect.IRedirectCtrl;
import by.citech.network.control.redirect.IRedirectCtrlReg;
import by.citech.network.control.redirect.Redirect;
import by.citech.network.control.stream.IStreamCtrl;
import by.citech.network.control.stream.IStreamCtrlReg;
import by.citech.network.control.stream.Stream;
import by.citech.network.server.asynctask.TaskServerOff;
import by.citech.network.server.asynctask.TaskServerOn;
import by.citech.network.server.connection.IServerCtrl;
import by.citech.network.server.connection.IServerCtrlReg;
import by.citech.network.server.connection.IServerOff;
import by.citech.param.Messages;
import by.citech.param.Settings;
import by.citech.param.Tags;

import static by.citech.util.NetworkInfo.getIpAddr;

public class ConnectorNetwork
        implements IServerCtrlReg, IRedirectCtrlReg, IStreamCtrlReg, IClientCtrlReg,
        IMessage, IServerOff, IDisc, INetworkListener, ICallUiListener {

    private IServerCtrl iServerCtrl;
    private IClientCtrl iClientCtrl;
    private IRedirectCtrl iRedirectCtrl;
    private IStreamCtrl iStreamCtrl;
    private IConnCtrl iConnCtrl;
    private Handler handler;
    private INetworkInfoListener iNetworkInfoListener;
    private ArrayList<ICallNetworkListener> iCallNetworkListeners;
    private ArrayList<ICallNetworkExchangeListener> iCallNetworkExchangeListeners;
    private StorageData storageBtToNet;
    private StorageData storageNetToBt;

    //--------------------- singleton

    private static volatile ConnectorNetwork instance = null;

    private ConnectorNetwork() {
        iCallNetworkListeners = new ArrayList<>();
        iCallNetworkExchangeListeners = new ArrayList<>();
    }

    public static ConnectorNetwork getInstance() {
        if (instance == null) {
            synchronized (ConnectorNetwork.class) {
                if (instance == null) {
                    instance = new ConnectorNetwork();
                }
            }
        }
        return instance;
    }

    //--------------------- getters and setters

    public ConnectorNetwork setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    public ConnectorNetwork setiNetworkInfoListener(INetworkInfoListener iNetworkInfoListener) {
        this.iNetworkInfoListener = iNetworkInfoListener;
        return this;
    }

    public ConnectorNetwork addiCallNetworkExchangeListener(ICallNetworkExchangeListener iCallNetworkExchangeListener) {
        iCallNetworkExchangeListeners.add(iCallNetworkExchangeListener);
        return this;
    }

    public ConnectorNetwork addiCallNetworkListener(ICallNetworkListener iCallNetworkListener) {
        iCallNetworkListeners.add(iCallNetworkListener);
        iCallNetworkExchangeListeners.add(iCallNetworkListener);
        return this;
    }

    public ConnectorNetwork setStorageBtToNet(StorageData storageBtToNet) {
        this.storageBtToNet = storageBtToNet;
        return this;
    }

    public ConnectorNetwork setStorageNetToBt(StorageData storageNetToBt) {
        this.storageNetToBt = storageNetToBt;
        return this;
    }

    private void setiConnCtrl(IConnCtrl iConnCtrl) {
        this.iConnCtrl = iConnCtrl;
    }

    private void resetiConnCtrl() {
        this.iConnCtrl = null;
    }

    //--------------------- main

    public void start() {
        new TaskServerOn(this, handler).execute(iNetworkInfoListener.getLocPort());
    }

    public void stop() {
        new ThreadNetStop().start();
    }

    //--------------------- common

    private boolean setState(State fromState, State toState) {
        return Caller.getInstance().setState(fromState, toState);
    }

    private String getStateName() {
        return Caller.getInstance().getState().getName();
    }

    private State getState() {
        return Caller.getInstance().getState();
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
        if (isLocalIp()) {
            if (Settings.debug) Log.w(Tags.NET_CONNECTOR, "callOutcomingStarted isLocalIp()");
            if (setState(State.OutcomingStarted, State.Idle))
                for (ICallNetworkListener listener : iCallNetworkListeners) listener.callOutcomingLocal();
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

    //--------------------- INetworkListener

    @Override
    public void srvOnOpen() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnOpen");
        switch (getState()) {
            case Idle:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnOpen Idle");
                if (setState(State.Idle, State.IncomingDetected))
                    for (ICallNetworkListener listener : iCallNetworkListeners) listener.callIncomingDetected();
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
                if (setState(State.IncomingDetected, State.Error))
                    for (ICallNetworkListener listener : iCallNetworkListeners) listener.callIncomingFailed();
                break;
            case Call:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnFailure Call");
                if (setState(State.Call, State.Error))
                    for (ICallNetworkExchangeListener listener : iCallNetworkExchangeListeners) listener.callFailed();
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
                if (setState(State.IncomingDetected, State.Idle))
                    for (ICallNetworkListener listener : iCallNetworkListeners) listener.callIncomingCanceled();
                break;
            case Call:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "srvOnClose Call");
                if (setState(State.Call, State.Idle))
                    for (ICallNetworkExchangeListener listener : iCallNetworkExchangeListeners) listener.callEndedExternally();
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
                if (setState(State.OutcomingStarted, State.OutcomingConnected))
                    for (ICallNetworkListener listener : iCallNetworkListeners) listener.callOutcomingConnected();
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
                if (setState(State.OutcomingConnected, State.Idle))
                    for (ICallNetworkListener listener : iCallNetworkListeners) listener.callOutcomingRejected();
                break;
            case OutcomingStarted:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnFailure OutcomingStarted");
                if (setState(State.OutcomingStarted, State.Error))
                    for (ICallNetworkListener listener : iCallNetworkListeners) listener.callOutcomingFailed();
                break;
            case Call:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnFailure Call");
                if (setState(State.Call, State.Error))
                    for (ICallNetworkExchangeListener listener : iCallNetworkExchangeListeners) listener.callFailed();
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
                    if (setState(State.OutcomingConnected, State.Call))
                        for (ICallNetworkExchangeListener listener : iCallNetworkExchangeListeners) listener.callOutcomingAccepted();
                    setiConnCtrl(iClientCtrl);
                    exchangeStart();
                } else if (message.equals(Messages.RESPONSE_REJECT)) {
                    if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnMessageText REJECT");
                    if (setState(State.OutcomingConnected, State.Idle))
                        for (ICallNetworkListener listener : iCallNetworkListeners) listener.callOutcomingRejected();
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
                if (setState(State.OutcomingConnected, State.Idle))
                    for (ICallNetworkListener listener : iCallNetworkListeners) listener.callOutcomingRejected();
                break;
            case Call:
                if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "cltOnClose Call");
                if (setState(State.Call, State.Idle))
                    for (ICallNetworkExchangeListener listener : iCallNetworkExchangeListeners) listener.callEndedExternally();
                break;
            default:
                if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "cltOnClose " + getStateName());
        }
    }

    //--------------------- network

    private void responseAccept() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "responseAccept");
        new TaskSendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.RESPONSE_ACCEPT);
    }

    private boolean isLocalIp() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "isLocalIp");
        String remAddr = iNetworkInfoListener.getRemAddr();
        return (remAddr.equals(getIpAddr(Settings.ipv4)) ||
                remAddr.equals("127.0.0.1") ||
                remAddr.equals("localhost"));
    }

    private void connect() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "connect");
        new TaskClientConn(this, handler).execute(String.format("ws://%s:%s",
                iNetworkInfoListener.getRemAddr(),
                iNetworkInfoListener.getRemPort()));
    }

    private void disconnect(IConnCtrl iConnCtrl) {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "disconnect");
        if (iConnCtrl != null) {
            new TaskDisc(this).execute(iConnCtrl);
        } else {
            if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "disconnect iConnCtrl is null");
        }
    }

    private void exchangeStart() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "exchangeStart");
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "exchangeStart iConnCtrl is instance of iServerCtrl: " + (iConnCtrl == iServerCtrl));
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "exchangeStart iConnCtrl is instance of iClientCtrl: " + (iConnCtrl == iClientCtrl));
        new Stream(this, iConnCtrl.getTransmitter(), storageBtToNet).execute();
        new Redirect(this, iConnCtrl.getReceiverRegister(), storageNetToBt).execute();
    }

    private void exchangeStop() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "exchangeStop");
        new ThreadExchangeStop().start();
    }

    //--------------------- network low level

    private class ThreadExchangeStop extends Thread {
        @Override
        public void run() {
            if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "ThreadExchangeStop");
            streamOff();
            redirectOff();
        }
    }

    private class ThreadNetStop extends Thread {
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
            new TaskServerOff(this).execute(iServerCtrl);
            iServerCtrl = null;
        }
    }

    private void redirectOff() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "redirectOff");
        if (iRedirectCtrl != null) {
            iRedirectCtrl.redirectOff();
            iRedirectCtrl = null;
        }
    }

    private void streamOff() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "streamOff");
        if (iStreamCtrl != null) {
            iStreamCtrl.streamOff();
            iStreamCtrl = null;
        }
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "ThreadNetStop iStreamCtrl.streamOff() done");
    }

    @Override
    public void serverStopped() {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "serverStopped");
    }

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "serverStarted");
        if (iServerCtrl == null) {
            if (setState(State.Null, State.GeneralFailure))
                for (ICallNetworkListener listener : iCallNetworkListeners) listener.connectorFailure();
        } else {
            if (setState(State.Null, State.Idle))
                for (ICallNetworkListener listener : iCallNetworkListeners) listener.connectorReady();
            this.iServerCtrl = iServerCtrl;
        }
    }

    @Override
    public void registerRedirectCtrl(IRedirectCtrl iRedirectCtrl) {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "registerRedirectCtrl");
        if (iRedirectCtrl == null) {
            if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "registerRedirectCtrl iRedirectCtrl is null");
        } else {
            this.iRedirectCtrl = iRedirectCtrl;
        }
    }

    @Override
    public void registerStreamCtrl(IStreamCtrl iStreamCtrl) {
        if (Settings.debug) Log.i(Tags.NET_CONNECTOR, "registerStreamCtrl");
        if (iStreamCtrl == null) {
            if (Settings.debug) Log.e(Tags.NET_CONNECTOR, "registerStreamCtrl iStreamCtrl is null");
        } else {
            this.iStreamCtrl = iStreamCtrl;
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
