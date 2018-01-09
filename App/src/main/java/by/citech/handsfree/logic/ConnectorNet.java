package by.citech.handsfree.logic;

import android.os.Handler;
import android.util.Log;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.management.IBase;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.RedirectFromNet;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.network.INetListener;
import by.citech.handsfree.network.client.ClientConn;
import by.citech.handsfree.network.client.IClientCtrl;
import by.citech.handsfree.network.client.IClientCtrlReg;
import by.citech.handsfree.network.control.IConnCtrl;
import by.citech.handsfree.network.control.IDisc;
import by.citech.handsfree.exchange.IMessageResult;
import by.citech.handsfree.network.control.Disconnect;
import by.citech.handsfree.exchange.SendMessage;
import by.citech.handsfree.exchange.ITransmitterCtrlReg;
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
import by.citech.handsfree.threading.IThreadManager;
import by.citech.handsfree.util.InetAddress;

import static by.citech.handsfree.logic.ECallReport.CallEndedByRemoteUser;
import static by.citech.handsfree.logic.ECallReport.CallFailedExt;
import static by.citech.handsfree.logic.ECallReport.SysExtError;
import static by.citech.handsfree.logic.ECallReport.SysExtReady;
import static by.citech.handsfree.logic.ECallReport.InCallCanceledByRemoteUser;
import static by.citech.handsfree.logic.ECallReport.InCallDetected;
import static by.citech.handsfree.logic.ECallReport.InCallFailed;
import static by.citech.handsfree.logic.ECallReport.OutCallAcceptedByRemoteUser;
import static by.citech.handsfree.logic.ECallReport.OutCallInvalidCoordinates;
import static by.citech.handsfree.logic.ECallReport.OutCallRejectedByRemoteUser;
import static by.citech.handsfree.logic.ECallReport.OutConnectionConnected;
import static by.citech.handsfree.logic.ECallReport.OutConnectionFailed;
import static by.citech.handsfree.util.Network.getIpAddr;

