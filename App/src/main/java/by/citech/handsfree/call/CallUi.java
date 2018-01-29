package by.citech.handsfree.call;

import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.debug.fsm.DebugFsm;
import by.citech.handsfree.debug.fsm.EDebugState;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.call.fsm.ECallReport.*;
import static by.citech.handsfree.debug.fsm.EDebugReport.RP_StartDebug;
import static by.citech.handsfree.debug.fsm.EDebugReport.RP_StopDebug;
import static by.citech.handsfree.debug.fsm.EDebugState.ST_DebugLoop;
import static by.citech.handsfree.settings.EOpMode.Normal;

public class CallUi implements CallFsm.ICallFsmReporter, DebugFsm.IDebugFsmReporter {

    private static final boolean debug = Settings.debug;
    private final String TAG = Tags.CallUi;
    private final static String onClickBtnGreen = "onClickBtnGreen";
    private final static String onClickBtnRed = "onClickBtnRed";

    //--------------------- preparation

    private EOpMode opMode;

    {
        opMode = Settings.Common.opMode;
    }

    //--------------------- singleton

    private static volatile CallUi instance = null;

    private CallUi() {
    }

    public static CallUi getInstance() {
        if (instance == null) {
            synchronized (CallUi.class) {
                if (instance == null) {instance = new CallUi();}}}
        return instance;
    }

    //--------------------- main

    private void onMethodWrongState(Object state, String methodName) {
        if (debug) Timber.e(methodName + " " + state);
    }

    //--------------------- btnGreen

    void onClickBtnGreen() {
        if (opMode == Normal) {
            onClickBtnGreenNormal();
        } else {
            onClickBtnGreenAbnormal();
        }
    }

    private void onClickBtnGreenNormal() {
        ECallState state = (ECallState) getCallFsmState();
        if (debug) Timber.w("onClickBtnGreenNormal opMode is %s, state is %s", opMode, state);
        switch (state) {
            case ST_Ready:
                if (reportToCallFsm(RP_OutStartedLocal, state, TAG)) return; else break;
            case ST_InConnected:
                if (reportToCallFsm(RP_InAcceptedLocal, state, TAG)) return; else break;
            default:
                onMethodWrongState(state, onClickBtnGreen); return;
        }
        if (debug) Timber.w("onClickBtnGreenNormal recursive call");
        onClickBtnGreenNormal();
    }

    private void onClickBtnGreenAbnormal() {
        EDebugState debugState = (EDebugState) getDebugFsmState();
        if (debug) Timber.w("onClickBtnGreenAbnormal opMode is %s, debugState is %s", opMode, debugState);
        switch (opMode) {
            case DataGen2Bt:
            case AudIn2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (debugState) {
                    case ST_TurnedOn:
                        if (reportToDebugFsm(RP_StartDebug, debugState, TAG)) return; else break;
                    default:
                        onMethodWrongState(debugState, onClickBtnGreen); return;
                }
                break;
            case Record:
                switch (debugState) {
                    case ST_DebugRecorded:
                    case ST_TurnedOn:
                        if (reportToDebugFsm(RP_StartDebug, debugState, TAG)) return; else break;
                    default:
                        onMethodWrongState(debugState, onClickBtnGreen); return;
                }
                break;
            case Net2Net:
            default:
                if (debug) Timber.e("onClickBtnGreenAbnormal illegal opMode: %s", opMode); return;
        }
        if (debug) Timber.w("onClickBtnGreenAbnormal recursive call");
        onClickBtnGreenAbnormal();
    }

    //--------------------- btnRed

    void onClickBtnRed() {
        if (opMode == Normal) {
            onClickBtnRedNormal();
        } else {
            onClickBtnRedAbnormal();
        }
    }

    private void onClickBtnRedNormal() {
        ECallState state = (ECallState) getCallFsmState();
        if (debug) Timber.w("onClickBtnRedNormal opMode is %s, state is %s", opMode, state);
        switch (state) {
            case ST_Call:
                if (reportToCallFsm(RP_CallEndedLocal, state, TAG)) return; else break;
            case ST_OutStarted:
            case ST_OutConnected:
                if (reportToCallFsm(RP_OutCanceledLocal, state, TAG)) return; else break;
            case ST_InConnected:
                if (reportToCallFsm(RP_InRejectedLocal, state, TAG)) return; else break;
            default:
                onMethodWrongState(state, onClickBtnRed); return;
        }
        if (debug) Timber.w("onClickBtnRedNormal recursive call");
        onClickBtnRedNormal();
    }

    private void onClickBtnRedAbnormal() {
        EDebugState state = (EDebugState) getDebugFsmState();
        if (debug) Timber.w("onClickBtnRedAbnormal opMode is %s, state is %s", opMode, state);
        switch (opMode) {
            case AudIn2Bt:
            case DataGen2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (state) {
                    case ST_DebugLoop:
                        if (reportToCallFsm(RP_StopDebug, state, TAG)) return; else break;
                    default:
                        onMethodWrongState(state, onClickBtnRed); return;
                }
                break;
            case Record:
                switch (state) {
                    case ST_DebugPlay:
                    case ST_DebugRecord:
                        if (reportToCallFsm(RP_StopDebug, state, TAG)) return; else break;
                    default:
                        onMethodWrongState(state, onClickBtnRed); return;
                }
                break;
            case Net2Net:
            default:
                if (debug) Timber.e("onClickBtnRedAbnormal illegal opMode: %s", opMode); return;
        }
        if (debug) Timber.w("onClickBtnRedAbnormal recursive call");
        onClickBtnRedAbnormal();
    }

}
