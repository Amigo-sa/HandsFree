package by.citech.handsfree.debug.fsm;

import by.citech.handsfree.fsm.IFsmReport;
import static by.citech.handsfree.debug.fsm.EDebugState.*;

public enum EDebugReport implements IFsmReport<EDebugState> {

    RP_TurningOn(ST_TurnedOn),
    RP_TurningOff(ST_TurnedOff),
    RP_StartDebug(null),
    RP_StopDebug(null);

    private EDebugState destination;
    @Override public EDebugState getDestination() {return destination;}
    EDebugReport(EDebugState state) {this.destination = state;}

}
