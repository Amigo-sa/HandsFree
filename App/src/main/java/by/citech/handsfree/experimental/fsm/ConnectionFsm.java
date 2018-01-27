package by.citech.handsfree.experimental.fsm;

import android.support.annotation.CallSuper;

import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.fsm.IFsmListener;
import by.citech.handsfree.fsm.IFsmReport;
import by.citech.handsfree.fsm.IFsmState;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtConnectedCompatible;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtConnectedIncompatible;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtConnecting;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtDisabled;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtDisabling;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtDisconnected;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtDisconnecting;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtEnabled;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtEnabling;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtFound;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtLeNotSupported;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtDeviceSearching;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtNotSupported;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtNotificationDisabled;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtNotificationDisabling;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtNotificationEnabled;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtNotificationEnabling;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtPrepared;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportChosenInvalid;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportChosenValid;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportConnectStart;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportConnectStop;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportDisableStart;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportDisableStop;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportDisconnectStop;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportEnableStart;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportEnableStop;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportNotificationDisableStart;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportNotificationDisableStop;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportNotificationEnableStart;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportNotificationEnableStop;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportSearchStart;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportSearchStop;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportTurningOff;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportTurningOn;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportUnconditional;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateBtNotSupported;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateBtPrepared;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateConnected;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateConnecting;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateDeviceChosen;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateDeviceNotChosen;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateDisconnected;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateFound;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateIncompatible;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateNotFound;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateSearching;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateTurnedOff;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateTurnedOn;

public class ConnectionFsm extends FsmCore {

    //--------------------- preparation

    {
        currState = StateTurnedOff;
        processReport(ReportTurningOn, StateTurnedOff, Tags.ConnectionFsm);
    }

    //--------------------- singleton

    private static volatile ConnectionFsm instance = null;

    private ConnectionFsm() {super(Tags.ConnectionFsm);}

    public static ConnectionFsm getInstance() {
        if (instance == null) {
            synchronized (ConnectionFsm.class) {
                if (instance == null) {instance = new ConnectionFsm();}}}
        return instance;
    }

    //--------------------- IConnectionFsmReporter

    synchronized boolean processReport(EConnectionReport report, EConnectionState from, String msg) {
        return processFsmReport(report, from, msg) && implementedProcessFsmReport(report, from);
    }

    //--------------------- processing

    @Override
    synchronized protected boolean implementedProcessFsmReport(IFsmReport report, IFsmState from) {
        if (debug) Timber.i("processConnectionReport");

        if (report == ReportTurningOn)
            return processFsmStateChange(report, from, StateTurnedOn);
        if (report == ReportTurningOff)
            return processFsmStateChange(report, from, StateTurnedOff);

        if (report == ReportBtNotSupported
                || report == ReportBtLeNotSupported)
            return processFsmStateChange(report, from, StateBtNotSupported);
        if (report == ReportBtPrepared)
            return processFsmStateChange(report, from, StateBtPrepared);

        if (report == ReportChosenValid)
            return processFsmStateChange(report, from, StateDeviceChosen);
        if (report == ReportChosenInvalid)
            return processFsmStateChange(report, from, StateDeviceNotChosen);

        if (report == ReportSearchStart)
            return processFsmStateChange(report, from, StateSearching);
        if (report == ReportSearchStop)
            return processFsmStateChange(report, from, StateNotFound);
        if (report == ReportBtFound)
            return processFsmStateChange(report, from, StateFound);

        if (report == ReportConnectStart)
            return processFsmStateChange(report, from, StateConnecting);
        if (report == ReportBtConnectedIncompatible)
            return processFsmStateChange(report, from, StateIncompatible);
        if (report == ReportBtConnectedCompatible)
            return processFsmStateChange(report, from, StateConnected);

        if (report == ReportBtDisconnected
                || report == ReportConnectStop)
            return processFsmStateChange(report, from, StateDisconnected);

        if (report == ReportUnconditional
                || report == ReportBtConnecting
                || report == ReportBtDeviceSearching

                || report == ReportEnableStart
                || report == ReportBtEnabling
                || report == ReportBtEnabled
                || report == ReportEnableStop

                || report == ReportDisableStart
                || report == ReportBtDisabling
                || report == ReportBtDisabled
                || report == ReportDisableStop

                || report == ReportBtDisconnecting
                || report == ReportDisconnectStop

                || report == ReportNotificationEnableStart
                || report == ReportBtNotificationEnabling
                || report == ReportBtNotificationEnabled
                || report == ReportNotificationEnableStop

                || report == ReportNotificationDisableStart
                || report == ReportBtNotificationDisabling
                || report == ReportBtNotificationDisabled
                || report == ReportNotificationDisableStop
                )
            return true;
        else
            return true;
    }

    //--------------------- interfaces

    public interface IConnectionFsmReporter {

        @CallSuper
        default EConnectionState getConnectionFsmState() {
            return (EConnectionState) ConnectionFsm.getInstance().getFsmCurrentState();
        }

        @CallSuper
        default boolean reportToConnectionFsm(EConnectionState fromWhichState, EConnectionReport whatHappened, String fromWho) {
            return ConnectionFsm.getInstance().processReport(whatHappened, fromWhichState, fromWho);
        }

    }

    public interface IConnectionFsmListenerRegister {

        @CallSuper
        default boolean registerConnectionFsmListener(IConnectionFsmListener listener, String who) {
            return ConnectionFsm.getInstance().registerFsmListener(listener, who);
        }

        @CallSuper
        default boolean unregisterConnectionFsmListener(IConnectionFsmListener listener, String who) {
            return ConnectionFsm.getInstance().unregisterFsmListener(listener, who);
        }

    }

    public interface IConnectionFsmListener extends IFsmListener {}

}
