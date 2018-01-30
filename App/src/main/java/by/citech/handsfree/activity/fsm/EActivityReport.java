package by.citech.handsfree.activity.fsm;

import by.citech.handsfree.fsm.IFsmReport;

import static by.citech.handsfree.activity.fsm.EActivityState.*;

public enum EActivityReport implements IFsmReport<EActivityState> {

    TurningOn             (ST_TurnedOn),
    TurningOff            (ST_TurnedOff),
    BackArrowPressed      (ST_BackArrow),
    HomePressed           (ST_Home),
    BackPressed           (ST_Back),
    PowerOffPressed       (ST_PowerOff),
    PowerOnPressed        (ST_PowerOn),
    CallA2SettingsAPressed(ST_CallA2SettingsA),
    SettingsA2CallAPressed(ST_SettingsA2CallA),
    onDestroy             (ST_Destroyed),
    CallAOnCreate         (ST_CallA),
    SettingsAOnCreate     (ST_SettingsA);

    private EActivityState destination;
    @Override public EActivityState getDestination() {return destination;}
    EActivityReport(EActivityState state) {this.destination = state;}

}
