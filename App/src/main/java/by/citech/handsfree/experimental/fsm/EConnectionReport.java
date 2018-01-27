package by.citech.handsfree.experimental.fsm;

import by.citech.handsfree.fsm.IFsmReport;

public enum EConnectionReport implements IFsmReport {
    UnconditionalTransition,
    BtLeNotSupported,
    BtNotSupported,
    BtPrepared,
    BtSearching,
    BtFound,
    BtConnecting,
    BtDisconnected,
//  BtNotificationStarted,
    BtConnectedCompatible,
    BtConnectedIncompatible,
//  PrepareBtStarted,
//  PrepareBtStopped,
    ConnectStarted,
    ConnectStopped,
    SearchStarted,
    SearchStopped,
//  NotificationStarted,
//  NotificationStopped,
    ChosenDevicePassedTheCheck,
    ChosenDeviceFailedTheCheck,
    GettingStatusStarted,
    GettingStatusStopped,
    GotStatus,
    GettingInitDataStarted,
    GettingInitDataStopped,
    GotInitData,
    TurningOff,
    TurningOn,
}
