package by.citech.handsfree.bluetoothlegatt.fsm;

import by.citech.handsfree.fsm.IFsmReport;

public enum EBtReport implements IFsmReport {

    ReportTurningOn,
    ReportTurningOff,
    ReportBtLeNotSupported,
    ReportBtNotSupported,
    ReportBtPrepared,
    ReportEnable,
    ReportDisable,
    ReportBtEnabled,
    ReportBtDisabled,
    ReportChosenValid,
    ReportChosenInvalid,
    ReportSearchStart,
    ReportSearchStop,
    ReportBtFound,
    ReportConnect,
    ReportDisconnect,
    ReportBtConnectedCompatible,
    ReportBtConnectedIncompatible,
    ReportBtDisconnected,
    ReportExchangeEnable,
    ReportBtExchangeEnabled,
    ReportExchangeDisable,
    ReportBtExchangeDisabled,

}
