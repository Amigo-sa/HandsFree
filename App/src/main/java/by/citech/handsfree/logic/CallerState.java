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
            return new HashSet<> (Arrays.asList(Failure, PhaseZero, PhaseReadyInt, ReadyToWork));
        }
    },

    PhaseReadyInt {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, PhaseZero, PhaseReadyExt, ReadyToWork));
        }
    },

    ReadyToWork {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, OutStarted, InDetected));
        }
    },

    OutStarted {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, ReadyToWork, OutConnected));
        }
    },

    OutConnected {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, ReadyToWork, Call));
        }
    },

    InDetected {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, ReadyToWork, Call));
        }
    },

    Call {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Failure, Error, ReadyToWork));
        }
    },

    Error {
        public HashSet<CallerState> availableStates() {
            return new HashSet<>(Collections.singletonList(ReadyToWork));
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
