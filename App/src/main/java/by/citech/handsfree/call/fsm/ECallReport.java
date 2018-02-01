package by.citech.handsfree.call.fsm;

import by.citech.handsfree.fsm.IFsmReport;
import static by.citech.handsfree.call.fsm.ECallState.*;

public enum ECallReport implements IFsmReport<ECallState> {

    RP_TurningOff(ST_TurnedOff),
    RP_TurningOn(ST_TurnedOn),
    RP_BtReady(null),
    RP_BtError(null),
    RP_NetReady(null),
    RP_NetError(null),
    RP_InConnected(ST_InConnected),
    RP_InCanceledRemote(ST_Ready),
    RP_InRejectedLocal(ST_Ready),
    RP_InAcceptedLocal(ST_Call),
    RP_OutStartedLocal(ST_OutStarted),
    RP_OutInvalidCoordinates(ST_Ready),
    RP_OutFailed(ST_Ready),
    RP_OutCanceledLocal(ST_Ready),
    RP_OutConnected(ST_OutConnected),
    RP_OutRejectedRemote(ST_Ready),
    RP_OutAcceptedRemote(ST_Call),
    RP_CallEndedLocal(ST_Ready),
    RP_CallEndedRemote(ST_Ready),
    RP_CallFailedExternally(ST_Ready),
    RP_CallFailedInternally(ST_Ready);

    private ECallState destination;
    @Override public ECallState getDestination() {return destination;}
    ECallReport(ECallState state) {this.destination = state;}

}
