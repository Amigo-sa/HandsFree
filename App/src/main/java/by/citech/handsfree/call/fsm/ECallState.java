package by.citech.handsfree.call.fsm;

import java.util.HashSet;

import static by.citech.handsfree.util.CollectionHelper.s;

public enum ECallState {

    TurnedOff {
        @Override public HashSet<ECallState> available() {return s(TurnedOn);}
    },
    TurnedOn {
        @Override public HashSet<ECallState> available() {return s(Failure, DebugLoop, DebugRecord, PhaseReadyExt, PhaseReadyInt);}
    },
    PhaseZero {
        @Override public HashSet<ECallState> available() {return s(Failure, DebugLoop, DebugRecord, PhaseReadyExt, PhaseReadyInt);}
    },
    PhaseReadyExt {
        @Override public HashSet<ECallState> available() {return s(Failure, PhaseZero, PhaseReadyInt, ReadyToWork);}
    },
    PhaseReadyInt {
        @Override public HashSet<ECallState> available() {return s(Failure, PhaseZero, PhaseReadyExt, ReadyToWork);}
    },
    ReadyToWork {
        @Override public HashSet<ECallState> available() {return s(Failure, PhaseReadyExt, PhaseReadyInt, OutStarted, InDetected);}
    },
    OutStarted {
        @Override public HashSet<ECallState> available() {return s(Failure, Error, ReadyToWork, PhaseReadyExt, PhaseReadyInt, OutConnected);}
    },
    OutConnected {
        @Override public HashSet<ECallState> available() {return s(Failure, Error, ReadyToWork, PhaseReadyExt, PhaseReadyInt, Call);}
    },
    InDetected {
        @Override public HashSet<ECallState> available() {return s(Failure, Error, ReadyToWork, PhaseReadyExt, PhaseReadyInt, Call);}
    },
    Call {
        @Override public HashSet<ECallState> available() {return s(Failure, Error, ReadyToWork, PhaseReadyExt, PhaseReadyInt);}
    },
    Error {
        @Override public HashSet<ECallState> available() {return s(ReadyToWork);}
    },
    Failure {
        @Override public HashSet<ECallState> available() {return s(PhaseZero);}
    },

    //--------------------- debug

    DebugRecord {
        @Override public HashSet<ECallState> available() {return s(DebugRecorded);}
    },
    DebugRecorded {
        @Override public HashSet<ECallState> available() {return s(DebugPlay);}
    },
    DebugPlay {
        @Override public HashSet<ECallState> available() {return s(DebugRecorded);}
    },
    DebugLoop {
        @Override public HashSet<ECallState> available() {return s(PhaseZero);}
    };

    public String getName() {return this.toString();}
    public abstract HashSet<ECallState> available();
    public static HashSet<ECallState> availableFromAny() {return s(TurnedOff);}

}
