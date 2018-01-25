package by.citech.handsfree.call.fsm;

public enum ECallReport {

    UnconditionalTransition, // безусловный переход
    TurningOff, // выключение
    TurningOn, // включение

//  SysIntFail,  // BT failed
//  SysIntDisconnected,  // BT disconnected
//  SysIntConnected,  // BT connected
//  SysIntConnectedCompatible,  // compatible BT device connected
    SysIntConnectedIncompatible, // incompatible BT device connected (обработки нет, вывод в лог)
    SysIntReady,  // BT ready
    SysIntError,  // BT error
//  SysExtFail,  // net failed
    SysExtReady,  // net ready
    SysExtError,  // net error

    OutCallCanceledByLocalUser,  //
    OutConnectionCanceledByLocalUser,  //
    OutConnectionStartedByLocalUser,  //
    InCallRejectedByLocalUser,  //

    CallEndedByLocalUser,   // turn off BT + net data exchange
    InCallAcceptedByLocalUser,  // turn on BT + net data exchange

    OutConnectionConnected,
    OutCallRejectedByRemoteUser,
    OutConnectionFailed,
    OutCallInvalidCoordinates,
    InCallDetected,
    InCallCanceledByRemoteUser,
    InCallFailed,

    OutCallAcceptedByRemoteUser,  // turn on BT + net data exchange
    CallFailedExt,  // turn off BT + net data exchange
    CallFailedInt,  // turn off BT + net data exchange (обработки нет, вывод в лог)
    CallEndedByRemoteUser,  // turn off BT + net data exchange

    StartDebug, //
    StopDebug, //

}
