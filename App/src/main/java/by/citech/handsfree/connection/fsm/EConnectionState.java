package by.citech.handsfree.connection.fsm;

import java.util.HashSet;

import static by.citech.handsfree.util.CollectionHelper.s;

public enum EConnectionState {

    TurnedOff {
        @Override public HashSet<EConnectionState> available() {return s(TurnedOn);}
    },
    TurnedOn {
        @Override public HashSet<EConnectionState> available() {return s(BtPrepared, BtNotSupported);}
    },
    BtPrepared {
        @Override public HashSet<EConnectionState> available() {return s(DeviceChosen, DeviceNotChosen);}
    },
    BtNotSupported {
        @Override public HashSet<EConnectionState> available() {return s();}
    },
    DeviceChosen {
        @Override public HashSet<EConnectionState> available() {return s(Searching);}
    },
    DeviceNotChosen {
        @Override public HashSet<EConnectionState> available() {return s(DeviceChosen);}
    },
    Searching {
        @Override public HashSet<EConnectionState> available() {return s(Found, NotFound);}
    },
    Found {
        @Override public HashSet<EConnectionState> available() {return s(Searching, Connecting);}
    },
    NotFound {
        @Override public HashSet<EConnectionState> available() {return s(Searching);}
    },
    Connecting {
        @Override public HashSet<EConnectionState> available() {return s(Disconnected, Incompatible, Connected);}
    },
    Disconnected {
        @Override public HashSet<EConnectionState> available() {return s(Searching);}
    },
    Incompatible {
        @Override public HashSet<EConnectionState> available() {return s(DeviceChosen);}
    },
    Connected {
        @Override public HashSet<EConnectionState> available() {return s(Disconnected, GettingStatus);}
    },
    GettingStatus {
        @Override public HashSet<EConnectionState> available() {return s(Disconnected, GotStatus, Connected);}
    },
    GotStatus {
        @Override public HashSet<EConnectionState> available() {return s(Disconnected, GettingInitData);}
    },
    GettingInitData {
        @Override public HashSet<EConnectionState> available() {return s(Disconnected, GotStatus, GotInitData);}
    },
    GotInitData {
        @Override public HashSet<EConnectionState> available() {return s(GettingInitData, Disconnected);}
    },
    Failure {
        @Override public HashSet<EConnectionState> available() {return s();}
    };

    public String getName() {return this.toString();}
    public abstract HashSet<EConnectionState> available();
    public static HashSet<EConnectionState> availableFromAny() {return s(Failure, TurnedOff);}

}
