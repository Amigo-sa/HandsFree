package by.citech.handsfree.activity.fsm;

import java.util.HashSet;

import static by.citech.handsfree.util.CollectionHelper.s;

public enum EActivityState {

    TurnedOff {
        @Override public HashSet<EActivityState> available() {return s(TurnedOn);}
    },
    TurnedOn {
        @Override public HashSet<EActivityState> available() {return s(LightA);}
    },
    ScanA2SettingsA {
        @Override public HashSet<EActivityState> available() {return s(SettingsA);}
    },
    ScanA2LightA {
        @Override public HashSet<EActivityState> available() {return s(LightA);}
    },
    LightA2SettingsA {
        @Override public HashSet<EActivityState> available() {return s(SettingsA);}
    },
    LightA2ScanA {
        @Override public HashSet<EActivityState> available() {return s(ScanA);}
    },
    SettingsA2ScanA {
        @Override public HashSet<EActivityState> available() {return s(ScanA);}
    },
    SettingsA2LightA {
        @Override public HashSet<EActivityState> available() {return s(LightA);}
    },
    PowerOn {
        @Override public HashSet<EActivityState> available() {return s(ScanA, LightA, SettingsA);}
    },
    BackArrow {
        @Override public HashSet<EActivityState> available() {return s(ScanA, LightA, SettingsA);}
    },
    Destroyed {
        @Override public HashSet<EActivityState> available() {return s(ScanA, LightA, SettingsA);}
    },
    Home {
        @Override public HashSet<EActivityState> available() {return s(ScanA, LightA, SettingsA);}
    },
    Back {
        @Override public HashSet<EActivityState> available() {return s(ScanA, LightA, SettingsA);}
    },
    PowerOff {
        @Override public HashSet<EActivityState> available() {return s(PowerOn);}
    },
    ScanA {
        @Override public HashSet<EActivityState> available() {return s(BackArrow, Destroyed, Home, Back, PowerOff, ScanA2SettingsA, ScanA2LightA);}
    },
    LightA {
        @Override public HashSet<EActivityState> available() {return s(BackArrow, Destroyed, Home, Back, PowerOff, LightA2SettingsA, LightA2ScanA);}
    },
    SettingsA {
        @Override public HashSet<EActivityState> available() {return s(BackArrow, Destroyed, Home, Back, PowerOff, SettingsA2LightA, SettingsA2ScanA);}
    };

    public String getName() {return this.toString();}
    public abstract HashSet<EActivityState> available();
    public static HashSet<EActivityState> availableFromAny() {return s(TurnedOff);}

}
