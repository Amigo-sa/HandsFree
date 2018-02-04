package by.citech.handsfree.input;

import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.debug.fsm.DebugFsm;
import by.citech.handsfree.debug.fsm.EDebugReport;
import by.citech.handsfree.debug.fsm.EDebugState;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.call.fsm.ECallReport.*;
import static by.citech.handsfree.debug.fsm.EDebugReport.*;
import static by.citech.handsfree.settings.EOpMode.Normal;

public class TwoInputController implements
        CallFsm.ICallFsmReporter,
        DebugFsm.IDebugFsmReporter {

    private static final boolean debug = Settings.debug;
    private final String TAG = Tags.CallUi;

    private EOpMode opMode;

    //--------------------- singleton

    private static volatile TwoInputController instance = null;

    private TwoInputController() {
        opMode = Settings.Common.opMode;
    }

    public static TwoInputController getInstance() {
        if (instance == null) {
            synchronized (TwoInputController.class) {
                if (instance == null) {instance = new TwoInputController();}}}
        return instance;
    }

    //--------------------- btnGreen

    private void onInput1() {
        if (opMode == Normal) {
            onInput1Call();
        } else {
            onInput1Debug();
        }
    }

    private void onInput1Call() {
        ECallState state = getCallFsmState();
        if (debug) Timber.w("onInput1Call opMode is %s, state is %s", opMode, state);
        switch (state) {
            case ST_Ready:
                toCall(RP_OutStartedLocal);
                break;
            case ST_InConnected:
                toCall(RP_InAcceptedLocal);
                break;
            default:
                break;
        }
    }

    private void onInput1Debug() {
        EDebugState debugState = getDebugFsmState();
        if (debug) Timber.w("onInput1Debug opMode is %s, debugState is %s", opMode, debugState);
        switch (opMode) {
            case DataGen2Bt:
            case AudIn2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (debugState) {
                    case ST_TurnedOn:
                        toDebug(RP_StartDebug);
                        break;
                    default:
                        break;
                }
                break;
            case Record:
                switch (debugState) {
                    case ST_DebugRecorded:
                    case ST_TurnedOn:
                        toDebug(RP_StartDebug);
                        break;
                    default:
                        break;
                }
                break;
            case Net2Net:
            default:
                break;
        }
    }

    //--------------------- btnRed

    private void onInput2() {
        if (opMode == Normal) {
            onInput2Call();
        } else {
            onInput2Debug();
        }
    }

    private void onInput2Call() {
        ECallState state = getCallFsmState();
        if (debug) Timber.w("onInput2Call opMode is %s, state is %s", opMode, state);
        switch (state) {
            case ST_Call:
                toCall(RP_CallEndedLocal);
                break;
            case ST_OutStarted:
            case ST_OutConnected:
                toCall(RP_OutCanceledLocal);
                break;
            case ST_InConnected:
                toCall(RP_InRejectedLocal);
                break;
            default:
                break;
        }
    }

    private void onInput2Debug() {
        EDebugState state = getDebugFsmState();
        if (debug) Timber.w("onInput2Debug opMode is %s, state is %s", opMode, state);
        switch (opMode) {
            case AudIn2Bt:
            case DataGen2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (state) {
                    case ST_DebugLoop:
                        toDebug(RP_StopDebug);
                        break;
                    default:
                        break;
                }
                break;
            case Record:
                switch (state) {
                    case ST_DebugPlay:
                    case ST_DebugRecord:
                        toDebug(RP_StopDebug);
                        break;
                    default:
                        break;
                }
                break;
            case Net2Net:
            default:
                break;
        }
    }

    //--------------------- report

    private void toDebug(EDebugReport report) {
        reportToDebugFsm(report, getDebugFsmState(), TAG);
    }

    private void toCall(ECallReport report) {
        reportToCallFsm(report, getCallFsmState(), TAG);
    }

    //--------------------- ITwoInput

    public interface ITwoInput {
        default void onInput1() {getInstance().onInput1();}
        default void onInput2() {getInstance().onInput2();}
    }

}
