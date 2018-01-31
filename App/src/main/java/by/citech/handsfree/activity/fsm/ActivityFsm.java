package by.citech.handsfree.activity.fsm;

import android.support.annotation.CallSuper;

import java.util.EnumMap;

import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.fsm.IFsmListener;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.activity.fsm.EActivityReport.*;
import static by.citech.handsfree.activity.fsm.EActivityState.*;

public class ActivityFsm extends FsmCore<EActivityReport, EActivityState> {

    private volatile EActivityState prevActivityState;

    //--------------------- singleton

    private static volatile ActivityFsm instance = null;

    private ActivityFsm() {
        super(Tags.ActivityFsm);
        reportToStateMap = new EnumMap<>(EActivityReport.class);
        currState = ST_TurnedOff;
        processReport(RP_TurningOn, getFsmCurrentState(), Tags.ActivityFsm);
    }

    public static ActivityFsm getInstance() {
        if (instance == null) {
            synchronized (ActivityFsm.class) {
                if (instance == null) {instance = new ActivityFsm();}}}
        return instance;
    }

    //--------------------- ICallFsmReporter

    synchronized EActivityState getPrevActivityState() {return prevActivityState;}

    synchronized boolean processReport(EActivityReport report, EActivityState from, String msg) {
        return checkFsmReport(report, from, msg) && processFsmReport(report, from);
    }

    //--------------------- processing

    @Override
    protected boolean processFsmReport(EActivityReport report, EActivityState from) {
        if (debug) Timber.i("processFsmReport");
        return processFsmStateChange(report, from, report.getDestination());
    }

    //--------------------- processing

    @Override
    protected boolean processFsmStateChange(EActivityState from, EActivityState to, EActivityReport why, boolean isForce) {
        if (!super.processFsmStateChange(from, to, why, isForce)) {
            return false;
        } else {
            if (isActivity(from) && isTransition(why)) prevActivityState = from;
            return true;
        }
    }

    private boolean isTransition(EActivityReport report) {
        return report != RP_PowerOffPressed && report != RP_HomePressed;
    }

    private boolean isActivity(EActivityState state) {
        return state == ST_SettingsA || state == ST_CallA;
    }

    //--------------------- interfaces

    public interface IActivityFsmReporter {
        @CallSuper
        default EActivityState getActivityFsmCurrState() {
            return getInstance().getFsmCurrentState();
        }
        @CallSuper
        default EActivityState getActivityFsmPrevState() {
            return getInstance().getFsmPreviousState();
        }
        @CallSuper
        default EActivityState getActivityFsmPrevActivityState() {
            return getInstance().getPrevActivityState();
        }
        @CallSuper
        default boolean reportToActivityFsm(EActivityState fromWhichState, EActivityReport whatHappened, String fromWho) {
            return getInstance().processReport(whatHappened, fromWhichState, fromWho);
        }
    }

    public interface IActivityFsmListenerRegister {
        @CallSuper
        default boolean registerActivityFsmListener(IActivityFsmListener listener, String who) {
            return getInstance().registerFsmListener(listener, who);
        }
        @CallSuper
        default boolean unregisterActivityFsmListener(IActivityFsmListener listener, String who) {
            return getInstance().registerFsmListener(listener, who);
        }
    }

    public interface IActivityFsmListener extends IFsmListener<EActivityReport, EActivityState> {}

}
