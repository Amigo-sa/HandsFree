package by.citech.handsfree.logic;

public enum ECallReport {

    UnconditionalTransition,

    //ICallToUiListener extends ICallToUiExchangeListener
    OutCallCanceledByLocalUser, //callOutcomingCanceled
    OutConnectionCanceledByLocalUser, //callOutcomingCanceled
    OutConnectionStartedByLocalUser, //callOutcomingStarted
    InCallRejectedByLocalUser, //callIncomingRejected

    //ICallToUiExchangeListener
    CallEndedByLocalUser, //callEndedInternally TODO: выключение BT
    InCallAcceptedByLocalUser, //callIncomingAccepted TODO: включение BT

    //ICallNetListener extends ICallNetExchangeListener
    OutConnectionConnected, //callOutcomingConnected
    OutCallRejectedByRemoteUser, //callOutcomingRejected
    OutConnectionFailed, //callOutcomingFailed
    OutCallInvalidCoordinates, //callOutcomingInvalid
    InCallDetected, //callIncomingDetected
    InCallCanceledByRemoteUser, //callIncomingCanceled
    InCallFailed, //callIncomingFailed
    InternalConnectorFail, //TODO: bluetooth failed
    InternalConnectorReady, //TODO: bluetooth ready
    ExternalConnectorFail, //connectorFailure  TODO: network failed
    ExternalConnectorReady, //connectorReady TODO: network ready

    //ICallNetExchangeListener
    OutCallAcceptedByRemoteUser, //callOutcomingAccepted TODO: включение BT
    CallFailedExternal, //callFailed TODO: выключение BT
    CallFailedInternal, //callFailed TODO: выключение BT
    CallEndedByRemoteUser, //callEndedExternally TODO: выключение BT

    //IDebugCtrl
    StartDebug, //startDebug
    StopDebug, //stopDebug

}
