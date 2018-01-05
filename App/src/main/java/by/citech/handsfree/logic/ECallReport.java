package by.citech.handsfree.logic;

public enum ECallReport {

    UnconditionalTransition,  // безусловный переход

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

    CallEndedByLocalUser,   // turn off BT data exchange
    InCallAcceptedByLocalUser,  // turn on BT data exchange

    OutConnectionConnected,
    OutCallRejectedByRemoteUser,
    OutConnectionFailed,
    OutCallInvalidCoordinates,
    InCallDetected,
    InCallCanceledByRemoteUser,
    InCallFailed,

    OutCallAcceptedByRemoteUser,  // turn on BT data exchange
    CallFailedExternal,  // turn off BT data exchange
    CallFailedInternal,  // turn off BT data exchange
    CallEndedByRemoteUser,  // turn off BT data exchange

    StartDebug, //
    StopDebug, //

}
