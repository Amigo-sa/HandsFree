package by.citech.handsfree.network.fsm;

import java.util.EnumSet;

import by.citech.handsfree.fsm.IFsmState;

public enum ENetState implements IFsmState<ENetState> {

    ST_TurnedOff,
    ST_TurnedOn;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public EnumSet<ENetState> available() {
        return null;
    }

    @Override
    public EnumSet<ENetState> availableFromAny() {
        return null;
    }
}
