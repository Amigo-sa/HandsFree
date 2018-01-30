package by.citech.handsfree.debug.fsm;

import java.util.HashSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.hSet;

public enum EDebugState implements IFsmState {

    ST_TurnedOff,
    ST_TurnedOn,
    ST_DebugRecord,
    ST_DebugRecorded,
    ST_DebugPlay,
    ST_DebugLoop;

    public String getName() {return this.name();}
    @Override public HashSet<IFsmState> available() {return availableSt(this);};
    @Override public HashSet<IFsmState> availableFromAny() {return hSet(ST_TurnedOff);}

    public static HashSet<IFsmState> availableSt(EDebugState state) {
        switch (state) {
            case ST_TurnedOff:
                return hSet(ST_TurnedOn);
            case ST_TurnedOn:
                return hSet(ST_DebugLoop, ST_DebugRecord);
            case ST_DebugRecord:
                return hSet(ST_DebugRecorded);
            case ST_DebugRecorded:
                return hSet(ST_DebugPlay);
            case ST_DebugPlay:
                return hSet(ST_DebugRecorded);
            case ST_DebugLoop:
                return hSet(ST_TurnedOn);
            default:
                return hSet();
        }
    }

}
