package by.citech.handsfree.activity.fsm;

import by.citech.handsfree.fsm.IFsmReport;

import static by.citech.handsfree.activity.fsm.EActivityState.*;

public enum EActivityReport implements IFsmReport<EActivityState> {

    RP_TurningOn(ST_TurnedOn),
    RP_TurningOff(ST_TurnedOff),
    RP_BackArrowPressed(ST_BackArrow),
    RP_HomePressed(ST_Home),
    RP_BackPressed(ST_Back),
    RP_PowerOffPressed(ST_PowerOff),
    RP_PowerOnPressed(ST_PowerOn),
    RP_CallA2SettingsAPressed(ST_CallA2SettingsA),
    RP_SettingsA2CallAPressed(ST_SettingsA2CallA),
    RP_onDestroy(ST_Destroyed),
    RP_CallAOnCreate(ST_CallA),
    RP_SettingsAOnCreate(ST_SettingsA);

    private EActivityState destination;
    @Override public EActivityState getDestination() {return destination;}
    EActivityReport(EActivityState state) {this.destination = state;}

}
