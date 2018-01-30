package by.citech.handsfree.bluetoothlegatt.fsm;

import android.support.annotation.CallSuper;

import java.util.EnumMap;

import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.fsm.IFsmListener;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.bluetoothlegatt.fsm.EBtReport.*;
import static by.citech.handsfree.bluetoothlegatt.fsm.EBtState.*;

public class BtFsm extends FsmCore<EBtReport, EBtState> {

    //--------------------- singleton

    private static volatile BtFsm instance = null;

    private BtFsm() {
        super(Tags.ConnectionFsm);
        reportToStateMap = new EnumMap<>(EBtReport.class);
        currState = ST_TurnedOff;
        processReport(ReportTurningOn, getFsmCurrentState(), Tags.ConnectionFsm);
    }

    public static BtFsm getInstance() {
        if (instance == null) {
            synchronized (BtFsm.class) {
                if (instance == null) {instance = new BtFsm();}}}
        return instance;
    }

    //--------------------- IConnectionFsmReporter

    synchronized boolean processReport(EBtReport report, EBtState from, String msg) {
        return checkFsmReport(report, from, msg) && processFsmReport(report, from);
    }

    //--------------------- processing

    @Override
    synchronized protected boolean processFsmReport(EBtReport why, EBtState from) {
        if (debug) Timber.i("processFsmReport");
        return processFsmStateChange(why, from, why.getDestination());
    }

    //--------------------- interfaces

    public interface IBtFsmReporter {

        @CallSuper
        default EBtState getBtFsmState() {
            return getInstance().getFsmCurrentState();
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

    public interface IBtFsmListener extends IFsmListener<EBtReport, EBtState> {}

}
