package by.citech.handsfree.call.fsm;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.settings.EOpMode;
import timber.log.Timber;

import static by.citech.handsfree.settings.EOpMode.Normal;

public class CallFsm
        implements ISettingsCtrl, IPrepareObject {

    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private EOpMode opMode;
    private volatile Collection<ICallFsmListener> listeners;
    private volatile ECallState currState;
    private volatile ECallState prevState;
    private volatile ECallReport prevReport;

    {
        opMode = Settings.Common.opMode;
        currState = ECallState.PhaseZero;
        listeners = new ConcurrentLinkedQueue<>();
        processStateChange(getState(), ECallState.PhaseZero, ECallReport.TurningOn, true);
    }

    //--------------------- singleton

    private static volatile CallFsm instance = null;

    private CallFsm() {
    }

    public static CallFsm getInstance() {
        if (instance == null) {
            synchronized (CallFsm.class) {
                if (instance == null) {
                    instance = new CallFsm();
                }
            }
        }
        return instance;
    }

    //--------------------- ICallFsmListenerRegister

    synchronized boolean registerListener(ICallFsmListener listener, String who) {
        boolean isAdded;
        if (listener == null) {
            if (debug) Timber.w("register fail, null listener: <%s>", who);
            return false;
        } else if (listeners.contains(listener)) {
            if (debug) Timber.w("register fail, already registered: <%s>", who);
            isAdded = true;
        } else {
            isAdded = listeners.add(listener);
            if (isAdded) {if (debug) Timber.i("register success: <%s>, count: <%d>", who, listeners.size());}
            else         {if (debug) Timber.e("register fail: <%s>, count: still <%d>", who, listeners.size());}
        }
        if (isAdded) listener.onCallerStateChange(prevState, currState, prevReport);
        return isAdded;
    }

    synchronized boolean unregisterListener(ICallFsmListener listener, String who) {
        boolean isRemoved;
        isRemoved = listeners.remove(listener);
        if (isRemoved) {if (debug) Timber.w("unregister success: <%s>, count: <%d>", who, listeners.size());}
        else           {if (debug) Timber.e("unregister fail: <%s>, count: still <%d>", who, listeners.size());}
        return isRemoved;
    }

    //--------------------- ICallFsmListener onCallerStateChange

    synchronized private void onStateChange(ECallState from, ECallState to, ECallReport why) {
        if (debug) Timber.w("onConnectionFsmStateChange: <%s> ==> <%s>, report: <%s>", from, to, why);
        for (ICallFsmListener listener : listeners) listener.onCallerStateChange(from, to, why);
    }

    //--------------------- ICallFsmReporter

    synchronized ECallState getState() {
        return currState;
    }

    synchronized boolean processReport(ECallReport report, ECallState from, String msg) {
        if (debug) Timber.w("processReport: report <%s> from <%s>, message: <%s>", report, from, msg);
        if (report == null || from == null || msg == null) {
            if (debug) Timber.e("processReport %s", StatusMessages.ERR_PARAMETERS);
            return false;
        } else if (opMode == Normal) {
            return processReportNormal(report, from);
        } else {
            return processReportAbnormal(report, from);
        }
    }

    //--------------------- process values

    synchronized private boolean processReportNormal(ECallReport report, ECallState from) {
        if (debug) Timber.i("processReportNormal");
        switch (report) {
            case SysIntError:
                switch (from) {
                    case PhaseReadyInt:
                        return (processStateChange(from, ECallState.PhaseZero, report));
                    default:
                        return (processStateChange(from, ECallState.PhaseReadyExt, report));
                }
            case SysIntReady:
                switch (from) {
                    case PhaseZero:
                        return (processStateChange(from, ECallState.PhaseReadyInt, report));
                    case PhaseReadyExt:
                        return (processStateChange(from, ECallState.ReadyToWork, report));
                    default:
                        return true;
                }
            case SysExtError:
                switch (from) {
                    case PhaseReadyExt:
                        return (processStateChange(from, ECallState.PhaseZero, report));
                    default:
                        return (processStateChange(from, ECallState.PhaseReadyInt, report));
                }
            case SysExtReady:
                switch (from) {
                    case PhaseZero:
                        return (processStateChange(from, ECallState.PhaseReadyExt, report));
                    case PhaseReadyInt:
                        return (processStateChange(from, ECallState.ReadyToWork, report));
                    default:
                        return true;
                }
            case OutConnectionFailed:
            case InCallFailed:
            case CallFailedExt:
                return (processStateChange(from, ECallState.Error, report));
            case InCallDetected:
                return (processStateChange(from, ECallState.InDetected, report));
            case OutConnectionConnected:
                return (processStateChange(from, ECallState.OutConnected, report));
            case OutConnectionCanceledByLocalUser:
            case InCallCanceledByRemoteUser:
            case InCallRejectedByLocalUser:
            case OutCallCanceledByLocalUser:
            case OutCallRejectedByRemoteUser:
            case OutCallInvalidCoordinates:
            case CallEndedByRemoteUser:
            case CallEndedByLocalUser:
                return (processStateChange(from, ECallState.ReadyToWork, report));
            case OutCallAcceptedByRemoteUser:
            case InCallAcceptedByLocalUser:
                return (processStateChange(from, ECallState.Call, report));
            case OutConnectionStartedByLocalUser:
                return (processStateChange(from, ECallState.OutStarted, report));
            case UnconditionalTransition:
            case SysIntConnectedIncompatible:
            case CallFailedInt:
            default:
                return true;
        }
    }

    synchronized private boolean processReportAbnormal(ECallReport report, ECallState from) {
        if (debug) Timber.i("processReportAbnormal");
        switch (report) {
            case StopDebug:
                switch (opMode) {
                    case DataGen2Bt:
                    case AudIn2AudOut:
                    case Bt2AudOut:
                    case AudIn2Bt:
                    case Bt2Bt:
                        return (processStateChange(from, ECallState.PhaseZero, report));
                    case Record:
                        return (processStateChange(from, ECallState.DebugRecorded, report));
                    case Net2Net:
                    default:
                        return false;
                }
            case StartDebug:
                switch (opMode) {
                    case DataGen2Bt:
                    case AudIn2AudOut:
                    case Bt2AudOut:
                    case AudIn2Bt:
                    case Bt2Bt:
                        return (processStateChange(from, ECallState.DebugLoop, report));
                    case Record:
                        switch (from) {
                            case DebugRecorded:
                                return (processStateChange(from, ECallState.DebugPlay, report));
                            case PhaseZero:
                                return (processStateChange(from, ECallState.DebugRecord, report));
                            default:
                                return false;
                        }
                    case Net2Net:
                    default:
                        return false;
                }
            case UnconditionalTransition:
            default:
                return true;
        }
    }

    //--------------------- process state change

    synchronized private boolean processStateChange(ECallState from, ECallState to, ECallReport why) {
        return processStateChange(from, to, why, false);
    }

    synchronized private boolean processStateChange(ECallState from, ECallState to, ECallReport why, boolean isForce) {
        if (currState == from || isForce) {
            if (ECallState.availableFromAny().contains(to) || from.available().contains(to) || isForce) {
                prevReport = why;
                prevState = currState;
                currState = to;
                onStateChange(from, to, why);
                return true;
            } else if (debug) Timber.e("processStateChange: <%s> not available from <%s>", to, from);
        } else if (debug) Timber.e("processStateChange: current currState is <%s>, not <%s>", currState, from);
        return false;
    }

}
