package by.citech.handsfree.logic;

import android.util.Log;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.settings.enumeration.OpMode;

import static by.citech.handsfree.logic.CallerState.*;
import static by.citech.handsfree.logic.ECallReport.UnconditionalTransition;

public class CallerFsm {

    private static final String STAG = Tags.CallerFsm;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private OpMode opMode;
    private Collection<ICallerFsmListener> listeners;
    private volatile CallerState state;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        listeners = new ConcurrentLinkedQueue<>();
        state = Null;
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

    //--------------------- getters and setters

    public boolean addListener(ICallerFsmListener listener) {
        return listeners.add(listener);
    }

    //--------------------- fsm

    private void onStateChange(CallerState from, CallerState to, ECallReport why) {
        for (ICallerFsmListener listener : listeners) {
            if (listener != null) {
                listener.onCallerStateChange(from, to, why);
            } else {
                listeners.remove(listener);
            }
        }
    }

    synchronized CallerState getState() {
        if (debug) Log.i(TAG, "getCallerState is " + state.getName());
        return state;
    }

    private synchronized boolean setState(CallerState from, CallerState to, ECallReport why) {
        if (debug) Log.w(TAG, String.format(Locale.US,
                "setState from %s to %s",
                from.getName(), to.getName()));
        if (state == from) {
            if (from.availableStates().contains(to)) {
                state = to;
                onStateChange(from, to, why);
                switch (state) {
                    case Error:
                        state = Idle;
                        onStateChange(Error, Idle, UnconditionalTransition);
                        break;
                    case GeneralFailure:
                        state = Null;
                        onStateChange(GeneralFailure, Null, UnconditionalTransition);
                        break;
                    default:
                        break;
                }
                return true;
            } else {
                if (debug) Log.e(TAG, String.format(Locale.US,
                        "setState: state <%s> is not available from state <%s>",
                        to.getName(), from.getName()));
            }
        } else {
            if (debug) Log.e(TAG, String.format(Locale.US,
                    "setState: current state is not <%s>, current state is <%s>",
                    from.getName(), state.getName()));
        }
        return false;
    }

    synchronized boolean processReport(ECallReport report, CallerState from, String who) {
        if (report == null || from == null || who == null) {
            return false;
        }
        switch (report) {
            case InternalConnectorFail://TODO: bluetooth failed
            case ExternalConnectorFail://connectorFailure  TODO: network failed
                return (setState(from, GeneralFailure, report));
            case InternalConnectorReady://TODO: bluetooth ready
            case ExternalConnectorReady://connectorReady TODO: network ready
                switch(from) {
                    case Null:
                        return (setState(from, PreparationPhase1, report));
                    case PreparationPhase1:
                        return (setState(from, PreparationPhase2, report));
                    case PreparationPhase2:
                        return (setState(from, Idle, report));
                }
            case CallFailedExternal://callFailed TODO: выключение BT
            case CallFailedInternal://callFailed TODO: выключение BT
            case InCallFailed://callIncomingFailed
            case OutConnectionFailed://callOutcomingFailed
                return (setState(from, Error, report));
            case InCallDetected://callIncomingDetected
                return (setState(from, InDetected, report));
            case OutConnectionConnected://callOutcomingConnected
                return (setState(from, OutConnected, report));
            case InCallCanceledByRemoteUser://callIncomingCanceled
            case OutCallInvalidCoordinates://callOutcomingInvalid
            case CallEndedByRemoteUser://callEndedExternally TODO: выключение BT
            case OutCallRejectedByRemoteUser://callOutcomingRejected
            case CallEndedByLocalUser://callEndedInternally TODO: выключение BT
            case InCallRejectedByLocalUser://callIncomingRejected
            case OutConnectionCanceledByLocalUser://callOutcomingCanceled
            case OutCallCanceledByLocalUser://callOutcomingCanceled
                return (setState(from, Idle, report));
            case OutCallAcceptedByRemoteUser://callOutcomingAccepted TODO: включение BT
            case InCallAcceptedByLocalUser://callIncomingAccepted TODO: включение BT
                return (setState(from, Call, report));
            case OutConnectionStartedByLocalUser://callOutcomingStarted
                return (setState(from, OutStarted, report));
            case StopDebug://stopDebug
                switch (opMode) {
                    case Record:
                        return (setState(from, DebugRecorded, report));
                    case AudIn2AudOut:
                    case Bt2AudOut:
                    case AudIn2Bt:
                    case Bt2Bt:
                        return (setState(from, Null, report));
                    case Net2Net:
                    case Normal:
                    default:
                        return false;
                }
            case StartDebug://startDebug
                switch (opMode) {
                    case Record:
                        switch (from) {
                            case DebugRecorded:
                                return (setState(from, DebugPlay, report));
                            case Null:
                                return (setState(from, DebugRecord, report));
                            default:
                                return false;
                        }
                    case AudIn2AudOut:
                    case Bt2AudOut:
                    case AudIn2Bt:
                    case Bt2Bt:
                        return (setState(from, DebugLoopBack, report));
                    case Net2Net:
                    case Normal:
                    default:
                        return false;
                }
            case UnconditionalTransition:
            default:
                return false;
        }
    }

}
