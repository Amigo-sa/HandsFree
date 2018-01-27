package by.citech.handsfree.experimental.fsm;

import android.view.ViewDebug;

import java.util.HashSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.s;

public enum EConnectionState implements IFsmState {

    TurnedOff {
        @Override public HashSet<IFsmState> available() {return s(TurnedOn);}
    },
    TurnedOn {
        @Override public HashSet<IFsmState> available() {return s(BtPrepared, BtNotSupported);}
    },
    BtPrepared {
        @Override public HashSet<IFsmState> available() {return s(DeviceChosen, DeviceNotChosen);}
    },
    BtNotSupported {
        @Override public HashSet<IFsmState> available() {return s();}
    },
    DeviceChosen {
        @Override public HashSet<IFsmState> available() {return s(Searching);}
    },
    DeviceNotChosen {
        @Override public HashSet<IFsmState> available() {return s(DeviceChosen);}
    },
    Searching {
        @Override public HashSet<IFsmState> available() {return s(Found, NotFound);}
    },
    Found {
        @Override public HashSet<IFsmState> available() {return s(Searching, Connecting);}
    },
    NotFound {
        @Override public HashSet<IFsmState> available() {return s(Searching);}
    },
    Connecting {
        @Override public HashSet<IFsmState> available() {return s(Disconnected, Incompatible, Connected);}
    },
    Disconnected {
        @Override public HashSet<IFsmState> available() {return s(Searching);}
    },
    Incompatible {
        @Override public HashSet<IFsmState> available() {return s(DeviceChosen);}
    },
    Connected {
        @Override public HashSet<IFsmState> available() {return s(Disconnected, GettingStatus);}
    },
    GettingStatus {
        @Override public HashSet<IFsmState> available() {return s(Disconnected, GotStatus, Connected);}
    },
    GotStatus {
        @Override public HashSet<IFsmState> available() {return s(Disconnected, GettingInitData);}
    },
    GettingInitData {
        @Override public HashSet<IFsmState> available() {return s(Disconnected, GotStatus, GotInitData);}
    },
    GotInitData {
        @Override public HashSet<IFsmState> available() {return s(GettingInitData, Disconnected);}
    },
    Failure {
        @Override public HashSet<IFsmState> available() {return s();}
    };

    @Override public String getName() {return this.name();}
    @Override public HashSet<IFsmState> availableFromAny() {return s(Failure, TurnedOff);}

}
