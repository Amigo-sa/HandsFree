package by.citech.handsfree.network;

import android.os.Handler;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.exchange.IRx;
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
import by.citech.handsfree.network.fsm.ENetReport;
import by.citech.handsfree.network.fsm.ENetState;
import by.citech.handsfree.network.fsm.NetFsm;
import by.citech.handsfree.network.server.ServerOff;
import by.citech.handsfree.network.server.ServerOn;
import by.citech.handsfree.network.server.IServerCtrl;
import by.citech.handsfree.network.server.IServerCtrlReg;
import by.citech.handsfree.network.server.IServerOff;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.threading.IThreading;
import by.citech.handsfree.util.InetAddress;
import timber.log.Timber;

import static by.citech.handsfree.network.fsm.ENetReport.*;
import static by.citech.handsfree.util.Network.getIpAddr;

public class ConnectorNet implements
        IServerCtrlReg, IStreamerRegister, IClientCtrlReg, IDisc,
        IThreading, IMessageResult, IServerOff, INetListener,
        NetFsm.INetFsmListener,
        NetFsm.INetFsmReporter {

    private static final String TAG = Tags.ConnectorNet;
    private static final boolean debug = Settings.debug;
    private static final EDataSource dataSource = Settings.Common.dataSource;

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
    private boolean isClientConnected;
    private boolean isServerConnected;

    //--------------------- producer

    private IRx<String> consumerToSetString;
    private IRx<byte[]> consumerToSetBytes;

    //--------------------- consumer

    IRx<String> consumerToGiveString = new IRx<String>() {
        @Override
        public void onRx(String received) {sendMessage(received);}
        @Override
        public void onRxFinished() {}
    };

    IRx<byte[]> consumerToGiveBytes = new IRx<byte[]>() {
        @Override
        public void onRx(byte[] received) {} //TODO: реализовать логику
        @Override
        public void onRxFinished() {}
    };

    //--------------------- runnables

    private Runnable startServerDelayed = () -> {
        if (debug) Timber.i("startServerDelayed run");
        while (isBaseStopInProcess) try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        handler.post(this::startServer);
    };

    private Runnable stopStreaming = () -> {
        if (debug) Timber.i("stopStreaming run");
        stopStream();
    };

    private Runnable stopNetworking = () -> {
        if (debug) Timber.i("stopNetworking run");
        stopStream();
        disconnect(iConnCtrl);
        stopServer();
    };

    //--------------------- singleton

    private static volatile ConnectorNet instance = null;

    private ConnectorNet() {
        transmitterCtrls = new ConcurrentLinkedQueue<>();
    }

    public static ConnectorNet getInstance() {
        if (instance == null) {
            synchronized (ConnectorNet.class) {
                if (instance == null) {instance = new ConnectorNet();}}}
        return instance;
    }

    //--------------------- getters and setters


    public IRx<String> getConsumerToGiveString() {
        return consumerToGiveString;
    }

    public IRx<byte[]> getConsumerToGiveBytes() {
        return consumerToGiveBytes;
    }

    public ConnectorNet setConsumerToTakeString(IRx<String> consumerToSetString) {
        this.consumerToSetString = consumerToSetString;
        return this;
    }

    public ConnectorNet setConsumerToTakeBytes(IRx<byte[]> consumerToSetBytes) {
        this.consumerToSetBytes = consumerToSetBytes;
        return this;
    }

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

    //--------------------- INetFsmListener

    @Override
    public void onFsmStateChange(ENetState from, ENetState to, ENetReport why) {
        if (debug) Timber.i("onNetFsmStateChange");
        switch (why) {
            case RP_TurningOn:
                startServer();
                break;
            case RP_ConnectOut:
                if (!isValidCoordinates()) toNet(ENetReport.RP_NetAddrInvalid);
                else connect();
                break;
            case RP_TurningOff:
                exchangeStop();
                disconnect(iConnCtrl);
                stopServer();
                break;
            case RP_Disconnect:
                exchangeStop();
                disconnect(iConnCtrl);
                break;
            case RP_ExchangeEnable:
                exchangeStart();
                break;
            case RP_ExchangeDisable:
                exchangeStop();
                break;
            default:
                break;
        }
    }

    //--------------------- INetListener

    @Override
    public void srvOnOpen() {
        if (isClientConnected) {
            disconnect(iServerCtrl);
        } else {
            toNet(RP_NetConnectedIn);
            isServerConnected = true;
        }
    }

    @Override
    public void srvOnFailure() {
        if (isServerConnected) {
            isServerConnected = false;
            toNet(RP_NetDisconnected);
        }
    }

    @Override
    public void srvOnClose() {
        if (isServerConnected) {
            isServerConnected = false;
            toNet(RP_NetDisconnected);
        }
    }

    @Override
    public void cltOnOpen() {
        toNet(RP_NetConnectedOut);
        isClientConnected = true;
    }

    @Override
    public void cltOnFailure() {
        toNet(RP_NetDisconnected);
    }

    @Override
    public void cltOnMessageText(String message) {
        if (consumerToSetString != null) consumerToSetString.onRx(message);
    }

    @Override
    public void cltOnClose() {
        if (isClientConnected) {
            isClientConnected = false;
            toNet(RP_NetDisconnected);
        }
    }

    //--------------------- IServerCtrlReg

    @Override
    public void registerServerCtrl(IServerCtrl iServerCtrl) {
        if (iServerCtrl == null) {
            startServer();
            toNet(RP_NetPrepareFail);
        } else {
            this.iServerCtrl = iServerCtrl;
            toNet(RP_NetPrepared);
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
            if (debug) Timber.i("startServer base stop in process, waiting");
            addRunnable(startServerDelayed);
        } else {
            if (debug) Timber.i("startServer base stop is finished, starting server");
            new ServerOn(this, handler).execute(iNetInfoGetter.getLocPort());
        }
    }

    private void connect() {
        if (debug) Timber.i("connect");
        new ClientConn(this, handler).execute(
                String.format("ws://%s:%s", remAddr, remPort));
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

    //--------------------- misc

    private boolean toNet(ENetReport report) {
        return reportToNetFsm(report, getNetFsmState(), TAG);
    }

    private void sendMessage(String message) {
        if (debug) Timber.i("sendMessage %s", message);
        new SendMessage(this, iConnCtrl.getTransmitter()).execute(message);
    }


    private void setiConnCtrl(IConnCtrl iConnCtrl) {
        this.iConnCtrl = iConnCtrl;
    }

}
