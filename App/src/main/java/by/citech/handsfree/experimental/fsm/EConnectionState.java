package by.citech.handsfree.experimental.fsm;

import java.util.HashSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.s;

public enum EConnectionState implements IFsmState {

    StateTurnedOff {
        @Override public HashSet<IFsmState> available() {return s(StateTurnedOn);}
    },
    StateTurnedOn {
        @Override public HashSet<IFsmState> available() {return s(StateBtPrepared, StateBtNotSupported);}
    },
    StateBtPrepared {
        @Override public HashSet<IFsmState> available() {return s(StateDeviceChosen, StateDeviceNotChosen);}
    },
    StateBtNotSupported {
        @Override public HashSet<IFsmState> available() {return s();}
    },
    StateDeviceChosen {
        @Override public HashSet<IFsmState> available() {return s(StateSearching);}
    },
    StateDeviceNotChosen {
        @Override public HashSet<IFsmState> available() {return s(StateDeviceChosen);}
    },
    StateSearching {
        @Override public HashSet<IFsmState> available() {return s(StateFound, StateNotFound);}
    },
    StateFound {
        @Override public HashSet<IFsmState> available() {return s(StateSearching, StateConnecting);}
    },
    StateNotFound {
        @Override public HashSet<IFsmState> available() {return s(StateSearching);}
    },
    StateConnecting {
        @Override public HashSet<IFsmState> available() {return s(StateDisconnected, StateIncompatible, StateConnected);}
    },
    StateDisconnected {
        @Override public HashSet<IFsmState> available() {return s(StateSearching);}
    },
    StateIncompatible {
        @Override public HashSet<IFsmState> available() {return s(StateDeviceChosen);}
    },
    StateConnected {
        @Override public HashSet<IFsmState> available() {return s(StateDisconnected, GettingStatus);}
    },
    GettingStatus {
        @Override public HashSet<IFsmState> available() {return s(StateDisconnected, GotStatus, StateConnected);}
    },
    GotStatus {
        @Override public HashSet<IFsmState> available() {return s(StateDisconnected, GettingInitData);}
    },
    GettingInitData {
        @Override public HashSet<IFsmState> available() {return s(StateDisconnected, GotStatus, GotInitData);}
    },
    GotInitData {
        @Override public HashSet<IFsmState> available() {return s(GettingInitData, StateDisconnected);}
    },
    Failure {
        @Override public HashSet<IFsmState> available() {return s();}
    };

    @Override public String getName() {return this.name();}
    @Override public HashSet<IFsmState> availableFromAny() {return s(Failure, StateTurnedOff);}

}
