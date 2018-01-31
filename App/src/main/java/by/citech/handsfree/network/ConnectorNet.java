package by.citech.handsfree.network;

import android.os.Handler;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.RedirectFromNet;
import by.citech.handsfree.network.client.ClientConn;
import by.citech.handsfree.network.client.IClientCtrl;
import by.citech.handsfree.network.client.IClientCtrlReg;
import by.citech.handsfree.network.control.IConnCtrl;
import by.citech.handsfree.network.control.IDisc;
import by.citech.handsfree.exchange.IMessageResult;
import by.citech.handsfree.network.control.Disconnect;
import by.citech.handsfree.exchange.SendMessage;
import by.citech.handsfree.exchange.IStreamerRegister;
import by.citech.handsfree.exchange.RedirectToNet;
import by.citech.handsfree.network.fsm.NetFsm;
import by.citech.handsfree.network.server.ServerOff;
import by.citech.handsfree.network.server.ServerOn;
import by.citech.handsfree.network.server.IServerCtrl;
import by.citech.handsfree.network.server.IServerCtrlReg;
import by.citech.handsfree.network.server.IServerOff;
import by.citech.handsfree.parameters.Messages;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.threading.IThreading;
import by.citech.handsfree.util.InetAddress;
import timber.log.Timber;

import static by.citech.handsfree.call.fsm.ECallReport.*;
import static by.citech.handsfree.util.Network.getIpAddr;

