package by.citech.handsfree.debug.fsm;

import android.support.annotation.CallSuper;

import java.util.EnumMap;

import by.citech.handsfree.application.ThisApp;
import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.fsm.IFsmListener;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.debug.fsm.EDebugReport.*;
import static by.citech.handsfree.debug.fsm.EDebugState.*;

public class DebugFsm extends FsmCore<EDebugReport, EDebugState> {

    private EOpMode opMode;

    //--------------------- singleton

    private static volatile DebugFsm instance = null;

    private DebugFsm() {
        super(Tags.DebugFsm);
        opMode = Settings.Common.opMode;
        reportToStateMap = new EnumMap<>(EDebugReport.class);
        currState = ST_TurnedOff;
        processReport(RP_TurningOn, getFsmCurrentState(), Tags.DebugFsm);
    }

    public static DebugFsm getInstance() {
        if (instance == null) {
            synchronized (DebugFsm.class) {
                if (instance == null) {instance = new DebugFsm();}}}
        return instance;
    }

    //--------------------- IDebugFsmReporter

    synchronized private boolean processReport(EDebugReport report, EDebugState from, String msg) {
        return checkFsmReport(report, from, msg) && processFsmReport(report, from);
    }

    //--------------------- processing

    @Override
    protected boolean processFsmReport(EDebugReport report, EDebugState from) {
        switch (report) {
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
                        switch (from) {
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
                return processFsmStateChange(report, from, report.getDestination());
        }
    }

    //--------------------- interfaces

    public interface IDebugFsmReporter {
        @CallSuper
        default EDebugState getDebugFsmState() {
            return getInstance().getFsmCurrentState();
        }
        @CallSuper
        default boolean reportToDebugFsm(EDebugReport report, EDebugState from, String message) {
            return getInstance().processReport(report, from, message);
        }
    }

    public interface IDebugFsmListenerRegister {
        @CallSuper
        default boolean registerDebugFsmListener(IDebugFsmListener listener, String message) {
            return getInstance().registerFsmListener(listener, message);
        }
        @CallSuper
        default boolean unregisterDebugFsmListener(IDebugFsmListener listener, String message) {
            return getInstance().unregisterFsmListener(listener, message);
        }
    }

    public interface IDebugFsmListener extends IFsmListener<EDebugReport, EDebugState> {}

}
