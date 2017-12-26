package by.citech.handsfree.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public enum CallerState {

    Null {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Idle, GeneralFailure, DebugLoopBack, DebugRecord, PreparationPhase1));//TODO: убрать Idle
        }
    },

    PreparationPhase1 {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Null, GeneralFailure, PreparationPhase2));
        }
    },

    PreparationPhase2 {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Null, GeneralFailure, Idle));
        }
    },

    Idle {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, OutStarted, InDetected));
        }
    },

    OutStarted {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, OutConnected));
        }
    },

    OutConnected {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, Call));
        }
    },

    InDetected {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, Call));
        }
    },

    Call {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle));
        }
    },

    Error {
        public HashSet<CallerState> availableStates() {
            return new HashSet<>(Collections.singletonList(Idle));
        }
    },

    GeneralFailure {
        public HashSet<CallerState> availableStates() {
            return new HashSet<>();
        }
    },

    //--------------------- debug

    DebugRecord {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Collections.singletonList(DebugRecorded));
        }
    },

    DebugRecorded {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Collections.singletonList(DebugPlay));
        }
    },

    DebugPlay {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Collections.singletonList(DebugRecorded));
        }
    },

    DebugLoopBack {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Collections.singletonList(Null));
        }
    };

    public String getName() {return this.toString();}
    public abstract HashSet<CallerState> availableStates();

}
