package by.citech.logic;

import java.util.Arrays;
import java.util.HashSet;

public enum State {

    Null {
        public HashSet<State> availableStates() {
            return new HashSet<> (Arrays.asList(Idle, GeneralFailure));
        }
        public String getName() {
            return "Null";
        }
    },

    Idle {
        public HashSet<State> availableStates() {
            return new HashSet<> (Arrays.asList(Error, OutcomingStarted, IncomingDetected, GeneralFailure));
        }
        public String getName() {
            return "Idle";
        }
    },

    OutcomingStarted {
        public HashSet<State> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, OutcomingConnected));
        }
        public String getName() {
            return "OutcomingStarted";
        }
    },

    OutcomingConnected {
        public HashSet<State> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, Call));
        }
        public String getName() {
            return "OutcomingConnected";
        }
    },

    IncomingDetected {
        public HashSet<State> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle, Call));
        }
        public String getName() {
            return "IncomingDetected";
        }
    },

    Call {
        public HashSet<State> availableStates() {
            return new HashSet<> (Arrays.asList(Error, Idle));
        }
        public String getName() {
            return "Call";
        }
    },

    Error {
        public HashSet<State> availableStates() {
            return new HashSet<> (Arrays.asList(Idle));
        }
        public String getName() {
            return "Error";
        }
    },

    GeneralFailure {
        public HashSet<State> availableStates() {
            return new HashSet<>();
        }
        public String getName() {
            return "GeneralFailure";
        }
    };

    public abstract HashSet<State> availableStates();
    public abstract String getName();
}
