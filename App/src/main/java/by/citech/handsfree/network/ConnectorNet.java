package by.citech.handsfree.network;

import android.os.Handler;
import android.util.Log;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.call.fsm.ICallFsmListener;
import by.citech.handsfree.call.fsm.ICallFsmListenerRegister;
import by.citech.handsfree.call.fsm.ICallFsmReporter;
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

import static by.citech.handsfree.call.fsm.ECallReport.CallEndedByRemoteUser;
import static by.citech.handsfree.call.fsm.ECallReport.CallFailedExt;
import static by.citech.handsfree.call.fsm.ECallReport.SysExtError;
import static by.citech.handsfree.call.fsm.ECallReport.SysExtReady;
import static by.citech.handsfree.call.fsm.ECallReport.InCallCanceledByRemoteUser;
import static by.citech.handsfree.call.fsm.ECallReport.InCallDetected;
import static by.citech.handsfree.call.fsm.ECallReport.InCallFailed;
import static by.citech.handsfree.call.fsm.ECallReport.OutCallAcceptedByRemoteUser;
import static by.citech.handsfree.call.fsm.ECallReport.OutCallInvalidCoordinates;
import static by.citech.handsfree.call.fsm.ECallReport.OutCallRejectedByRemoteUser;
import static by.citech.handsfree.call.fsm.ECallReport.OutConnectionConnected;
import static by.citech.handsfree.call.fsm.ECallReport.OutConnectionFailed;
import static by.citech.handsfree.util.Network.getIpAddr;

