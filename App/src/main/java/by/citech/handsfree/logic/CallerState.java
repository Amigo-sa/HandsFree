package by.citech.handsfree.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public enum CallerState {

    PhaseZero {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, DebugLoop, DebugRecord, PhaseReadyExt, PhaseReadyInt));
        }
    },

    PhaseReadyExt {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, PhaseZero, PhaseReadyInt, Idle));
        }
    },

    PhaseReadyInt {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, PhaseZero, PhaseReadyExt, Idle));
        }
    },

    Idle {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, OutStarted, InDetected));
        }
    },

    OutStarted {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, Idle, OutConnected));
        }
    },

    OutConnected {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, Idle, Call));
        }
    },

    InDetected {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, Idle, Call));
        }
    },

    Call {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, Idle));
        }
    },

    Error {
        public HashSet<CallerState> availableStates() {
            return new HashSet<>(Collections.singletonList(Idle));
        }
    },

    Failure {
        public HashSet<CallerState> availableStates() {
            return new HashSet<>(Collections.singletonList(PhaseZero));
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

    DebugLoop {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Collections.singletonList(PhaseZero));
        }
    };

    public String getName() {return this.toString();}
    public abstract HashSet<CallerState> availableStates();

}
