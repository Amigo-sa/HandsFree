package by.citech.handsfree.call.fsm;

import by.citech.handsfree.fsm.IFsmReport;

public enum ECallReport implements IFsmReport {

    RP_TurningOff,
    RP_TurningOn,
    RP_BtReady,
    RP_BtError,
    RP_NetReady,
    RP_NetError,
    RP_InConnected,
    RP_InFailed,
    RP_InCanceledRemote,
    RP_InRejectedLocal,
    RP_InAcceptedLocal,
    RP_OutStartedLocal,
    RP_OutInvalidCoordinates,
    RP_OutFailed,
    RP_OutCanceledLocal,
    RP_OutConnected,
    RP_OutRejectedRemote,
    RP_OutAcceptedRemote,
    RP_CallEndedLocal,
    RP_CallEndedRemote,
    RP_CallFailedExternally,
    RP_CallFailedInternally,

}
