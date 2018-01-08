package by.citech.handsfree.logic;

import android.util.Log;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.management.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;
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
        if (isObjectPrepared()) return true;
        takeSettings();
        listeners = new ConcurrentLinkedQueue<>();
        state = PhaseZero;
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return state != null && listeners != null;
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
    public boolean baseCreate() {
        if (debug) Log.w(TAG, "baseCreate");
        IBase.super.baseCreate();
        prepareObject();
        return true;
    }

    @Override
    public boolean baseDestroy() {
        if (debug) Log.w(TAG, "baseDestroy");
        state = null;
        opMode = null;
        listeners.clear();
        IBase.super.baseDestroy();
        return true;
    }

    //--------------------- base

    synchronized boolean registerListener(ICallerFsmListener listener, String who) {
        boolean isAdded;
        if (listeners.contains(listener)) {
            if (debug) Log.w(TAG, "registerListener already contains listener " + who);
            isAdded = true;
        } else {
            isAdded = listeners.add(listener);
            if (isAdded) {
                if (debug) Log.w(TAG, String.format(Locale.US,
                        "registerListener added listener %s, listeners count is %d",
                        who, listeners.size()));
            } else {
                if (debug) Log.e(TAG, String.format(Locale.US,
                        "registerListener failed to add listener %s, listeners count still %d",
                        who, listeners.size()));
            }
        }
        return isAdded;
    }

    synchronized boolean unregisterListener(ICallerFsmListener listener, String who) {
        boolean isRemoved;
        if (!listeners.contains(listener)) {
            if (debug) Log.w(TAG, "unregisterListener not contains listener " + who);
            isRemoved = true;
        } else {
            isRemoved = listeners.remove(listener);
            if (isRemoved) {
                if (debug) Log.w(TAG, String.format(Locale.US,
                        "unregisterListener removed listener %s, listeners count is %d",
                        who, listeners.size()));
            } else {
                if (debug) Log.e(TAG, String.format(Locale.US,
                        "unregisterListener failed to remove listener %s, listeners count still %d",
                        who, listeners.size()));
            }
        }
        return isRemoved;
    }

    synchronized ECallerState getState() {
        if (!isObjectPrepared()) {
            if (debug) Log.w(TAG, "getState object not prepared");
            return null;
        }
        return state;
    }

    synchronized boolean processReport(ECallReport report, ECallerState from, String msg) {
        if (!isObjectPrepared()) {
            if (debug) Log.w(TAG, "processReport object not prepared");
            return false;
        }
        if (debug) Log.w(TAG, String.format(Locale.US,
                "processReport: report <%s>, from state <%s>, message is <%s>",
                report, from, msg));
        if (report == null || from == null || msg == null) {
            if (debug) Log.e(TAG, "processReport" + StatusMessages.ERR_PARAMETERS);
            return false;
        }
        if (opMode == Normal) {
            return processReportNormal(report, from);
        } else {
            return processReportAbnormal(report, from);
        }
    }

    //--------------------- main

    private boolean processReportNormal(ECallReport report, ECallerState from) {
        if (debug) Log.i(TAG, "processReportNormal");
        switch (report) {
//          case SysIntConnectedCompatible:
//          case SysIntDisconnected:
//          case SysIntConnected:
//          case SysIntFail:
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

    private boolean processReportAbnormal(ECallReport report, ECallerState from) {
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
                return false;
        }
    }

    private boolean processStateChange(ECallerState from, ECallerState to, ECallReport why) {
        if (state == from) {
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

    private void onStateChange(ECallerState from, ECallerState to, ECallReport why) {
        if (debug) Log.w(TAG, String.format(Locale.US,
                "onStateChange from state <%s> to state <%s>, reason is <%s>",
                from.getName(), to.getName(), why.name()));
        for (ICallerFsmListener listener : listeners) {
            if (listener != null) {
                listener.onCallerStateChange(from, to, why);
            } else {
                listeners.remove(listener);
            }
        }
    }

}
