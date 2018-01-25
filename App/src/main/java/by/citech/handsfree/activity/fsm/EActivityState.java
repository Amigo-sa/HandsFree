package by.citech.handsfree.activity.fsm;

import java.util.HashSet;

import static by.citech.handsfree.util.CollectionHelper.s;

public enum EActivityState {

    TurnedOff {
        @Override public HashSet<EActivityState> available() {return s(TurnedOn);}
    },
    TurnedOn {
        @Override public HashSet<EActivityState> available() {return s(CallA);}
    },
    CallA2SettingsA {
        @Override public HashSet<EActivityState> available() {return s(SettingsA);}
    },
    SettingsA2CallA {
        @Override public HashSet<EActivityState> available() {return s(CallA);}
    },
    PowerOn {
        @Override public HashSet<EActivityState> available() {return s(CallA, SettingsA);}
    },
    BackArrow {
        @Override public HashSet<EActivityState> available() {return s(CallA, SettingsA);}
    },
    Destroyed {
        @Override public HashSet<EActivityState> available() {return s(CallA, SettingsA);}
    },
    Home {
        @Override public HashSet<EActivityState> available() {return s(CallA, SettingsA);}
    },
    Back {
        @Override public HashSet<EActivityState> available() {return s(CallA, SettingsA);}
    },
    PowerOff {
        @Override public HashSet<EActivityState> available() {return s(PowerOn);}
    },
    CallA {
        @Override public HashSet<EActivityState> available() {return s(BackArrow, Destroyed, Home, Back, PowerOff, CallA2SettingsA);}
    },
    SettingsA {
        @Override public HashSet<EActivityState> available() {return s(BackArrow, Destroyed, Home, Back, PowerOff, SettingsA2CallA);}
    };

    public String getName() {return this.toString();}
    public abstract HashSet<EActivityState> available();
    public static HashSet<EActivityState> availableFromAny() {return s(TurnedOff);}

}
