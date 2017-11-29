package by.citech.logic;

import java.util.Arrays;
import java.util.HashSet;

public enum CallerState {

    Null {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Idle, GeneralFailure, DebugLoopBack, DebugRecord));
        }
        public String getName() {
            return "Null";
        }
    },

    Idle {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, OutcomingStarted, IncomingDetected));
        }
        public String getName() {
            return "Idle";
        }
    },

    OutcomingStarted {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, OutcomingConnected));
        }
        public String getName() {
            return "OutcomingStarted";
        }
    },

    OutcomingConnected {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, Call));
        }
        public String getName() {
            return "OutcomingConnected";
        }
    },

    IncomingDetected {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, Call));
        }
        public String getName() {
            return "IncomingDetected";
        }
    },

    Call {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle));
        }
        public String getName() {
            return "Call";
        }
    },

    Error {
        public HashSet<CallerState> availableStates() {
            return new HashSet<>(Arrays.asList(Idle));
        }
        public String getName() {
            return "Error";
        }
    },

    GeneralFailure {
        public HashSet<CallerState> availableStates() {
            return new HashSet<>();
        }
        public String getName() {
            return "GeneralFailure";
        }
    },

    //--------------------- debug

    DebugRecord {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(DebugRecorded));
        }
        public String getName() {
            return "DebugRecord";
        }
    },

    DebugRecorded {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(DebugPlay));
        }
        public String getName() {
            return "DebugRecorded";
        }
    },

    DebugPlay {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(DebugRecorded));
        }
        public String getName() {
            return "DebugPlay";
        }
    },

    DebugLoopBack {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Null));
        }
        public String getName() {
            return "DebugLoopBack";
        }
    };

    public abstract HashSet<CallerState> availableStates();
    public abstract String getName();
}
