package by.citech.handsfree.bluetoothlegatt.fsm;

import android.support.annotation.CallSuper;

import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.fsm.IFsmListener;
import by.citech.handsfree.fsm.IFsmReport;
import by.citech.handsfree.fsm.IFsmState;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.bluetoothlegatt.fsm.EBtReport.*;
import static by.citech.handsfree.bluetoothlegatt.fsm.EBtState.*;

public class BtFsm extends FsmCore {

    //--------------------- singleton

    private static volatile BtFsm instance = null;

    private BtFsm() {
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
        processReport(ReportTurningOn, getFsmCurrentState(), Tags.ConnectionFsm);
    }

    public static BtFsm getInstance() {
        if (instance == null) {
            synchronized (BtFsm.class) {
                if (instance == null) {instance = new BtFsm();}}}
        return instance;
    }

    //--------------------- IConnectionFsmReporter

    synchronized boolean processReport(IFsmReport report, IFsmState from, String msg) {
        return checkFsmReport(report, from, msg) && processFsmReport(report, from);
    }

    //--------------------- processing

    @Override
    synchronized protected boolean processFsmReport(IFsmReport why, IFsmState from) {
        if (debug) Timber.i("processFsmReport");
        return processFsmStateChange(why, from, fromMap(why));
    }

    //--------------------- interfaces

    public interface IBtFsmReporter {

        @CallSuper
        default EBtState getBtFsmState() {
            return (EBtState) getInstance().getFsmCurrentState();
        }

        @CallSuper
        default boolean reportToBtFsm(EBtReport whatHappened, EBtState fromWhichState, String fromWho) {
            return getInstance().processReport(whatHappened, fromWhichState, fromWho);
        }

    }

    public interface IBtFsmListenerRegister {

        @CallSuper
        default boolean registerBtFsmListener(IBtFsmListener listener, String who) {
            return getInstance().registerFsmListener(listener, who);
        }

        @CallSuper
        default boolean unregisterBtFsmListener(IBtFsmListener listener, String who) {
            return getInstance().unregisterFsmListener(listener, who);
        }

    }

    public interface IBtFsmListener extends IFsmListener {}

}
