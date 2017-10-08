package by.citech.logic;

public enum State {

    Idle {
        public State[] availableStates() {
            return new State[] { Error, OutgoingCall, IncomeCall };
        }

        public String getName() {
            return "Idle";
        }
    },           // -> Error, OutgoingCall, IncomeCall

    Error {
        public State[] availableStates() {
            return new State[] { Idle };
        }

        public String getName() {
            return "Error";
        }
    },          // -> Idle

    OutgoingCall {
        public State[] availableStates() {
            return new State[] { Error, NotAnswer, Connected };
        }

        public String getName() {
            return "Outgoing call";
        }
    },   // -> Error, NotAnswer, Connected

    NotAnswer {
        public State[] availableStates() {
            return new State[] { Error, Idle };
        }

        public String getName() {
            return "Not answer";
        }
    },      // -> Error, Idle

    IncomeCall {
        public State[] availableStates() {
            return new State[] { Error, AcceptCall };
        }

        public String getName() {
            return "Income call";
        }
    },     // -> Error, AcceptCall

    AcceptCall {
        public State[] availableStates() {
            return new State[] { Error, Connected };
        }

        public String getName() {
            return "Accept call";
        }
    },     // -> Error, Connected

    Connected {
        public State[] availableStates() {
            return new State[] { Error, HangupCall };
        }

        public String getName() {
            return "Connected";
        }
    },      // -> Error, HangupCall

    HangupCall {
        public State[] availableStates() {
            return new State[] { Error, Idle };
        }

        public String getName() {
            return "Hangup call";
        }
    },     // -> Error, Idle

    DeclineCall {
        public State[] availableStates() {
            return new State[] { Error, Idle };
        }

        public String getName() {
            return "Decline call";
        }
    };    // -> Error, Idle

    public abstract String getName();
}
