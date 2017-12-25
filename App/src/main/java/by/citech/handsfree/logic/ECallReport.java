package by.citech.handsfree.logic;

public enum ECallReport {

    //ICallToUiListener extends ICallToUiExchangeListener
    OutcomingCanceledByLocalUser, //callOutcomingCanceled
    OutcomingConnectionStartedByLocalUser, //callOutcomingStarted
    IncomingRejectedByLocalUser, //callIncomingRejected

    //ICallToUiExchangeListener
    EndedByLocalUser, //callEndedInternally TODO: выключение BT
    IncomingAcceptedByUser, //callIncomingAccepted TODO: включение BT

    //ICallNetListener extends ICallNetExchangeListener
    OutcomingConnected, //callOutcomingConnected
    OutcomingRejectedByRemoteUser, //callOutcomingRejected
    OutcomingConnectFailed, //callOutcomingFailed
    OutcomingInvalid, //callOutcomingInvalid
    IncomingDetected, //callIncomingDetected
    IncomingCanceled, //callIncomingCanceled
    IncomingFailed, //callIncomingFailed
    InternalConnectorFail, // TODO: bluetooth failed
    InternalConnectorReady, // TODO: bluetooth ready
    ExternalConnectorFail, //connectorFailure  TODO: network failed
    ExternalConnectorReady, //connectorReady TODO: network ready

    //ICallNetExchangeListener
    OutcomingAccepted,//callOutcomingAccepted TODO: включение BT
    FailExternal, //callFailed TODO: выключение BT
    FailInternal, //callFailed TODO: выключение BT
    EndedByRemoteUser, //callEndedExternally TODO: выключение BT

}