public class ConnectorNet
        implements IServerCtrlReg, IStreamerRegister, IClientCtrlReg, ICallFsmListener,
        IMessageResult, IServerOff, IDisc, INetListener, ICallFsmReporter, IThreading, ICallFsmListenerRegister {

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
            if (debug) Log.i(TAG, "startServerDelayed run");
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
            if (debug) Log.i(TAG, "stopStreaming run");
            stopStream();
        }
    };

    private Runnable stopNetworking = new Runnable() {
        @Override
        public void run() {
            if (debug) Log.i(TAG, "stopNetworking run");
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
        if (debug) Log.i(TAG, "build");
        registerCallerFsmListener(this, TAG);
        startServer();
    }

    public void destroy() {
        if (debug) Log.i(TAG, "destroy");
        unregisterCallerFsmListener(this, TAG);
        isBaseStopInProcess = true;
        iNetInfoGetter = null;
        handler = null;
        storageToNet = null;
        storageFromNet = null;
        addRunnable(stopNetworking);
    }

    private void finishBaseStop() {
        if (debug) Log.i(TAG, "finishBaseStop");
        iServerCtrl = null;
        iClientCtrl = null;
        iConnCtrl = null;
        transmitterCtrls.clear();
        isBaseStopInProcess = false;
    }

    private void processReport(Report report) {
        if (debug) Log.i(TAG, "processReport: " + report);
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
        if (debug) Log.i(TAG, "onCallerStateChange");
        switch (why) {
            case SysIntError:
            case CallFailedInt:
            case CallEndedByLocalUser:
                exchangeStop();
                disconnect(iConnCtrl);
                break;
            case OutCallCanceledByLocalUser:
                disconnect(iClientCtrl);
                break;
            case InCallAcceptedByLocalUser:
                setiConnCtrl(iServerCtrl);
                responseAccept();
                exchangeStart();
                break;
            case InCallRejectedByLocalUser:
                disconnect(iServerCtrl);
                break;
            case OutConnectionStartedByLocalUser:
                if (!isValidCoordinates()) reportToCallerFsm(to, OutCallInvalidCoordinates, TAG);
                else connect();
                break;
            default:
                break;
        }
    }

    //--------------------- INetListener

    @Override
    public void srvOnOpen() {
        ECallState callerState = getCallerFsmState();
        if (debug) Log.i(TAG, "srvOnOpen callerState is " + callerState);
        switch (callerState) {
            case ReadyToWork:
                if (reportToCallerFsm(callerState, InCallDetected, TAG)) return; else break;
            default:
                if (debug) Log.w(TAG, "srvOnOpen " + callerState);
                disconnect(iServerCtrl); // TODO: обрываем, если не ждём звонка?
                return;
        }
        if (debug) Log.w(TAG, "srvOnOpen recursive call");
        srvOnOpen();
    }

    @Override
    public void srvOnFailure() {
        ECallState callerState = getCallerFsmState();
        if (debug) Log.i(TAG, "srvOnFailure callerState is " + callerState);
        switch (callerState) {
            case InDetected:
                if (reportToCallerFsm(callerState, InCallFailed, TAG)) return; else break;
            case Call:
                if (reportToCallerFsm(callerState, CallFailedExt, TAG)) {
                    exchangeStop();
                    return;
                }
                else break;
            default:
                if (debug) Log.w(TAG, "srvOnFailure " + callerState); return;
        }
        if (debug) Log.w(TAG, "srvOnFailure recursive call");
        srvOnFailure();
    }

    @Override
    public void srvOnClose() {
        ECallState callerState = getCallerFsmState();
        if (debug) Log.i(TAG, "srvOnClose callerState is " + callerState);
        switch (callerState) {
            case InDetected:
                if (reportToCallerFsm(callerState, InCallCanceledByRemoteUser, TAG)) return; else break;
            case Call:
                if (reportToCallerFsm(callerState, CallEndedByRemoteUser, TAG)) {
                    exchangeStop();
                    return;
                }
                else break;
            default:
                if (debug) Log.e(TAG, "srvOnClose " + callerState); return;
        }
        if (debug) Log.w(TAG, "srvOnClose recursive call");
        srvOnClose();
    }

    @Override
    public void cltOnOpen() {
        ECallState callerState = getCallerFsmState();
        if (debug) Log.i(TAG, "cltOnOpen callerState is " + callerState);
        switch (callerState) {
            case OutStarted:
                if (reportToCallerFsm(callerState, OutConnectionConnected, TAG)) return; else break;
            default:
                if (debug) Log.w(TAG, "cltOnOpen " + callerState);
                disconnect(iClientCtrl); // TODO: обрываем, если не звонили?
                return;
        }
        if (debug) Log.w(TAG, "cltOnOpen recursive call");
        cltOnOpen();
    }

    @Override
    public void cltOnFailure() {
        ECallState callerState = getCallerFsmState();
        if (debug) Log.i(TAG, "cltOnFailure callerState is " + callerState);
        switch (callerState) {
            case OutConnected:
                if (reportToCallerFsm(callerState, OutCallRejectedByRemoteUser, TAG)) return; else break;
            case OutStarted:
                if (reportToCallerFsm(callerState, OutConnectionFailed, TAG)) return; else break;
            case Call:
                if (reportToCallerFsm(callerState, CallFailedExt, TAG)) {
                    exchangeStop();
                    return;
                }
                else break;
            default:
                if (debug) Log.w(TAG, "cltOnFailure " + callerState); return;
        }
        if (debug) Log.w(TAG, "cltOnFailure recursive call");
        cltOnFailure();
    }

    @Override
    public void cltOnMessageText(String message) {
        ECallState callerState = getCallerFsmState();
        if (debug) Log.i(TAG, "cltOnMessageText callerState is " + callerState);
        switch (callerState) {
            case OutConnected:
                if (message.equals(Messages.RESPONSE_ACCEPT)) {
                    if (reportToCallerFsm(callerState, OutCallAcceptedByRemoteUser, TAG)) {
                        setiConnCtrl(iClientCtrl);
                        exchangeStart();
                    } else {
                        break;
                    }
                } else if (message.equals(Messages.RESPONSE_REJECT)) {
                    if (reportToCallerFsm(callerState, OutCallRejectedByRemoteUser, TAG)) {
                        disconnect(iClientCtrl);
                    } else {
                        break;
                    }
                }
                return;
            default:
                if (debug) Log.w(TAG, "cltOnMessageText " + callerState); return;
        }
        if (debug) Log.w(TAG, "cltOnMessageText recursive call");
        cltOnMessageText(message);
    }

    @Override
    public void cltOnClose() {
        ECallState callerState = getCallerFsmState();
        if (debug) Log.i(TAG, "cltOnClose callerState is " + callerState);
        switch (callerState) {
            case OutConnected:
                if (reportToCallerFsm(callerState, OutCallRejectedByRemoteUser, TAG)) return; else break;
            case Call:
                if (reportToCallerFsm(callerState, CallEndedByRemoteUser, TAG)) return; else break;
            default:
                if (debug) Log.w(TAG, "cltOnClose " + callerState); return;
        }
        if (debug) Log.w(TAG, "cltOnClose recursive call");
        cltOnClose();
    }

    //--------------------- IServerCtrlReg

    @Override
    public void registerServerCtrl(IServerCtrl iServerCtrl) {
        ECallState callerState = getCallerFsmState();
        if (debug) Log.i(TAG, "registerServerCtrl callerState is " + callerState);
        switch (callerState) {
            case PhaseReadyInt:
            case PhaseZero:
                if (iServerCtrl == null) {
                    reportToCallerFsm(callerState, SysExtError, TAG);
                    startServer();
                } else {
                    reportToCallerFsm(callerState, SysExtReady, TAG);
                    this.iServerCtrl = iServerCtrl;
                }
                return;
            default:
                if (debug) Log.w(TAG, "registerServerCtrl " + callerState); return;
        }
    }

    //--------------------- IClientCtrlReg

    @Override
    public void registerClientCtrl(IClientCtrl iClientCtrl) {
        if (debug) Log.i(TAG, "registerClientCtrl");
        if (iClientCtrl == null) {
            if (debug) Log.e(TAG, "registerClientCtrl iClientCtrl is null");
        } else {
            this.iClientCtrl = iClientCtrl;
        }
    }

    //--------------------- IStreamerRegister

    @Override
    public void registerTransmitterCtrl(IStreamer iStreamer) {
        if (debug) Log.i(TAG, "registerTransmitterCtrl");
        if (iStreamer == null) {
            if (debug) Log.e(TAG, "registerTransmitterCtrl fromCtrl is null");
        } else {
            transmitterCtrls.add(iStreamer);
        }
    }

    //--------------------- IServerOff

    @Override
    public void onServerStop() {
        if (debug) Log.i(TAG, "onServerStop");
        processReport(Report.ServerStopped);
    }

    //--------------------- network

    private void startServer() {
        if (debug) Log.i(TAG, "startServer");
        if (isBaseStopInProcess) {
            if (debug) Log.w(TAG, "startServer base stop in applyPrefsToSettings, waiting");
            addRunnable(startServerDelayed);
        } else {
            if (debug) Log.w(TAG, "startServer base stop is finisfed, starting server");
            new ServerOn(this, handler).execute(iNetInfoGetter.getLocPort());
        }
    }

    private void responseAccept() {
        if (debug) Log.i(TAG, "responseAccept");
        new SendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.RESPONSE_ACCEPT);
    }

    private void responseReject() {
        if (debug) Log.i(TAG, "responseAccept");
        new SendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.RESPONSE_REJECT);
    }

    private void connect() {
        if (debug) Log.i(TAG, "connect");
        new ClientConn(this, handler).execute(String.format(
                "ws://%s:%s", remAddr, remPort));
    }

    private void disconnect(IConnCtrl iConnCtrl) {
        if (debug) Log.i(TAG, "disconnect");
        printConnectControl();
        if (iConnCtrl != null) {
            new Disconnect(this).execute(iConnCtrl);
        } else {
            if (debug) Log.e(TAG, "disconnect iConnCtrl is null");
        }
    }

    private void exchangeStart() {
        if (debug) Log.i(TAG, "exchangeStart");
        printConnectControl();
        new RedirectToNet(this, iConnCtrl.getTransmitter(), storageToNet).execute(dataSource);
        new RedirectFromNet(this, iConnCtrl, storageFromNet).execute(dataSource);
    }

    private void exchangeStop() {
        if (debug) Log.i(TAG, "stopStreaming");
        addRunnable(stopStreaming);
    }

    private boolean isValidCoordinates() {
        if (debug) Log.i(TAG, "isValidCoordinates");
        remAddr = iNetInfoGetter.getRemAddr();
        remPort = iNetInfoGetter.getRemPort();
        return !(remAddr.matches(getIpAddr(Settings.Network.isIpv4Used))
                || remAddr.matches("127.0.0.1")
                || !InetAddress.checkForValidityIpAddr(remAddr));
    }

    private void printConnectControl() {
        if (debug) Log.i(TAG, String.format(Locale.US,
                "printConnectControl iConnCtrl is instance of %s",
                (iConnCtrl == null)        ? "null"        :
                (iConnCtrl == iServerCtrl) ? "iServerCtrl" :
                (iConnCtrl == iClientCtrl) ? "iClientCtrl" : "unknown"));
    }

    //--------------------- network low level

    private void stopServer() {
        if (debug) Log.i(TAG, "stopServer");
        if (iServerCtrl != null) {
            new ServerOff(this).execute(iServerCtrl);
        }
    }

    private void stopStream() {
        if (debug) Log.i(TAG, "stopStream");
        for (IStreamer transmitterCtrl : transmitterCtrls) {
            if (transmitterCtrl != null) {
                transmitterCtrl.finishStream();
            }
            transmitterCtrls.remove(transmitterCtrl);
        }
        if (debug) Log.i(TAG, "stopStream done");
    }

}
