package by.citech.handsfree.activity.fsm;

import java.util.EnumSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.eCopy;
import static by.citech.handsfree.util.CollectionHelper.eSet;

public enum EActivityState implements IFsmState<EActivityState> {

    ST_TurnedOff,
    ST_TurnedOn,
    ST_CallA2SettingsA,
    ST_SettingsA2CallA,
    ST_PowerOn,
    ST_BackArrow,
    ST_Destroyed,
    ST_Home,
    ST_Back,
    ST_PowerOff,
    ST_CallA,
    ST_SettingsA;

    static {
        availableFromAny = s(ST_TurnedOff);
        ST_TurnedOff      .a(ST_TurnedOn);
        ST_TurnedOn       .a(ST_CallA);
        ST_CallA2SettingsA.a(ST_SettingsA);
        ST_SettingsA2CallA.a(ST_CallA);
        ST_PowerOn        .a(ST_CallA, ST_SettingsA);
        ST_BackArrow      .a(ST_CallA, ST_SettingsA);
        ST_Destroyed      .a(ST_CallA, ST_SettingsA);
        ST_Home           .a(ST_CallA, ST_SettingsA);
        ST_Back           .a(ST_CallA, ST_SettingsA);
        ST_PowerOff       .a(ST_PowerOn);
        ST_CallA          .a(ST_BackArrow, ST_Destroyed, ST_Home, ST_Back, ST_PowerOff, ST_CallA2SettingsA);
        ST_SettingsA      .a(ST_BackArrow, ST_Destroyed, ST_Home, ST_Back, ST_PowerOff, ST_SettingsA2CallA);
    }

    //--------------------- constructor

    EActivityState(EActivityState... states) {a(states);}
    private static EnumSet<EActivityState> availableFromAny;
    private EnumSet<EActivityState> available;
    void a(EActivityState... states) {available = s(states);}

    //--------------------- IFsmState

    @Override public String getName() {return this.name();}
    @Override public EnumSet<EActivityState> available() {return c(available);}
    @Override public EnumSet<EActivityState> availableFromAny() {return c(availableFromAny);}

    //--------------------- additional

    private static EnumSet<EActivityState> s(EActivityState... states) {return eSet(EActivityState.class, states);}
    private static EnumSet<EActivityState> c(EnumSet<EActivityState> set) {return eCopy(set);}

}
