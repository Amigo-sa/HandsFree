package by.citech.handsfree.activity.fsm;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.activity.fsm.EActivityReport.HomePressed;
import static by.citech.handsfree.activity.fsm.EActivityReport.PowerOffPressed;
import static by.citech.handsfree.activity.fsm.EActivityReport.TurningOn;
import static by.citech.handsfree.activity.fsm.EActivityState.Back;
import static by.citech.handsfree.activity.fsm.EActivityState.BackArrow;
import static by.citech.handsfree.activity.fsm.EActivityState.Destroyed;
import static by.citech.handsfree.activity.fsm.EActivityState.Home;
import static by.citech.handsfree.activity.fsm.EActivityState.LightA;
import static by.citech.handsfree.activity.fsm.EActivityState.LightA2ScanA;
import static by.citech.handsfree.activity.fsm.EActivityState.LightA2SettingsA;
import static by.citech.handsfree.activity.fsm.EActivityState.PowerOff;
import static by.citech.handsfree.activity.fsm.EActivityState.PowerOn;
import static by.citech.handsfree.activity.fsm.EActivityState.ScanA;
import static by.citech.handsfree.activity.fsm.EActivityState.ScanA2LightA;
import static by.citech.handsfree.activity.fsm.EActivityState.ScanA2SettingsA;
import static by.citech.handsfree.activity.fsm.EActivityState.SettingsA;
import static by.citech.handsfree.activity.fsm.EActivityState.SettingsA2LightA;
import static by.citech.handsfree.activity.fsm.EActivityState.SettingsA2ScanA;
import static by.citech.handsfree.activity.fsm.EActivityState.TurnedOff;
import static by.citech.handsfree.activity.fsm.EActivityState.TurnedOn;

public class ActivityFsm {

    private static final String STAG = Tags.ActivityFsm;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    //--------------------- preparation

    private Collection<IActivityFsmListener> listeners;
    private volatile EActivityState currState, prevState, prevActivityState;
    private volatile EActivityReport prevReport;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        listeners = new ConcurrentLinkedQueue<>();
        currState = TurnedOff;
        processReport(TurningOn, currState, TAG);
    }

    //--------------------- singleton

    private static volatile ActivityFsm instance = null;

    private ActivityFsm() {
    }

    public static ActivityFsm getInstance() {
        if (instance == null) {
            synchronized (ActivityFsm.class) {
                if (instance == null) {
                    instance = new ActivityFsm();
                }
            }
        }
        return instance;
    }

    //--------------------- IFsmListenerRegister register and unregister

    synchronized boolean registerListener(IActivityFsmListener listener, String who) {
        boolean isAdded;
        if (listener == null) {
            if (debug) Timber.tag(TAG).w("register fail, null listener: <%s>", who);
            return false;
        } else if (listeners.contains(listener)) {
            if (debug) Timber.tag(TAG).w("register fail, already registered: <%s>", who);
            isAdded = true;
        } else {
            isAdded = listeners.add(listener);
            if (isAdded) {if (debug) Timber.tag(TAG).i("register success: <%s>, count: <%d>", who, listeners.size());}
            else         {if (debug) Timber.tag(TAG).e("register fail: <%s>, count: still <%d>", who, listeners.size());}
        }
        if (isAdded) listener.onActivityFsmStateChange(prevState, currState, prevReport);
        return isAdded;
    }

    synchronized boolean unregisterListener(IActivityFsmListener listener, String who) {
        boolean isRemoved;
        isRemoved = listeners.remove(listener);
        if (isRemoved) {if (debug) Timber.tag(TAG).w("unregister success: <%s>, count: <%d>", who, listeners.size());}
        else           {if (debug) Timber.tag(TAG).e("unregister fail: <%s>, count: still <%d>", who, listeners.size());}
        return isRemoved;
    }

    //--------------------- IActivityFsmListener onConnectionFsmStateChange

    synchronized private void onStateChange(EActivityState from, EActivityState to, EActivityReport why) {
        if (debug) Timber.tag(TAG).w("onStateChange: <%s> ==> <%s>, report: <%s>", from, to, why);
        for (IActivityFsmListener listener : listeners) listener.onActivityFsmStateChange(from, to, why);
    }

    //--------------------- IConnectionFsmReporter

    synchronized EActivityState getCurrState() {return currState;}
    synchronized EActivityState getPrevActivityState() {return prevActivityState;}
    synchronized EActivityState getPrevState() {return prevState;}

    synchronized boolean processReport(EActivityReport report, EActivityState from, String msg) {
        if (debug) Timber.tag(TAG).w("processReport: report <%s> from <%s>, message: <%s>", report, from, msg);
        if (report == null || from == null || msg == null) {
            if (debug) Timber.e("processReport %s", StatusMessages.ERR_PARAMETERS);
            return false;
        }
        return processReportNormal(report, from);
    }

    //--------------------- processing

    private boolean processReportNormal(EActivityReport report, EActivityState from) {
        if (debug) Timber.tag(TAG).i("processReportNormal");
        switch (report) {
            case BackArrowPressed:
                return processStateChange(from, BackArrow, report);
            case onDestroy:
                return processStateChange(from, Destroyed, report);
            case SettingsA2ScanAPressed:
                return processStateChange(from, SettingsA2ScanA, report);
            case SettingsA2LightAPressed:
                return processStateChange(from, SettingsA2LightA, report);
            case TurningOn:
                return processStateChange(from, TurnedOn, report);
            case TurningOff:
                return processStateChange(from, TurnedOff, report);
            case ScanA2LightAPressed:
                return processStateChange(from, ScanA2LightA, report);
            case ScanA2SettingsAPressed:
                return processStateChange(from, ScanA2SettingsA, report);
            case LightA2ScanAPressed:
                return processStateChange(from, LightA2ScanA, report);
            case LightA2SettingsAPressed:
                return processStateChange(from, LightA2SettingsA, report);
            case ScanAOnCreate:
                return processStateChange(from, ScanA, report);
            case LightAOnCreate:
                return processStateChange(from, LightA, report);
            case SettingsAOnCreate:
                return processStateChange(from, SettingsA, report);
            case BackPressed:
                return processStateChange(from, Back, report);
            case HomePressed:
                return processStateChange(from, Home, report);
            case PowerOnPressed:
                return processStateChange(from, PowerOn, report);
            case PowerOffPressed:
                return processStateChange(from, PowerOff, report);
            default:
                return true;
        }
    }

    synchronized private boolean processStateChange(EActivityState from, EActivityState to, EActivityReport why) {
        return processStateChange(from, to, why, false);
    }

    private boolean processStateChange(EActivityState from, EActivityState to, EActivityReport why, boolean isForce) {
        if (currState == from || isForce) {
            if (EActivityState.availableFromAny().contains(to) || from.available().contains(to) || isForce) {
                if (isActivity(from) && isTransition(why)) prevActivityState = from;
                prevReport = why;
                prevState = currState;
                currState = to;
                onStateChange(from, to, why);
                return true;
            } else if (debug) Timber.tag(TAG).e("processStateChange: <%s> not available from <%s>", to, from);
        } else if (debug) Timber.tag(TAG).e("processStateChange: current currState is <%s>, not <%s>", currState, from);
        return false;
    }

    private boolean isTransition(EActivityReport report) {
        return report != PowerOffPressed && report != HomePressed;
    }

    private boolean isActivity(EActivityState state) {
        return state == ScanA || state == SettingsA || state == LightA;
    }

}
