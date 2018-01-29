package by.citech.handsfree.experimental.fsm;

import android.support.annotation.CallSuper;

import java.util.HashMap;

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

    //--------------------- preparation

    {
        map = new HashMap<>();

        map.put(ReportTurningOn,               StateTurnedOn);
        map.put(ReportTurningOff,              StateTurnedOff);
        map.put(ReportBtLeNotSupported,        StateBtPrepareFail);
        map.put(ReportBtNotSupported,          StateBtPrepareFail);
        map.put(ReportBtPrepared,              StateBtPrepared);
        map.put(ReportEnable,                  StateBtEnabling);
        map.put(ReportDisable,                 StateBtDisabled);
        map.put(ReportBtEnabled,               StateBtEnabled);
        map.put(ReportBtDisabled,              StateBtDisabled);
        map.put(ReportChosenValid,             StateDeviceChosen);
        map.put(ReportChosenInvalid,           StateDeviceNotChosen);
        map.put(ReportSearchStart,             StateSearching);
        map.put(ReportSearchStop,              StateDeviceChosen);
        map.put(ReportBtFound,                 StateFound);
        map.put(ReportConnect,                 StateConnecting);
        map.put(ReportDisconnect,              StateDisconnecting);
        map.put(ReportBtConnectedCompatible,   StateConnected);
        map.put(ReportBtConnectedIncompatible, StateIncompatible);
        map.put(ReportBtDisconnected,          StateDisconnected);
        map.put(ReportExchangeEnable,          StateExchangeEnabling);
        map.put(ReportBtExchangeEnabled,       StateExchangeEnabled);
        map.put(ReportExchangeDisable,         StateExchangeDisabling);
        map.put(ReportBtExchangeDisabled,      StateConnected);

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
