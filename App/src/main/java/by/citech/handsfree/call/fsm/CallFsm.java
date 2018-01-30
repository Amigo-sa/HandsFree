package by.citech.handsfree.call.fsm;

import android.support.annotation.CallSuper;

import java.util.EnumMap;

import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.fsm.IFsmListener;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.call.fsm.ECallReport.*;
import static by.citech.handsfree.call.fsm.ECallState.*;

public class CallFsm extends FsmCore<ECallReport, ECallState> {

    //--------------------- singleton

    private static volatile CallFsm instance = null;

    private CallFsm() {
        super(Tags.CallFsm);
        reportToStateMap = new EnumMap<>(ECallReport.class);
        currState = ST_TurnedOff;
        processReport(RP_TurningOn, getFsmCurrentState(), Tags.CallFsm);
    }

    public static CallFsm getInstance() {
        if (instance == null) {
            synchronized (CallFsm.class) {
                if (instance == null) {instance = new CallFsm();}}}
        return instance;
    }

    //--------------------- ICallFsmReporter

    synchronized boolean processReport(ECallReport report, ECallState from, String msg) {
        return checkFsmReport(report, from, msg) && processFsmReport(report, from);
    }

    //--------------------- processing

    @Override
    protected boolean processFsmReport(ECallReport report, ECallState from) {
        if (debug) Timber.i("processFsmReport");
        switch (report) {
            case RP_BtError:
                switch (from) {
                    case ST_BtReady:
                        return processFsmStateChange(report, from, ST_TurnedOn);
                    default:
                        return processFsmStateChange(report, from, ST_NetReady);
                }
            case RP_BtReady:
                switch (from) {
                    case ST_TurnedOn:
                        return processFsmStateChange(report, from, ST_BtReady);
                    case ST_NetReady:
                        return processFsmStateChange(report, from, ST_Ready);
                    default:
                        return true;
                }
            case RP_NetError:
                switch (from) {
                    case ST_NetReady:
                        return processFsmStateChange(report, from, ST_TurnedOn);
                    default:
                        return processFsmStateChange(report, from, ST_BtReady);
                }
            case RP_NetReady:
                switch (from) {
                    case ST_TurnedOn:
                        return processFsmStateChange(report, from, ST_NetReady);
                    case ST_BtReady:
                        return processFsmStateChange(report, from, ST_Ready);
                    default:
                        return true;
                }
            default:
                return processFsmStateChange(report, from, report.getDestination());
        }
    }

    //--------------------- interfaces

    public interface ICallFsmReporter {
        @CallSuper
        default ECallState getCallFsmState() {
            return getInstance().getFsmCurrentState();
        }
        @CallSuper
        default boolean reportToCallFsm(ECallReport report, ECallState from, String message) {
            return getInstance().processReport(report, from, message);
        }
    }

    public interface ICallFsmListenerRegister {
        @CallSuper
        default boolean registerCallFsmListener(ICallFsmListener listener, String who) {
            return getInstance().registerFsmListener(listener, who);
        }
        @CallSuper
        default boolean unregisterCallFsmListener(ICallFsmListener listener, String who) {
            return getInstance().unregisterFsmListener(listener, who);
        }
    }

    public interface ICallFsmListener extends IFsmListener<ECallReport, ECallState> {}

}
