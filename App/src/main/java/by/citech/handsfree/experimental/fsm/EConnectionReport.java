package by.citech.handsfree.experimental.fsm;

import by.citech.handsfree.fsm.IFsmReport;

public enum EConnectionReport implements IFsmReport {

    ReportUnconditional,

    ReportTurningOff,
    ReportTurningOn,

    ReportBtLeNotSupported,
    ReportBtNotSupported,
    ReportBtPrepared,

    ReportEnableStart,
    ReportBtEnabling,
    ReportBtEnabled,
    ReportEnableStop,

    ReportDisableStart,
    ReportBtDisabling,
    ReportBtDisabled,
    ReportDisableStop,

    ReportSearchStart,
    ReportBtDeviceSearching,
    ReportBtFound,
    ReportSearchStop,

    ReportConnectStart,
    ReportBtConnecting,
    ReportBtConnectedCompatible,
    ReportBtConnectedIncompatible,
    ReportConnectStop,

    ReportDisconnectStart,
    ReportBtDisconnecting,
    ReportBtDisconnected,
    ReportDisconnectStop,

    ReportNotificationEnableStart,
    ReportBtNotificationEnabling,
    ReportBtNotificationEnabled,
    ReportNotificationEnableStop,

    ReportNotificationDisableStart,
    ReportBtNotificationDisabling,
    ReportBtNotificationDisabled,
    ReportNotificationDisableStop,

    ReportChosenValid,
    ReportChosenInvalid,

}
