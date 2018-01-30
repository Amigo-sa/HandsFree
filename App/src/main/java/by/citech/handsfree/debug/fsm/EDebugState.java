package by.citech.handsfree.debug.fsm;

import java.util.EnumSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.eCopy;
import static by.citech.handsfree.util.CollectionHelper.eSet;

public enum EDebugState implements IFsmState<EDebugState> {

    ST_TurnedOff,
    ST_TurnedOn,
    ST_DebugRecord,
    ST_DebugRecorded,
    ST_DebugPlay,
    ST_DebugLoop;

    static {
        availableFromAny = s(ST_TurnedOff);
        ST_TurnedOff    .a(ST_TurnedOn);
        ST_TurnedOn     .a(ST_DebugLoop, ST_DebugRecord);
        ST_DebugRecord  .a(ST_DebugRecorded);
        ST_DebugRecorded.a(ST_DebugPlay);
        ST_DebugPlay    .a(ST_DebugRecorded);
        ST_DebugLoop    .a(ST_TurnedOn);
    }

    //--------------------- constructor

    EDebugState(EDebugState... states) {a(states);}
    private static EnumSet<EDebugState> availableFromAny;
    private EnumSet<EDebugState> available;
    void a(EDebugState... states) {available = s(states);}

    //--------------------- IFsmState

    @Override public String getName() {return this.name();}
    @Override public EnumSet<EDebugState> available() {return c(available);}
    @Override public EnumSet<EDebugState> availableFromAny() {return c(availableFromAny);}

    //--------------------- additional

    private static EnumSet<EDebugState> s(EDebugState... states) {return eSet(EDebugState.class, states);}
    private static EnumSet<EDebugState> c(EnumSet<EDebugState> set) {return eCopy(set);}

}
