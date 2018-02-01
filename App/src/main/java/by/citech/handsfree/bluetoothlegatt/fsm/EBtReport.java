package by.citech.handsfree.bluetoothlegatt.fsm;

import by.citech.handsfree.fsm.IFsmReport;
import static by.citech.handsfree.bluetoothlegatt.fsm.EBtState.*;

public enum EBtReport implements IFsmReport<EBtState> {

    RP_TurningOn               (ST_TurnedOn),
    RP_TurningOff              (ST_TurnedOff),
    RP_BtLeNotSupported        (ST_BtPrepareFail),
    RP_BtNotSupported          (ST_BtPrepareFail),
    RP_BtPrepared              (ST_BtPrepared),
    RP_Enable                  (ST_BtEnabling),
    RP_Disable                 (ST_BtDisabled),
    RP_BtEnabled               (ST_BtEnabled),
    RP_BtDisabled              (ST_BtDisabled),
    RP_BtChosenValid           (ST_DeviceChosen),
    RP_BtChosenInvalid         (ST_DeviceNotChosen),
    RP_SearchStart             (ST_Searching),
    RP_SearchStop              (ST_DeviceChosen),
    RP_BtFound                 (ST_Found),
    RP_Connect                 (ST_Connecting),
    RP_Disconnect              (ST_Disconnecting),
    RP_DisconnectManual        (ST_Disconnecting),
    RP_BtConnectedCompatible   (ST_Connected),
    RP_BtConnectedIncompatible (ST_Incompatible),
    RP_BtDisconnected          (ST_Disconnected),
    RP_ExchangeEnable          (ST_ExchangeEnabling),
    RP_BtExchangeEnabled       (ST_ExchangeEnabled),
    RP_ExchangeDisable         (ST_ExchangeDisabling),
    RP_BtExchangeDisabled      (ST_Connected);

    private EBtState destination;
    @Override public EBtState getDestination() {return destination;}
    EBtReport(EBtState state) {this.destination = state;}
    
}
