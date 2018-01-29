package by.citech.handsfree.debug.fsm;

import java.util.HashSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.s;

public enum EDebugState implements IFsmState {

    ST_TurnedOff,
    ST_TurnedOn,
    ST_DebugRecord,
    ST_DebugRecorded,
    ST_DebugPlay,
    ST_DebugLoop;

    public String getName() {return this.name();}
    @Override public HashSet<IFsmState> available(){return availableSt(this);};
    @Override public HashSet<IFsmState> availableFromAny() {return s(ST_TurnedOff);}

    public static HashSet<IFsmState> availableSt(EDebugState state) {
        switch (state) {
            case ST_TurnedOff:
                return s(ST_TurnedOn);
            case ST_TurnedOn:
                return s(ST_DebugLoop, ST_DebugRecord);
            case ST_DebugRecord:
                return s(ST_DebugRecorded);
            case ST_DebugRecorded:
                return s(ST_DebugPlay);
            case ST_DebugPlay:
                return s(ST_DebugRecorded);
            case ST_DebugLoop:
                return s(ST_TurnedOn);
            default:
                return s();
        }
    }

}
