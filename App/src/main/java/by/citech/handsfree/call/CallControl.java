package by.citech.handsfree.call;

import by.citech.handsfree.application.ThisApp;
import by.citech.handsfree.bluetoothlegatt.fsm.BtFsm;
import by.citech.handsfree.bluetoothlegatt.fsm.EBtReport;
import by.citech.handsfree.bluetoothlegatt.fsm.EBtState;
import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.network.fsm.ENetReport;
import by.citech.handsfree.network.fsm.ENetState;
import by.citech.handsfree.network.fsm.NetFsm;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;

public class CallControl implements
        NetFsm.INetFsmReporter,
        NetFsm.INetFsmListenerRegister,
        BtFsm.IBtFsmReporter,
        BtFsm.IBtFsmListenerRegister,
        CallFsm.ICallFsmReporter {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.CallControl;

    private Bt bt;
    private Net net;
    private Call call;

    //--------------------- singleton

    private static volatile CallControl instance = null;

    private CallControl() {
        bt = new Bt();
        net = new Net();
        call = new Call();
    }

    public static CallControl getInstance() {
        if (instance == null) {
            synchronized (CallControl.class) {
                if (instance == null) {instance = new CallControl();}}}
        return instance;
    }

    //--------------------- getters

    public Bt getBt() {return bt;}
    public Net getNet() {return net;}
    public Call getCall() {return call;}

    //--------------------- listeners

    private class Bt implements BtFsm.IBtFsmListener {
        @Override
        public void onFsmStateChange(EBtState from, EBtState to, EBtReport report) {
            switch (report) {
                case RP_BtFound:
                case RP_BtEnabled:
                case RP_BtDisabled:
                case RP_BtPrepared:
                case RP_BtChosenValid:
                case RP_BtChosenInvalid:
                case RP_BtDisconnected:
                case RP_BtNotSupported:
                case RP_BtLeNotSupported:
                case RP_BtConnectedCompatible:
                case RP_BtExchangeEnabled:
                case RP_BtExchangeDisabled:
                case RP_BtConnectedIncompatible:
                case RP_TurningOn:
                case RP_TurningOff:
            }
        }
    }

    private class Net implements NetFsm.INetFsmListener {
        @Override
        public void onFsmStateChange(ENetState from, ENetState to, ENetReport report) {
            switch (report) {
                case RP_NetConnectedIn:
                    toCall(ECallReport.RP_InConnected);
                    break;
                case RP_NetPrepared:
                    toCall(ECallReport.RP_NetReady);
                    break;
                case RP_NetConnectedOut:
                    toCall(ECallReport.RP_OutConnected);
                    break;
                case RP_NetPrepareFail:
                case RP_NetDisconnected:
                case RP_NetOutFail:
            }
        }
    }

    private class Call implements CallFsm.ICallFsmListener {
        @Override
        public void onFsmStateChange(ECallState from, ECallState to, ECallReport report) {
            switch (report) {
                case RP_OutCanceledLocal:
                case RP_InRejectedLocal:
                    toNet(ENetReport.RP_Disconnect);
                    break;
                case RP_OutStartedLocal:
                    toNet(ENetReport.RP_ConnectOut);
                    break;
                case RP_InAcceptedLocal:
                    toBt(EBtReport.RP_ExchangeEnable);
                    toNet(ENetReport.RP_ExchangeEnable);
                    break;
                case RP_CallEndedLocal:
                    toBt(EBtReport.RP_ExchangeDisable);
                    toNet(ENetReport.RP_ExchangeDisable);
                    break;
                case RP_TurningOn:
                    registerBtFsmListener(ThisApp.getConnectorBluetooth(), Tags.ConnectorBluetooth);
                    registerNetFsmListener(ThisApp.getConnectorNet(), Tags.ConnectorNet);
                    break;
                case RP_TurningOff:
                    unregisterBtFsmListener(ThisApp.getConnectorBluetooth(), Tags.ConnectorBluetooth);
                    unregisterNetFsmListener(ThisApp.getConnectorNet(), Tags.ConnectorNet);
                    break;

            }
        }
    }

    private boolean toBt(EBtReport report) {
        return reportToBtFsm(report, getBtFsmState(), TAG);
    }

    private boolean toNet(ENetReport report) {
        return reportToBtFsm(report, getNetFsmState(), TAG);
    }

    private boolean toCall(ECallReport report) {
        return reportToCallFsm(report, getCallFsmState(), TAG);
    }

}
