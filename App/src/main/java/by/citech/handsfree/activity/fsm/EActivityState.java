package by.citech.handsfree.activity.fsm;

import java.util.HashSet;

import static by.citech.handsfree.util.CollectionHelper.hSet;

public enum EActivityState {

    TurnedOff {
        @Override public HashSet<EActivityState> available() {return hSet(TurnedOn);}
    },
    TurnedOn {
        @Override public HashSet<EActivityState> available() {return hSet(CallA);}
    },
    CallA2SettingsA {
        @Override public HashSet<EActivityState> available() {return hSet(SettingsA);}
    },
    SettingsA2CallA {
        @Override public HashSet<EActivityState> available() {return hSet(CallA);}
    },
    PowerOn {
        @Override public HashSet<EActivityState> available() {return hSet(CallA, SettingsA);}
    },
    BackArrow {
        @Override public HashSet<EActivityState> available() {return hSet(CallA, SettingsA);}
    },
    Destroyed {
        @Override public HashSet<EActivityState> available() {return hSet(CallA, SettingsA);}
    },
    Home {
        @Override public HashSet<EActivityState> available() {return hSet(CallA, SettingsA);}
    },
    Back {
        @Override public HashSet<EActivityState> available() {return hSet(CallA, SettingsA);}
    },
    PowerOff {
        @Override public HashSet<EActivityState> available() {return hSet(PowerOn);}
    },
    CallA {
        @Override public HashSet<EActivityState> available() {return hSet(BackArrow, Destroyed, Home, Back, PowerOff, CallA2SettingsA);}
    },
    SettingsA {
        @Override public HashSet<EActivityState> available() {return hSet(BackArrow, Destroyed, Home, Back, PowerOff, SettingsA2CallA);}
    };

    public String getName() {return this.name();}
    public abstract HashSet<EActivityState> available();
    public static HashSet<EActivityState> availableFromAny() {return hSet(TurnedOff);}

}
