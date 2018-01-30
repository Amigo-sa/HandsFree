package by.citech.handsfree.bluetoothlegatt.fsm;

import by.citech.handsfree.fsm.IFsmReport;
import static by.citech.handsfree.bluetoothlegatt.fsm.EBtState.*;

public enum EBtReport implements IFsmReport<EBtState> {

    ReportTurningOn              (ST_TurnedOn),
    ReportTurningOff             (ST_TurnedOff),
    ReportBtLeNotSupported       (ST_BtPrepareFail),
    ReportBtNotSupported         (ST_BtPrepareFail),
    ReportBtPrepared             (ST_BtPrepared),
    ReportEnable                 (ST_BtEnabling),
    ReportDisable                (ST_BtDisabled),
    ReportBtEnabled              (ST_BtEnabled),
    ReportBtDisabled             (ST_BtDisabled),
    ReportChosenValid            (ST_DeviceChosen),
    ReportChosenInvalid          (ST_DeviceNotChosen),
    ReportSearchStart            (ST_Searching),
    ReportSearchStop             (ST_DeviceChosen),
    ReportBtFound                (ST_Found),
    ReportConnect                (ST_Connecting),
    ReportDisconnect             (ST_Disconnecting),
    ReportBtConnectedCompatible  (ST_Connected),
    ReportBtConnectedIncompatible(ST_Incompatible),
    ReportBtDisconnected         (ST_Disconnected),
    ReportExchangeEnable         (ST_ExchangeEnabling),
    ReportBtExchangeEnabled      (ST_ExchangeEnabled),
    ReportExchangeDisable        (ST_ExchangeDisabling),
    ReportBtExchangeDisabled     (ST_Connected);

    private EBtState destination;
    @Override public EBtState getDestination() {return destination;}
    EBtReport(EBtState state) {this.destination = state;}
    
}
