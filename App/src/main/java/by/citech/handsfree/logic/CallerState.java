package by.citech.handsfree.logic;

import java.util.Arrays;
import java.util.HashSet;

public enum CallerState {

    Null {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Idle, GeneralFailure, DebugLoopBack, DebugRecord));
        }
    },

    Idle {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, OutcomingStarted, IncomingDetected));
        }
    },

    OutcomingStarted {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, OutcomingConnected));
        }
    },

    OutcomingConnected {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, Call));
        }
    },

    IncomingDetected {
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
            return new HashSet<>(Arrays.asList(Idle));
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
            return new HashSet<> (Arrays.asList(DebugRecorded));
        }
    },

    DebugRecorded {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(DebugPlay));
        }
    },

    DebugPlay {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(DebugRecorded));
        }
    },

    DebugLoopBack {
        public HashSet<CallerState> availableStates() {
            return new HashSet<> (Arrays.asList(Null));
        }
    };

    public String getName() {return this.toString();}
    public abstract HashSet<CallerState> availableStates();

}
