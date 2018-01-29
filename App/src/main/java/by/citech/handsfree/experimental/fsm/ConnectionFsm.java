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
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtDisabled;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtDisconnected;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtEnabled;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtFound;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtLeNotSupported;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtNotSupported;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtExchangeDisabled;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtExchangeEnabled;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportBtPrepared;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportChosenInvalid;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportChosenValid;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportConnect;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportDisable;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportDisconnect;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportEnable;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportExchangeDisable;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportExchangeEnable;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportSearchStart;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportSearchStop;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportTurningOff;
import static by.citech.handsfree.experimental.fsm.EConnectionReport.ReportTurningOn;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateBtDisabled;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateBtEnabled;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateBtEnabling;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateBtPrepareFail;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateBtPrepared;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateConnected;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateConnecting;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateDeviceChosen;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateDeviceNotChosen;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateDisconnected;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateDisconnecting;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateExchangeDisabling;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateExchangeEnabled;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateExchangeEnabling;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateFound;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateIncompatible;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateSearching;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateTurnedOff;
import static by.citech.handsfree.experimental.fsm.EConnectionState.StateTurnedOn;

public class ConnectionFsm extends FsmCore {

    //--------------------- singleton

    private static volatile ConnectionFsm instance = null;

    private ConnectionFsm() {
        super(Tags.ConnectionFsm);

        toMap(ReportTurningOn,               StateTurnedOn);
        toMap(ReportTurningOff,              StateTurnedOff);
        toMap(ReportBtLeNotSupported,        StateBtPrepareFail);
        toMap(ReportBtNotSupported,          StateBtPrepareFail);
        toMap(ReportBtPrepared,              StateBtPrepared);
        toMap(ReportEnable,                  StateBtEnabling);
        toMap(ReportDisable,                 StateBtDisabled);
        toMap(ReportBtEnabled,               StateBtEnabled);
        toMap(ReportBtDisabled,              StateBtDisabled);
        toMap(ReportChosenValid,             StateDeviceChosen);
        toMap(ReportChosenInvalid,           StateDeviceNotChosen);
        toMap(ReportSearchStart,             StateSearching);
        toMap(ReportSearchStop,              StateDeviceChosen);
        toMap(ReportBtFound,                 StateFound);
        toMap(ReportConnect,                 StateConnecting);
        toMap(ReportDisconnect,              StateDisconnecting);
        toMap(ReportBtConnectedCompatible,   StateConnected);
        toMap(ReportBtConnectedIncompatible, StateIncompatible);
        toMap(ReportBtDisconnected,          StateDisconnected);
        toMap(ReportExchangeEnable,          StateExchangeEnabling);
        toMap(ReportBtExchangeEnabled,       StateExchangeEnabled);
        toMap(ReportExchangeDisable,         StateExchangeDisabling);
        toMap(ReportBtExchangeDisabled,      StateConnected);

        currState = StateTurnedOff;
        processReport(ReportTurningOn, StateTurnedOff, Tags.ConnectionFsm);
    }

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
    synchronized protected boolean implementedProcessFsmReport(IFsmReport why, IFsmState from) {
        if (debug) Timber.i("implementedProcessFsmReport");
        return processFsmStateChange(why, from, changeMap.get(why));
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
