package by.citech.handsfree.logic;

public enum ECallReport {

    UnconditionalTransition, // безусловный переход

    SysIntFail,  // BT failed
    SysIntDisconnected,  // BT disconnected
    SysIntConnected,  // BT connected
    SysIntConnectedCompatible,  // compatible BT device connected
    SysIntConnectedIncompatible, // incompatible BT device connected
    SysIntReady,  // BT ready
    SysIntError,  // BT error
    SysExtFail,  // net failed
    SysExtReady,  // net ready

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
    CallFailedInt,  // turn off BT + net data exchange
    CallEndedByRemoteUser,  // turn off BT + net data exchange

    StartDebug, //
    StopDebug, //

}
