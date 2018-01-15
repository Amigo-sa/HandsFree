package by.citech.handsfree.logic;

import android.util.Log;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.management.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.settings.EOpMode;

import static by.citech.handsfree.logic.ECallerState.*;
import static by.citech.handsfree.logic.ECallReport.*;
import static by.citech.handsfree.settings.EOpMode.Normal;

public class CallerFsm
        implements ISettingsCtrl, IPrepareObject, IBase {

    private static final String STAG = Tags.CallerFsm;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private EOpMode opMode;
    private volatile Collection<ICallerFsmListener> listeners;
    private volatile ECallerState state;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        takeSettings();
        state = PhaseZero;
        listeners = new ConcurrentLinkedQueue<>();
        return true;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        opMode = Settings.getInstance().getCommon().getOpMode();
        return false;
    }

    //--------------------- singleton

    private static volatile CallerFsm instance = null;

    private CallerFsm() {
    }

    public static CallerFsm getInstance() {
        if (instance == null) {
            synchronized (CallerFsm.class) {
                if (instance == null) {
                    instance = new CallerFsm();
                }
            }
        }
        return instance;
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        if (debug) Log.i(TAG, "baseStart");
        IBase.super.baseStart();
        processStateChange(getState(), PhaseZero, TurningOn, true);
        return false;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        processStateChange(getState(), PhaseZero, TurningOff, true);
        IBase.super.baseStop();
        return false;
    }

    //--------------------- ICallerFsmRegisterListener

    boolean registerListener(ICallerFsmListener listener, String who) {
        boolean isAdded;
        if (listeners.contains(listener)) {
            if (debug) Log.w(TAG, "registerListener already contains " + who);
            isAdded = true;
        } else {
            isAdded = listeners.add(listener);
            if (isAdded) {
                if (debug) Log.w(TAG, String.format(Locale.US,
                        "registerListener added %s, count: %d",
                        who, listeners.size()));
            } else {
                if (debug) Log.e(TAG, String.format(Locale.US,
                        "registerListener failed to add %s, count: still %d",
                        who, listeners.size()));
            }
        }
        return isAdded;
    }

    boolean unregisterListener(ICallerFsmListener listener, String who) {
        boolean isRemoved;
        if (!listeners.contains(listener)) {
            if (debug) Log.w(TAG, "unregisterListener no such listener: " + who);
            isRemoved = true;
        } else {
            isRemoved = listeners.remove(listener);
            if (isRemoved) {
                if (debug) Log.w(TAG, String.format(Locale.US,
                        "unregisterListener removed %s, count: %d",
                        who, listeners.size()));
            } else {
                if (debug) Log.e(TAG, String.format(Locale.US,
                        "unregisterListener failed to remove %s, count: still %d",
                        who, listeners.size()));
            }
        }
        return isRemoved;
    }

    //--------------------- ICallerFsm

    synchronized ECallerState getState() {
        return state;
    }

    synchronized boolean processReport(ECallReport report, ECallerState from, String msg) {
        if (debug) Log.w(TAG, String.format(Locale.US,
                "processReport: report <%s>, from state <%s>, message is <%s>",
                report, from, msg));
        if (report == null || from == null || msg == null) {
            if (debug) Log.e(TAG, "processReport" + StatusMessages.ERR_PARAMETERS);
            return false;
        } else if (opMode == Normal) {
            return processReportNormal(report, from);
        } else {
            return processReportAbnormal(report, from);
        }
    }

    //--------------------- main

    synchronized private boolean processReportNormal(ECallReport report, ECallerState from) {
        if (debug) Log.i(TAG, "processReportNormal");
        switch (report) {
            case SysIntError:
                switch (from) {
                    case PhaseReadyInt:
                        return (processStateChange(from, PhaseZero, report));
                    default:
                        return (processStateChange(from, PhaseReadyExt, report));
                }
            case SysIntReady:
                switch (from) {
                    case PhaseZero:
                        return (processStateChange(from, PhaseReadyInt, report));
                    case PhaseReadyExt:
                        return (processStateChange(from, ReadyToWork, report));
                    default:
                        return true;
                }
            case SysExtError:
                switch (from) {
                    case PhaseReadyExt:
                        return (processStateChange(from, PhaseZero, report));
                    default:
                        return (processStateChange(from, PhaseReadyInt, report));
                }
            case SysExtReady:
                switch (from) {
                    case PhaseZero:
                        return (processStateChange(from, PhaseReadyExt, report));
                    case PhaseReadyInt:
                        return (processStateChange(from, ReadyToWork, report));
                    default:
                        return true;
                }
            case OutConnectionFailed:
            case InCallFailed:
            case CallFailedExt:
                return (processStateChange(from, Error, report));
            case InCallDetected:
                return (processStateChange(from, InDetected, report));
            case OutConnectionConnected:
                return (processStateChange(from, OutConnected, report));
            case OutConnectionCanceledByLocalUser:
            case InCallCanceledByRemoteUser:
            case InCallRejectedByLocalUser:
            case OutCallCanceledByLocalUser:
            case OutCallRejectedByRemoteUser:
            case OutCallInvalidCoordinates:
            case CallEndedByRemoteUser:
            case CallEndedByLocalUser:
                return (processStateChange(from, ReadyToWork, report));
            case OutCallAcceptedByRemoteUser:
            case InCallAcceptedByLocalUser:
                return (processStateChange(from, Call, report));
            case OutConnectionStartedByLocalUser:
                return (processStateChange(from, OutStarted, report));
            case UnconditionalTransition:
            case SysIntConnectedIncompatible:
            case CallFailedInt:
            default:
                return true;
        }
    }

    synchronized private boolean processReportAbnormal(ECallReport report, ECallerState from) {
        if (debug) Log.i(TAG, "processReportAbnormal");
        switch (report) {
            case StopDebug:
                switch (opMode) {
                    case DataGen2Bt:
                    case AudIn2AudOut:
                    case Bt2AudOut:
                    case AudIn2Bt:
                    case Bt2Bt:
                        return (processStateChange(from, PhaseZero, report));
                    case Record:
                        return (processStateChange(from, DebugRecorded, report));
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
                        return (processStateChange(from, DebugLoop, report));
                    case Record:
                        switch (from) {
                            case DebugRecorded:
                                return (processStateChange(from, DebugPlay, report));
                            case PhaseZero:
                                return (processStateChange(from, DebugRecord, report));
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

    synchronized private boolean processStateChange(ECallerState from, ECallerState to, ECallReport why) {
        return processStateChange(from, to, why, false);
    }

    synchronized private boolean processStateChange(ECallerState from, ECallerState to, ECallReport why, boolean isForce) {
        if (isForce) {
            state = to;
            onStateChange(from, to, why);
            return true;
        } else if (state == from) {
            if (from.availableStates().contains(to)) {
                state = to;
                onStateChange(from, to, why);
                switch (state) {
                    case Error:
                        state = ReadyToWork;
                        onStateChange(Error, ReadyToWork, UnconditionalTransition);
                        break;
                    case Failure:
                        state = PhaseZero;
                        onStateChange(Failure, PhaseZero, UnconditionalTransition);
                        break;
                    default:
                        break;
                }
                return true;
            } else {
                if (debug) Log.e(TAG, String.format(Locale.US,
                        "processStateChange: state <%s> is not available from state <%s>",
                        to, from));
            }
        } else {
            if (debug) Log.e(TAG, String.format(Locale.US,
                    "processStateChange: current state is <%s>, not <%s>",
                    state, from));
        }
        return false;
    }

    synchronized private void onStateChange(ECallerState from, ECallerState to, ECallReport why) {
        if (debug) Log.w(TAG, String.format(Locale.US,
                "onStateChange from state <%s> to state <%s>, reason is <%s>",
                from, to, why));
        for (ICallerFsmListener listener : listeners) {
            if (listener != null) {
                listener.onCallerStateChange(from, to, why);
            } else {
                listeners.remove(listener);
            }
        }
    }

}
