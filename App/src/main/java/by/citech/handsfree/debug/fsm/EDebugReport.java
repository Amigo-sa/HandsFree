package by.citech.handsfree.debug.fsm;

import by.citech.handsfree.fsm.IFsmReport;

public enum EDebugReport implements IFsmReport {
    RP_TurningOn,
    RP_TurningOff,
    RP_StartDebug,
    RP_StopDebug,
}