public class ConnectorNet
        implements IServerCtrlReg, IStreamerRegister, IClientCtrlReg, IThreading, IMessageResult,
        IServerOff, IDisc, INetListener, NetFsm.INetFsmListener, NetFsm.INetFsmReporter {

    private static final String STAG = Tags.ConnectorNet;
    private static final boolean debug = Settings.debug;
    private static final EDataSource dataSource = Settings.Common.dataSource;

    private static int objCount;
    private final String TAG;
    static {objCount = 0;}


    {
        objCount++;
        TAG = STAG + " " + objCount;
        transmitterCtrls = new ConcurrentLinkedQueue<>();
    }

    private String remAddr;
    private String remPort;
    private IServerCtrl iServerCtrl;
    private IClientCtrl iClientCtrl;
    private Collection<IStreamer> transmitterCtrls;
    private IConnCtrl iConnCtrl;
    private Handler handler;
    private INetInfoGetter iNetInfoGetter;
    private StorageData<byte[]> storageToNet;
    private StorageData<byte[][]> storageFromNet;
    private boolean isBaseStopInProcess;

    //--------------------- runnables

    private Runnable startServerDelayed = new Runnable() {
        @Override
        public void run() {
            if (debug) Timber.i("startServerDelayed run");
            while (isBaseStopInProcess) try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(() -> startServer());
        }
    };

    private Runnable stopStreaming = new Runnable() {
        @Override
        public void run() {
            if (debug) Timber.i("stopStreaming run");
            stopStream();
        }
    };

    private Runnable stopNetworking = new Runnable() {
        @Override
        public void run() {
            if (debug) Timber.i("stopNetworking run");
            stopStream();
            disconnect(iConnCtrl);
            stopServer();
        }
    };

    //--------------------- singleton

    private static volatile ConnectorNet instance = null;

    private ConnectorNet() {
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

    public ConnectorNet setStorageToNet(StorageData<byte[]> storageBtToNet) {
        this.storageToNet = storageBtToNet;
        return this;
    }

    public ConnectorNet setStorageFromNet(StorageData<byte[][]> storageNetToBt) {
        this.storageFromNet = storageNetToBt;
        return this;
    }

    private void setiConnCtrl(IConnCtrl iConnCtrl) {
        this.iConnCtrl = iConnCtrl;
    }

    //--------------------- main

    public void build() {
        if (debug) Timber.i("build");
        startServer();
    }

    public void destroy() {
        if (debug) Timber.i("destroy");
        isBaseStopInProcess = true;
        iNetInfoGetter = null;
        handler = null;
        storageToNet = null;
        storageFromNet = null;
        addRunnable(stopNetworking);
    }

    private void finishBaseStop() {
        if (debug) Timber.i("finishBaseStop");
        iServerCtrl = null;
        iClientCtrl = null;
        iConnCtrl = null;
        transmitterCtrls.clear();
        isBaseStopInProcess = false;
    }

    private void processReport(Report report) {
        if (debug) Timber.i("processReport: %s", report);
        if (report == null) return;
        switch (report) {
            case ServerStopped:
                if (isBaseStopInProcess) finishBaseStop();
                break;
            default:
                break;
        }
    }

    private enum Report {
        ServerStopped
    }

    //--------------------- ICallFsmListener

    @Override
    public void onCallerStateChange(ECallState from, ECallState to, ECallReport why) {
        if (debug) Timber.i("onCallerStateChange");
        switch (why) {
            case RP_BtError:
            case RP_CallFailedInternally:
            case RP_CallEndedLocal:
                exchangeStop();
                disconnect(iConnCtrl);
                break;
            case RP_OutCanceledLocal:
                disconnect(iClientCtrl);
                break;
            case RP_InAcceptedLocal:
                setiConnCtrl(iServerCtrl);
                responseAccept();
                exchangeStart();
                break;
            case RP_InRejectedLocal:
                disconnect(iServerCtrl);
                break;
            case RP_OutStartedLocal:
                if (!isValidCoordinates()) reportToCallFsm(to, RP_OutInvalidCoordinates, TAG);
                else connect();
                break;
            default:
                break;
        }
    }

    //--------------------- INetListener

    @Override
    public void srvOnOpen() {
        ECallState callerState = getCallFsmState();
        if (debug) Timber.i("srvOnOpen callerState is %s", callerState);
        switch (callerState) {
            case ST_Ready:
                if (reportToCallFsm(callerState, RP_InConnected, TAG)) return; else break;
            default:
                if (debug) Timber.i("srvOnOpen %s", callerState);
                disconnect(iServerCtrl); // TODO: обрываем, если не ждём звонка?
                return;
        }
        if (debug) Timber.i("srvOnOpen recursive call");
        srvOnOpen();
    }

    @Override
    public void srvOnFailure() {
        ECallState callerState = getCallFsmState();
        if (debug) Timber.i("srvOnFailure callerState is %s", callerState);
        switch (callerState) {
            case ST_InConnected:
                if (reportToCallFsm(callerState, RP_InFailed, TAG)) return; else break;
            case ST_Call:
                if (reportToCallFsm(callerState, RP_CallFailedExternally, TAG)) {
                    exchangeStop();
                    return;
                }
                else break;
            default:
                if (debug) Timber.i("srvOnFailure %s", callerState); return;
        }
        if (debug) Timber.i("srvOnFailure recursive call");
        srvOnFailure();
    }

    @Override
    public void srvOnClose() {
        ECallState callerState = getCallFsmState();
        if (debug) Timber.i("srvOnClose callerState is %s", callerState);
        switch (callerState) {
            case ST_InConnected:
                if (reportToCallFsm(callerState, RP_InCanceledRemote, TAG)) return; else break;
            case ST_Call:
                if (reportToCallFsm(callerState, RP_CallEndedRemote, TAG)) {
                    exchangeStop();
                    return;
                }
                else break;
            default:
                if (debug) Timber.i("srvOnClose %s", callerState); return;
        }
        if (debug) Timber.i("srvOnClose recursive call");
        srvOnClose();
    }

    @Override
    public void cltOnOpen() {
        ECallState callerState = getCallFsmState();
        if (debug) Timber.i("cltOnOpen callerState is %s", callerState);
        switch (callerState) {
            case ST_OutStarted:
                if (reportToCallFsm(callerState, RP_OutConnected, TAG)) return; else break;
            default:
                if (debug) Timber.i("cltOnOpen %s", callerState);
                disconnect(iClientCtrl); // TODO: обрываем, если не звонили?
                return;
        }
        if (debug) Timber.i("cltOnOpen recursive call");
        cltOnOpen();
    }

    @Override
    public void cltOnFailure() {
        ECallState callerState = getCallFsmState();
        if (debug) Timber.i("cltOnFailure callerState is %s", callerState);
        switch (callerState) {
            case ST_OutConnected:
                if (reportToCallFsm(callerState, RP_OutRejectedRemote, TAG)) return; else break;
            case ST_OutStarted:
                if (reportToCallFsm(callerState, RP_OutFailed, TAG)) return; else break;
            case ST_Call:
                if (reportToCallFsm(callerState, RP_CallFailedExternally, TAG)) {
                    exchangeStop();
                    return;
                }
                else break;
            default:
                if (debug) Timber.i("cltOnFailure %s", callerState); return;
        }
        if (debug) Timber.i("cltOnFailure recursive call");
        cltOnFailure();
    }

    @Override
    public void cltOnMessageText(String message) {
        ECallState callerState = getCallFsmState();
        if (debug) Timber.i("cltOnMessageText callerState is %s", callerState);
        switch (callerState) {
            case ST_OutConnected:
                if (message.equals(Messages.RESPONSE_ACCEPT)) {
                    if (reportToCallFsm(callerState, RP_OutAcceptedRemote, TAG)) {
                        setiConnCtrl(iClientCtrl);
                        exchangeStart();
                    } else {
                        break;
                    }
                } else if (message.equals(Messages.RESPONSE_REJECT)) {
                    if (reportToCallFsm(callerState, RP_OutRejectedRemote, TAG)) {
                        disconnect(iClientCtrl);
                    } else {
                        break;
                    }
                }
                return;
            default:
                if (debug) Timber.i("cltOnMessageText %s", callerState); return;
        }
        if (debug) Timber.i("cltOnMessageText recursive call");
        cltOnMessageText(message);
    }

    @Override
    public void cltOnClose() {
        ECallState callerState = getCallFsmState();
        if (debug) Timber.i("cltOnClose callerState is %s", callerState);
        switch (callerState) {
            case ST_OutConnected:
                if (reportToCallFsm(callerState, RP_OutRejectedRemote, TAG)) return; else break;
            case ST_Call:
                if (reportToCallFsm(callerState, RP_CallEndedRemote, TAG)) return; else break;
            default:
                if (debug) Timber.i("cltOnClose %s", callerState); return;
        }
        if (debug) Timber.i("cltOnClose recursive call");
        cltOnClose();
    }

    //--------------------- IServerCtrlReg

    @Override
    public void registerServerCtrl(IServerCtrl iServerCtrl) {
        ECallState callerState = getCallFsmState();
        if (debug) Timber.i("registerServerCtrl callerState is %s", callerState);
        switch (callerState) {
            case ST_BtReady:
            case ST_TurnedOn:
                if (iServerCtrl == null) {
                    reportToCallFsm(callerState, RP_NetError, TAG);
                    startServer();
                } else {
                    reportToCallFsm(callerState, RP_NetReady, TAG);
                    this.iServerCtrl = iServerCtrl;
                }
                return;
            default:
                if (debug) Timber.i("registerServerCtrl %s", callerState); return;
        }
    }

    //--------------------- IClientCtrlReg

    @Override
    public void registerClientCtrl(IClientCtrl iClientCtrl) {
        if (debug) Timber.i("registerClientCtrl");
        if (iClientCtrl == null) {
            if (debug) Timber.e("registerClientCtrl iClientCtrl is null");
        } else {
            this.iClientCtrl = iClientCtrl;
        }
    }

    //--------------------- IStreamerRegister

    @Override
    public void registerTransmitterCtrl(IStreamer iStreamer) {
        if (debug) Timber.i("registerTransmitterCtrl");
        if (iStreamer == null) {
            if (debug) Timber.e("registerTransmitterCtrl fromCtrl is null");
        } else {
            transmitterCtrls.add(iStreamer);
        }
    }

    //--------------------- IServerOff

    @Override
    public void onServerStop() {
        if (debug) Timber.i("onServerStop");
        processReport(Report.ServerStopped);
    }

    //--------------------- network

    private void startServer() {
        if (debug) Timber.i("startServer");
        if (isBaseStopInProcess) {
            if (debug) Timber.i("startServer base stop in applyPrefsToSettings, waiting");
            addRunnable(startServerDelayed);
        } else {
            if (debug) Timber.i("startServer base stop is finisfed, starting server");
            new ServerOn(this, handler).execute(iNetInfoGetter.getLocPort());
        }
    }

    private void responseAccept() {
        if (debug) Timber.i("responseAccept");
        new SendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.RESPONSE_ACCEPT);
    }

    private void responseReject() {
        if (debug) Timber.i("responseAccept");
        new SendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.RESPONSE_REJECT);
    }

    private void connect() {
        if (debug) Timber.i("connect");
        new ClientConn(this, handler).execute(String.format(
                "ws://%s:%s", remAddr, remPort));
    }

    private void disconnect(IConnCtrl iConnCtrl) {
        if (debug) Timber.i("disconnect");
        printConnectControl();
        if (iConnCtrl != null) {
            new Disconnect(this).execute(iConnCtrl);
        } else {
            if (debug) Timber.e("disconnect iConnCtrl is null");
        }
    }

    private void exchangeStart() {
        if (debug) Timber.i("exchangeStart");
        printConnectControl();
        new RedirectToNet(this, iConnCtrl.getTransmitter(), storageToNet).execute(dataSource);
        new RedirectFromNet(this, iConnCtrl, storageFromNet).execute(dataSource);
    }

    private void exchangeStop() {
        if (debug) Timber.i("stopStreaming");
        addRunnable(stopStreaming);
    }

    private boolean isValidCoordinates() {
        if (debug) Timber.i("isValidCoordinates");
        remAddr = iNetInfoGetter.getRemAddr();
        remPort = iNetInfoGetter.getRemPort();
        return !(remAddr.matches(getIpAddr(Settings.Network.isIpv4Used))
                || remAddr.matches("127.0.0.1")
                || !InetAddress.checkForValidityIpAddr(remAddr));
    }

    private void printConnectControl() {
        if (debug) Timber.i("printConnectControl iConnCtrl is instance of %s",
                (iConnCtrl == null)        ? "null"        :
                (iConnCtrl == iServerCtrl) ? "iServerCtrl" :
                (iConnCtrl == iClientCtrl) ? "iClientCtrl" : "unknown");
    }

    //--------------------- network low level

    private void stopServer() {
        if (debug) Timber.i("stopServer");
        if (iServerCtrl != null) {
            new ServerOff(this).execute(iServerCtrl);
        }
    }

    private void stopStream() {
        if (debug) Timber.i("stopStream");
        for (IStreamer transmitterCtrl : transmitterCtrls) {
            if (transmitterCtrl != null) {
                transmitterCtrl.finishStream();
            }
            transmitterCtrls.remove(transmitterCtrl);
        }
        if (debug) Timber.i("stopStream done");
    }

}
