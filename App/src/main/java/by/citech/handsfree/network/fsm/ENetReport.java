package by.citech.handsfree.network.fsm;

import by.citech.handsfree.fsm.IFsmReport;

public enum ENetReport implements IFsmReport<ENetState> {

    RP_TurningOn,
    RP_TurningOff;

    @Override
    public ENetState getDestination() {
        return null;
    }

}