public class ConnectorNet
        implements IServerCtrlReg, ITransmitterCtrlReg, IClientCtrlReg, ICallerFsmListener,
        IMessageResult, IServerOff, IDisc, INetListener, IBase, ICallerFsm, IThreadManager, ICallerFsmRegisterListener {

    private static final String STAG = Tags.ConnectorNet;
    private static final boolean debug = Settings.debug;
    private static final EDataSource dataSource = Settings.dataSource;

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
    private Collection<ITransmitterCtrl> transmitterCtrls;
    private IConnCtrl iConnCtrl;
    private Handler handler;
    private INetInfoGetter iNetInfoGetter;
    private StorageData<byte[]> storageToNet;
    private StorageData<byte[][]> storageFromNet;
    private boolean isBaseStop;

    //--------------------- runnables

    private Runnable exchangeStop = new Runnable() {
        @Override
        public void run() {
            if (debug) Log.i(TAG, "exchangeStop run");
            streamOff();
        }
    };

    private Runnable netStop = new Runnable() {
        @Override
        public void run() {
            if (debug) Log.i(TAG, "netStop run");
            streamOff();
            disconnect(iConnCtrl);
            serverOff();
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

    ConnectorNet setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    ConnectorNet setiNetInfoGetter(INetInfoGetter iNetInfoGetter) {
        this.iNetInfoGetter = iNetInfoGetter;
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
        registerCallerFsmListener(this, TAG);
        new ServerOn(this, handler).execute(iNetInfoGetter.getLocPort());
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        unregisterCallerFsmListener(this, TAG);
        isBaseStop = true;
        addRunnable(netStop);
        IBase.super.baseStop();
        return true;
    }

    private void finishBaseStop() {
        if (debug) Log.i(TAG, "finishBaseStop");
        iNetInfoGetter = null;
        handler = null;
        storageToNet = null;
        storageFromNet = null;
        iServerCtrl = null;
        iClientCtrl = null;
        transmitterCtrls.clear();
        iConnCtrl = null;
        isBaseStop = false;
    }

    private void processReport(Report report) {
        if (debug) Log.i(TAG, "processReport");
        if (report == null) return;
        switch (report) {
            case ServerStopped:
                if (isBaseStop) finishBaseStop();
                break;
            default:
                break;
        }
    }

    private enum Report {
        ServerStopped
    }

    //--------------------- ICallerFsmListener

    @Override
    public void onCallerStateChange(ECallerState from, ECallerState to, ECallReport why) {
        if (debug) Log.i(TAG, "onCallerStateChange");
        switch (why) {
//          case SysIntFail:
//          case SysIntDisconnected:
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
        ECallerState callerState = getCallerFsmState();
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
        ECallerState callerState = getCallerFsmState();
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
        ECallerState callerState = getCallerFsmState();
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
        ECallerState callerState = getCallerFsmState();
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
        ECallerState callerState = getCallerFsmState();
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
        ECallerState callerState = getCallerFsmState();
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
        ECallerState callerState = getCallerFsmState();
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

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        ECallerState callerState = getCallerFsmState();
        if (debug) Log.i(TAG, "serverStarted callerState is " + callerState);
        switch (callerState) {
            case PhaseReadyInt:
            case PhaseZero:
                if (iServerCtrl == null) {
                    if (reportToCallerFsm(callerState, SysExtError, TAG)) return;
                } else if (reportToCallerFsm(callerState, SysExtReady, TAG)) {
                    this.iServerCtrl = iServerCtrl;
                    return;
                }
                break;
            default:
                if (debug) Log.w(TAG, "serverStarted " + callerState); return;
        }
        if (debug) Log.w(TAG, "serverStarted recursive call");
        serverStarted(iServerCtrl);
    }

    //--------------------- network

    private void responseAccept() {
        if (debug) Log.i(TAG, "responseAccept");
        new SendMessage(this, iServerCtrl.getTransmitter()).execute(Messages.RESPONSE_ACCEPT);
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
        if (debug) Log.i(TAG, "exchangeStop");
        addRunnable(exchangeStop);
    }

    private boolean isValidCoordinates() {
        if (debug) Log.i(TAG, "isValidCoordinates");
        remAddr = iNetInfoGetter.getRemAddr();
        remPort = iNetInfoGetter.getRemPort();
        return !(remAddr.matches(getIpAddr(Settings.isIpv4Used))
                || remAddr.matches("127.0.0.1")
                || !InetAddress.checkForValidityIpAddr(remAddr));
    }

    private void printConnectControl() {
        if (debug) Log.i(TAG, String.format(Locale.US,
                "printConnectControl iConnCtrl is instance of %s",
                (iConnCtrl == iServerCtrl) ? "iServerCtrl" :
                (iConnCtrl == iClientCtrl) ? "iClientCtrl" : "unknown"));
    }

    //--------------------- network low level

    private void serverOff() {
        if (debug) Log.i(TAG, "serverOff");
        if (iServerCtrl != null) {
            new ServerOff(this).execute(iServerCtrl);
            iServerCtrl = null;
        }
    }

    private void streamOff() {
        if (debug) Log.i(TAG, "streamOff");
        for (ITransmitterCtrl transmitterCtrl : transmitterCtrls) {
            if (transmitterCtrl != null) {
                transmitterCtrl.finishStream();
            }
            transmitterCtrls.remove(transmitterCtrl);
        }
        if (debug) Log.i(TAG, "streamOff done");
    }

    @Override
    public void serverStopped() {
        if (debug) Log.i(TAG, "serverStopped");
        processReport(Report.ServerStopped);
    }

    @Override
    public void registerTransmitterCtrl(ITransmitterCtrl iTransmitterCtrl) {
        if (debug) Log.i(TAG, "registerTransmitterCtrl");
        if (iTransmitterCtrl == null) {
            if (debug) Log.e(TAG, "registerTransmitterCtrl fromCtrl is null");
        } else {
            transmitterCtrls.add(iTransmitterCtrl);
        }
    }

    @Override
    public void registerClientCtrl(IClientCtrl iClientCtrl) {
        if (debug) Log.i(TAG, "registerClientCtrl");
        if (iClientCtrl == null) {
            if (debug) Log.e(TAG, "registerClientCtrl iClientCtrl is null");
        } else {
            this.iClientCtrl = iClientCtrl;
        }
    }

}
