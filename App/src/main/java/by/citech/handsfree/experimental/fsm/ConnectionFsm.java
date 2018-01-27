package by.citech.handsfree.experimental.fsm;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.experimental.fsm.EConnectionReport.TurningOn;
import static by.citech.handsfree.experimental.fsm.EConnectionState.BtNotSupported;
import static by.citech.handsfree.experimental.fsm.EConnectionState.BtPrepared;
import static by.citech.handsfree.experimental.fsm.EConnectionState.Connected;
import static by.citech.handsfree.experimental.fsm.EConnectionState.Connecting;
import static by.citech.handsfree.experimental.fsm.EConnectionState.DeviceChosen;
import static by.citech.handsfree.experimental.fsm.EConnectionState.DeviceNotChosen;
import static by.citech.handsfree.experimental.fsm.EConnectionState.Disconnected;
import static by.citech.handsfree.experimental.fsm.EConnectionState.Found;
import static by.citech.handsfree.experimental.fsm.EConnectionState.GettingInitData;
import static by.citech.handsfree.experimental.fsm.EConnectionState.GettingStatus;
import static by.citech.handsfree.experimental.fsm.EConnectionState.GotInitData;
import static by.citech.handsfree.experimental.fsm.EConnectionState.GotStatus;
import static by.citech.handsfree.experimental.fsm.EConnectionState.Incompatible;
import static by.citech.handsfree.experimental.fsm.EConnectionState.NotFound;
import static by.citech.handsfree.experimental.fsm.EConnectionState.Searching;
import static by.citech.handsfree.experimental.fsm.EConnectionState.TurnedOff;
import static by.citech.handsfree.experimental.fsm.EConnectionState.TurnedOn;

public class ConnectionFsm
        extends FsmCore<IConnectionFsmListener, EConnectionState, EConnectionReport> {

    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private FsmCore<IConnectionFsmListener, EConnectionState, EConnectionReport> core;
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
        super("ConnectionFsm");
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

    //--------------------- registerConnectionFsmListener and unregisterConnectionFsmListener

    synchronized boolean registerConnectionFsmListener(IConnectionFsmListener listener, String who) {
        return super.registerFsmListener(listener, who);
    }

    synchronized boolean unregisterConnectionFsmListener(IConnectionFsmListener listener, String who) {
        return super.unregisterFsmListener(listener, who);
    }

    //--------------------- IConnectionFsmListener onConnectionFsmStateChange

    synchronized private void onChange(EConnectionState from, EConnectionState to, EConnectionReport why) {
        super.onFsmStateChange(from, to, why);
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
                return super.processFsmStateTransition(from, TurnedOn, report);
            case TurningOff:
                return super.processFsmStateTransition(from, TurnedOff, report);
            case BtNotSupported:
            case BtLeNotSupported:
                return super.processFsmStateTransition(from, BtNotSupported, report);
            case BtPrepared:
                return super.processFsmStateTransition(from, BtPrepared, report);
            case ChosenDevicePassedTheCheck:
                return super.processFsmStateTransition(from, DeviceChosen, report);
            case ChosenDeviceFailedTheCheck:
                return super.processFsmStateTransition(from, DeviceNotChosen, report);
            case SearchStarted:
                return super.processFsmStateTransition(from, Searching, report);
            case SearchStopped:
                return super.processFsmStateTransition(from, NotFound, report);
            case BtFound:
                return super.processFsmStateTransition(from, Found, report);
            case ConnectStarted:
                return processFsmStateTransition(from, Connecting, report);
            case BtConnectedIncompatible:
                return super.processFsmStateTransition(from, Incompatible, report);
            case GettingStatusStopped:
            case BtConnectedCompatible:
                return super.processFsmStateTransition(from, Connected, report);
            case BtDisconnected:
            case ConnectStopped:
                return super.processFsmStateTransition(from, Disconnected, report);
            case GettingInitDataStarted:
                return super.processFsmStateTransition(from, GettingInitData, report);
            case GotInitData:
                return super.processFsmStateTransition(from, GotInitData, report);
            case GettingStatusStarted:
                return super.processFsmStateTransition(from, GettingStatus, report);
            case GotStatus:
            case GettingInitDataStopped:
                return super.processFsmStateTransition(from, GotStatus, report);
            case UnconditionalTransition:
            case BtConnecting:
            case BtSearching:
            default:
                return true;
        }
    }

}
