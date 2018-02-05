package by.citech.handsfree.call;

import by.citech.handsfree.bluetoothlegatt.fsm.BtFsm;
import by.citech.handsfree.bluetoothlegatt.fsm.EBtReport;
import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.network.fsm.ENetReport;
import by.citech.handsfree.network.fsm.NetFsm;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.application.ThisApp.getCallHandshake;
import static by.citech.handsfree.application.ThisApp.getConnectorBluetooth;
import static by.citech.handsfree.application.ThisApp.getConnectorNet;

public class CallControl implements
        NetFsm.INetFsmReporter,
        NetFsm.INetFsmListenerRegister,
        BtFsm.IBtFsmReporter,
        BtFsm.IBtFsmListenerRegister,
        CallFsm.ICallFsmListener,
        CallFsm.ICallFsmReporter {

    private static final String TAG = Tags.CallControl;

    private boolean isBtDisconnectManual;

    //--------------------- singleton

    private static volatile CallControl instance = null;

    private CallControl() {
    }

    public static CallControl getInstance() {
        if (instance == null) {
            synchronized (CallControl.class) {
                if (instance == null) {instance = new CallControl();}}}
        return instance;
    }

    //--------------------- listeners

    @Override
    public void onFsmStateChange(ECallState from, ECallState to, ECallReport report) {
        switch (report) {
            case RP_OutRejectedRemote:
            case RP_OutCanceledLocal:
            case RP_InRejectedLocal:
                toNet(ENetReport.RP_Disconnect);
                break;
            case RP_OutStartedLocal:
                toNet(ENetReport.RP_ConnectOut);
                break;
            case RP_OutAcceptedRemote:
            case RP_InAcceptedLocal:
                toBt(EBtReport.RP_ExchangeEnable);
                toNet(ENetReport.RP_ExchangeEnable);
                break;
            case RP_CallEndedLocal:
                toBt(EBtReport.RP_ExchangeDisable);
                toNet(ENetReport.RP_ExchangeDisable);
                break;
            case RP_TurningOn:
                registerBtFsmListener(getConnectorBluetooth(), Tags.ConnectorBluetooth);
                registerNetFsmListener(getConnectorNet(), Tags.ConnectorNet);
                registerBtFsmListener(btFsmListener, TAG);
                registerNetFsmListener(netFsmListener, TAG);
                getCallHandshake().registerRx(getConnectorNet().getConsumerToGiveString());
                getConnectorNet().setConsumerToTakeString(getCallHandshake());
                break;
            case RP_TurningOff:
                unregisterBtFsmListener(getConnectorBluetooth(), Tags.ConnectorBluetooth);
                unregisterNetFsmListener(getConnectorNet(), Tags.ConnectorNet);
                unregisterBtFsmListener(btFsmListener, TAG);
                unregisterNetFsmListener(netFsmListener, TAG);
                getCallHandshake().unregisterRx(getConnectorNet().getConsumerToGiveString());
                getConnectorNet().setConsumerToTakeString(null);
                break;
            default:
                break;
        }
    }

    private BtFsm.IBtFsmListener btFsmListener = (from, to, report) -> {
        switch (report) {
            case RP_BtNotSupported:
            case RP_BtLeNotSupported:
                toCall(ECallReport.RP_BtError);
                break;
            case RP_BtFound:
                toBt(EBtReport.RP_Connect);
                break;
            case RP_BtConnectedCompatible:
                toCall(ECallReport.RP_BtReady);
                break;
            case RP_BtChosenValid:
                toBt(EBtReport.RP_SearchStart);
                break;
            case RP_BtPrepared:
            case RP_BtDisabled:
                toBt(EBtReport.RP_Enable);
                break;
            case RP_DisconnectManual:
                isBtDisconnectManual = true;
                toBt(EBtReport.RP_Disconnect);
                break;
            case RP_BtDisconnected:
                if (isBtDisconnectManual) {
                    isBtDisconnectManual = false;
                } else {
                    toBt(EBtReport.RP_Connect);
                }
                toCall(ECallReport.RP_BtError);
                break;
            default:
                break;
        }
    };

    private NetFsm.INetFsmListener netFsmListener = (from, to, report) -> {
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
                toCall(ECallReport.RP_NetError);
                break;
            case RP_NetDisconnected:
                switch (getCallFsmState()) {
                    case ST_OutConnected:
                        toCall(ECallReport.RP_OutRejectedRemote);
                        break;
                    case ST_Call:
                        toCall(ECallReport.RP_CallEndedRemote);
                        break;
                    case ST_InConnected:
                        toCall(ECallReport.RP_InCanceledRemote);
                        break;
                }
            case RP_NetOutFail:
                toCall(ECallReport.RP_OutFailed);
                break;
            default:
                break;
        }
    };

    private boolean toBt(EBtReport report) {
        return reportToBtFsm(report, getBtFsmState(), TAG);
    }

    private boolean toNet(ENetReport report) {
        return reportToNetFsm(report, getNetFsmState(), TAG);
    }

    private boolean toCall(ECallReport report) {
        return reportToCallFsm(report, getCallFsmState(), TAG);
    }

}
