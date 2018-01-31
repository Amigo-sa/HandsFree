package by.citech.handsfree.network.fsm;

import android.support.annotation.CallSuper;

import java.util.EnumMap;

import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.fsm.IFsmListener;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.network.fsm.ENetReport.*;
import static by.citech.handsfree.network.fsm.ENetState.*;

public class NetFsm extends FsmCore<ENetReport, ENetState> {

    //--------------------- singleton

    private static volatile NetFsm instance = null;

    private NetFsm() {
        super(Tags.NetFsm);
        reportToStateMap = new EnumMap<>(ENetReport.class);
        currState = ST_TurnedOff;
        processReport(RP_TurningOn, getFsmCurrentState(), Tags.NetFsm);
    }

    public static NetFsm getInstance() {
        if (instance == null) {
            synchronized (NetFsm.class) {
                if (instance == null) {instance = new NetFsm();}}}
        return instance;
    }

    //--------------------- INetFsmReporter

    synchronized boolean processReport(ENetReport report, ENetState from, String msg) {
        return checkFsmReport(report, from, msg) && processFsmReport(report, from);
    }

    //--------------------- processing

    @Override
    protected boolean processFsmReport(ENetReport report, ENetState from) {
        if (debug) Timber.i("processFsmReport");
        return processFsmStateChange(report, from, report.getDestination());
    }

    //--------------------- interfaces

    public interface INetFsmReporter {
        @CallSuper
        default ENetState getNetFsmState() {
            return getInstance().getFsmCurrentState();
        }
        @CallSuper
        default boolean reportToBtFsm(ENetReport report, ENetState from, String message) {
            return getInstance().processReport(report, from, message);
        }
    }

    public interface INetFsmListenerRegister {
        @CallSuper
        default boolean registerNetFsmListener(INetFsmListener listener, String message) {
            return getInstance().registerFsmListener(listener, message);
        }
        @CallSuper
        default boolean unregisterNetFsmListener(INetFsmListener listener, String message) {
            return getInstance().unregisterFsmListener(listener, message);
        }
    }

    public interface INetFsmListener extends IFsmListener<ENetReport, ENetState> {}

}
