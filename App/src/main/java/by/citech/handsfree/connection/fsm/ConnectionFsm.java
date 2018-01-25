package by.citech.handsfree.connection.fsm;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.connection.fsm.EConnectionReport.TurningOn;
import static by.citech.handsfree.connection.fsm.EConnectionState.BtNotSupported;
import static by.citech.handsfree.connection.fsm.EConnectionState.BtPrepared;
import static by.citech.handsfree.connection.fsm.EConnectionState.Connected;
import static by.citech.handsfree.connection.fsm.EConnectionState.Connecting;
import static by.citech.handsfree.connection.fsm.EConnectionState.DeviceChosen;
import static by.citech.handsfree.connection.fsm.EConnectionState.DeviceNotChosen;
import static by.citech.handsfree.connection.fsm.EConnectionState.Disconnected;
import static by.citech.handsfree.connection.fsm.EConnectionState.Found;
import static by.citech.handsfree.connection.fsm.EConnectionState.GettingInitData;
import static by.citech.handsfree.connection.fsm.EConnectionState.GettingStatus;
import static by.citech.handsfree.connection.fsm.EConnectionState.GotInitData;
import static by.citech.handsfree.connection.fsm.EConnectionState.GotStatus;
import static by.citech.handsfree.connection.fsm.EConnectionState.Incompatible;
import static by.citech.handsfree.connection.fsm.EConnectionState.NotFound;
import static by.citech.handsfree.connection.fsm.EConnectionState.Searching;
import static by.citech.handsfree.connection.fsm.EConnectionState.TurnedOff;
import static by.citech.handsfree.connection.fsm.EConnectionState.TurnedOn;

public class ConnectionFsm {

    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private Collection<IConnectionFsmListener> listeners;
    private volatile EConnectionState currState, prevState;
    private volatile EConnectionReport prevReport;

    {
        listeners = new ConcurrentLinkedQueue<>();
        currState = EConnectionState.TurnedOff;
        processReport(TurningOn, currState, Tags.ConnectionFsm);
    }

    //--------------------- singleton

    private static volatile ConnectionFsm instance = null;

    private ConnectionFsm() {
    }

    public static ConnectionFsm getInstance() {
        if (instance == null) {
            synchronized (ConnectionFsm.class) {
                if (instance == null) {
                    instance = new ConnectionFsm();
                }
            }
        }
        return instance;
    }

    //--------------------- IConnectionFsmListenerRegister register and unregister

    synchronized boolean registerListener(IConnectionFsmListener listener, String who) {
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
        if (isAdded) listener.onConnectionFsmStateChange(prevState, currState, prevReport);
        return isAdded;
    }

    synchronized boolean unregisterListener(IConnectionFsmListener listener, String who) {
        boolean isRemoved;
        isRemoved = listeners.remove(listener);
        if (isRemoved) {if (debug) Timber.w("unregister success: <%s>, count: <%d>", who, listeners.size());}
        else           {if (debug) Timber.e("unregister fail: <%s>, count: still <%d>", who, listeners.size());}
        return isRemoved;
    }

    //--------------------- IConnectionFsmListener onConnectionFsmStateChange

    synchronized private void onStateChange(EConnectionState from, EConnectionState to, EConnectionReport why) {
        if (debug) Timber.w("onConnectionFsmStateChange: <%s> ==> <%s>, report: <%s>", from, to, why);
        for (IConnectionFsmListener listener : listeners) listener.onConnectionFsmStateChange(from, to, why);
    }

    //--------------------- IConnectionFsmReporter

    synchronized EConnectionState getState() {
        return currState;
    }

    synchronized boolean processReport(EConnectionReport report, EConnectionState from, String msg) {
        if (debug) Timber.w("processReport: report <%s> from <%s>, message: <%s>", report, from, msg);
        if (report == null || from == null || msg == null) {
            if (debug) Timber.e("processReport %s", StatusMessages.ERR_PARAMETERS);
            return false;
        }
        return processReportNormal(report, from);
    }

    //--------------------- processing

    private boolean processReportNormal(EConnectionReport report, EConnectionState from) {
        if (debug) Timber.i("processReportNormal");
        switch (report) {
            case TurningOn:
                return processStateChange(from, TurnedOn, report);
            case TurningOff:
                return processStateChange(from, TurnedOff, report);
            case BtNotSupported:
            case BtLeNotSupported:
                return processStateChange(from, BtNotSupported, report);
            case BtPrepared:
                return processStateChange(from, BtPrepared, report);
            case ChosenDevicePassedTheCheck:
                return processStateChange(from, DeviceChosen, report);
            case ChosenDeviceFailedTheCheck:
                return processStateChange(from, DeviceNotChosen, report);
            case SearchStarted:
                return processStateChange(from, Searching, report);
            case SearchStopped:
                return processStateChange(from, NotFound, report);
            case BtFound:
                return processStateChange(from, Found, report);
            case ConnectStarted:
                return processStateChange(from, Connecting, report);
            case BtConnectedIncompatible:
                return processStateChange(from, Incompatible, report);
            case GettingStatusStopped:
            case BtConnectedCompatible:
                return processStateChange(from, Connected, report);
            case BtDisconnected:
            case ConnectStopped:
                return processStateChange(from, Disconnected, report);
            case GettingInitDataStarted:
                return processStateChange(from, GettingInitData, report);
            case GotInitData:
                return processStateChange(from, GotInitData, report);
            case GettingStatusStarted:
                return processStateChange(from, GettingStatus, report);
            case GotStatus:
            case GettingInitDataStopped:
                return processStateChange(from, GotStatus, report);
            case UnconditionalTransition:
            case BtConnecting:
            case BtSearching:
            default:
                return true;
        }
    }

    synchronized private boolean processStateChange(EConnectionState from, EConnectionState to, EConnectionReport why) {
        return processStateChange(from, to, why, false);
    }

    private boolean processStateChange(EConnectionState from, EConnectionState to, EConnectionReport why, boolean isForce) {
        if (currState == from || isForce) {
            if (EConnectionState.availableFromAny().contains(to) || from.available().contains(to) || isForce) {
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
