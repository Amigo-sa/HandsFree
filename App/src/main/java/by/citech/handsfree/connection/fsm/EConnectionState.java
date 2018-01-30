package by.citech.handsfree.connection.fsm;

import java.util.HashSet;

import static by.citech.handsfree.util.CollectionHelper.hSet;

public enum EConnectionState {

    TurnedOff {
        @Override public HashSet<EConnectionState> available() {return hSet(TurnedOn);}
    },
    TurnedOn {
        @Override public HashSet<EConnectionState> available() {return hSet(BtPrepared, BtNotSupported);}
    },
    BtPrepared {
        @Override public HashSet<EConnectionState> available() {return hSet(DeviceChosen, DeviceNotChosen);}
    },
    BtNotSupported {
        @Override public HashSet<EConnectionState> available() {return hSet();}
    },
    DeviceChosen {
        @Override public HashSet<EConnectionState> available() {return hSet(Searching);}
    },
    DeviceNotChosen {
        @Override public HashSet<EConnectionState> available() {return hSet(DeviceChosen);}
    },
    Searching {
        @Override public HashSet<EConnectionState> available() {return hSet(Found, NotFound);}
    },
    Found {
        @Override public HashSet<EConnectionState> available() {return hSet(Searching, Connecting);}
    },
    NotFound {
        @Override public HashSet<EConnectionState> available() {return hSet(Searching);}
    },
    Connecting {
        @Override public HashSet<EConnectionState> available() {return hSet(Disconnected, Incompatible, Connected);}
    },
    Disconnected {
        @Override public HashSet<EConnectionState> available() {return hSet(Searching);}
    },
    Incompatible {
        @Override public HashSet<EConnectionState> available() {return hSet(DeviceChosen);}
    },
    Connected {
        @Override public HashSet<EConnectionState> available() {return hSet(Disconnected, GettingStatus);}
    },
    GettingStatus {
        @Override public HashSet<EConnectionState> available() {return hSet(Disconnected, GotStatus, Connected);}
    },
    GotStatus {
        @Override public HashSet<EConnectionState> available() {return hSet(Disconnected, GettingInitData);}
    },
    GettingInitData {
        @Override public HashSet<EConnectionState> available() {return hSet(Disconnected, GotStatus, GotInitData);}
    },
    GotInitData {
        @Override public HashSet<EConnectionState> available() {return hSet(GettingInitData, Disconnected);}
    },
    Failure {
        @Override public HashSet<EConnectionState> available() {return hSet();}
    };

    public String getName() {return this.name();}
    public abstract HashSet<EConnectionState> available();
    public static HashSet<EConnectionState> availableFromAny() {return hSet(Failure, TurnedOff);}

}
