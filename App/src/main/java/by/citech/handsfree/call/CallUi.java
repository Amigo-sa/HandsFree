package by.citech.handsfree.call;

import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.call.fsm.ICallFsmReporter;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.call.fsm.ECallReport.CallEndedByLocalUser;
import static by.citech.handsfree.call.fsm.ECallReport.InCallAcceptedByLocalUser;
import static by.citech.handsfree.call.fsm.ECallReport.InCallRejectedByLocalUser;
import static by.citech.handsfree.call.fsm.ECallReport.OutCallCanceledByLocalUser;
import static by.citech.handsfree.call.fsm.ECallReport.OutConnectionCanceledByLocalUser;
import static by.citech.handsfree.call.fsm.ECallReport.OutConnectionStartedByLocalUser;
import static by.citech.handsfree.call.fsm.ECallReport.StartDebug;
import static by.citech.handsfree.call.fsm.ECallReport.StopDebug;

public class CallUi
        implements ICallFsmReporter {

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
                if (instance == null) {
                    instance = new CallUi();
                }
            }
        }
        return instance;
    }

    //--------------------- main

    private void onMethodWrongState(ECallState callerState, String methodName) {
        if (debug) Timber.e(methodName + " " + callerState);
    }

    void onClickBtnGreen() {
        ECallState callerState = getCallerFsmState();
        if (debug) Timber.w("onClickBtnGreen opMode is %s, callerState is %s", opMode, callerState);
        switch (opMode) {
            case DataGen2Bt:
            case AudIn2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (callerState) {
                    case PhaseZero:
                        if (reportToCallerFsm(callerState, StartDebug, TAG)) return; else break;
                    default:
                        onMethodWrongState(callerState, onClickBtnGreen); return;
                }
                break;
            case Record:
                switch (callerState) {
                    case DebugRecorded:
                    case PhaseZero:
                        if (reportToCallerFsm(callerState, StartDebug, TAG)) return; else break;
                    default:
                        onMethodWrongState(callerState, onClickBtnGreen); return;
                }
                break;
            case Normal:
                switch (callerState) {
                    case ReadyToWork:
                        if (reportToCallerFsm(callerState, OutConnectionStartedByLocalUser, TAG)) return; else break;
                    case InDetected:
                        if (reportToCallerFsm(callerState, InCallAcceptedByLocalUser, TAG)) return; else break;
                    default:
                        onMethodWrongState(callerState, onClickBtnGreen); return;
                }
                break;
            case Net2Net:
            default:
                if (debug) Timber.e("onClickBtnGreen illegal opMode: %s", opMode); return;
        }
        if (debug) Timber.tag(TAG).w("onClickBtnGreen recursive call");
        onClickBtnGreen();
    }

    void onClickBtnRed() {
        ECallState callerState = getCallerFsmState();
        if (debug) Timber.w("onClickBtnRed opMode is %s, callerState is %s", opMode, callerState);
        switch (opMode) {
            case AudIn2Bt:
            case DataGen2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (callerState) {
                    case DebugLoop:
                        if (reportToCallerFsm(callerState, StopDebug, TAG)) return; else break;
                    default:
                        onMethodWrongState(callerState, onClickBtnRed); return;
                }
                break;
            case Record:
                switch (callerState) {
                    case DebugPlay:
                    case DebugRecord:
                        if (reportToCallerFsm(callerState, StopDebug, TAG)) return; else break;
                    default:
                        onMethodWrongState(callerState, onClickBtnRed); return;
                }
                break;
            case Normal:
                switch (callerState) {
                    case Call:
                        if (reportToCallerFsm(callerState, CallEndedByLocalUser, TAG)) return; else break;
                    case OutStarted:
                        if (reportToCallerFsm(callerState, OutConnectionCanceledByLocalUser, TAG)) return; else break;
                    case OutConnected:
                        if (reportToCallerFsm(callerState, OutCallCanceledByLocalUser, TAG)) return; else break;
                    case InDetected:
                        if (reportToCallerFsm(callerState, InCallRejectedByLocalUser, TAG)) return; else break;
                    default:
                        onMethodWrongState(callerState, onClickBtnRed); return;
                }
                break;
            case Net2Net:
            default:
                if (debug) Timber.e("onClickBtnRed illegal opMode: %s", opMode); return;
        }
        if (debug) Timber.tag(TAG).w("onClickBtnRed recursive call");
        onClickBtnRed();
    }

}
