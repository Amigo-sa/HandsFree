package by.citech.handsfree.debug.fsm;

import android.support.annotation.CallSuper;

import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.fsm.IFsmListener;
import by.citech.handsfree.fsm.IFsmReport;
import by.citech.handsfree.fsm.IFsmState;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.debug.fsm.EDebugReport.*;
import static by.citech.handsfree.debug.fsm.EDebugState.*;

public class DebugFsm extends FsmCore {

    private EOpMode opMode;

    //--------------------- singleton

    private static volatile DebugFsm instance = null;

    private DebugFsm() {
        super(Tags.DebugFsm);
        opMode = Settings.Common.opMode;
        currState = ST_TurnedOff;
        processReport(RP_TurningOn, getFsmCurrentState(), Tags.ConnectionFsm);

    }

    public static DebugFsm getInstance() {
        if (instance == null) {
            synchronized (DebugFsm.class) {
                if (instance == null) {instance = new DebugFsm();}}}
        return instance;
    }

    //--------------------- IDebugFsmReporter

    synchronized private boolean processReport(IFsmReport report, IFsmState from, String msg) {
        return checkFsmReport(report, from, msg) && processFsmReport(report, from);
    }

    //--------------------- processing

    @Override
    protected boolean processFsmReport(IFsmReport report, IFsmState from) {
        if (debug) Timber.i("processFsmReport");
        EDebugState fromCasted = (EDebugState) from;
        EDebugReport reportCasted = (EDebugReport) report;
        switch (reportCasted) {
            case RP_StopDebug:
                switch (opMode) {
                    case DataGen2Bt:
                    case AudIn2AudOut:
                    case Bt2AudOut:
                    case AudIn2Bt:
                    case Bt2Bt:
                        return processFsmStateChange(report, from, ST_TurnedOn);
                    case Record:
                        return processFsmStateChange(report, from, ST_DebugRecorded);
                    case Net2Net:
                    default:
                        return false;
                }
            case RP_StartDebug:
                switch (opMode) {
                    case DataGen2Bt:
                    case AudIn2AudOut:
                    case Bt2AudOut:
                    case AudIn2Bt:
                    case Bt2Bt:
                        return processFsmStateChange(report, from, ST_DebugLoop);
                    case Record:
                        switch (fromCasted) {
                            case ST_DebugRecorded:
                                return processFsmStateChange(report, from, ST_DebugPlay);
                            case ST_TurnedOn:
                                return processFsmStateChange(report, from, ST_DebugRecord);
                            default:
                                return false;
                        }
                    case Net2Net:
                    default:
                        return false;
                }
            default:
                return true;
        }
    }

    //--------------------- interfaces

    public interface IDebugFsmReporter {

        @CallSuper
        default IFsmState getDebugFsmState() {
            return getInstance().getFsmCurrentState();
        }

        @CallSuper
        default boolean reportToDebugFsm(IFsmReport whatHappened, IFsmState fromWhichState, String fromWho) {
            return getInstance().processReport(whatHappened, fromWhichState, fromWho);
        }

    }

    public interface IDebugFsmListenerRegister {

        @CallSuper
        default boolean registerDebugFsmListener(IDebugFsmListener listener, String who) {
            return getInstance().registerFsmListener(listener, who);
        }

        @CallSuper
        default boolean unregisterDebugFsmListener(IDebugFsmListener listener, String who) {
            return getInstance().unregisterFsmListener(listener, who);
        }

    }

    public interface IDebugFsmListener extends IFsmListener {}

}
