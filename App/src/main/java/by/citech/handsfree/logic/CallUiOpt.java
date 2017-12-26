package by.citech.handsfree.logic;

import android.util.Log;
import java.util.Locale;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.enumeration.OpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

import static by.citech.handsfree.logic.ECallReport.CallEndedByLocalUser;
import static by.citech.handsfree.logic.ECallReport.InCallAcceptedByLocalUser;
import static by.citech.handsfree.logic.ECallReport.InCallRejectedByLocalUser;
import static by.citech.handsfree.logic.ECallReport.OutCallCanceledByLocalUser;
import static by.citech.handsfree.logic.ECallReport.OutConnectionCanceledByLocalUser;
import static by.citech.handsfree.logic.ECallReport.OutConnectionStartedByLocalUser;
import static by.citech.handsfree.logic.ECallReport.StartDebug;
import static by.citech.handsfree.logic.ECallReport.StopDebug;

public class CallUiOpt
        implements IBase, ISettingsCtrl, IPrepareObject, ICallerFsm {

    private static final String STAG = Tags.CallUi;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    private final static String onClickBtnGreen = "onClickBtnGreen";
    private final static String onClickBtnRed = "onClickBtnRed";

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private OpMode opMode;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return opMode != null;
    }

    @Override
    public boolean takeSettings() {
        opMode = Settings.getInstance().getCommon().getOpMode();
        return true;
    }

    //--------------------- singleton

    private static volatile CallUiOpt instance = null;

    private CallUiOpt() {
    }

    public static CallUiOpt getInstance() {
        if (instance == null) {
            synchronized (CallUiOpt.class) {
                if (instance == null) {
                    instance = new CallUiOpt();
                }
            }
        } else {
            instance.prepareObject();
        }
        return instance;
    }

    //--------------------- base

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        prepareObject();
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        opMode = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- main

    private void onBtnWrongState(String btnName, CallerState callerState) {
        Log.e(TAG, btnName + " " + callerState.getName());
    }

    void onClickBtnGreen() {
        CallerState callerState = getCallerFsmState();
        if (debug) Log.w(TAG, String.format(Locale.US,
                "onClickBtnGreen opMode is %s, callerState is %s",
                opMode.getSettingName(), callerState.getName()));
        switch (opMode) {
            case Net2Net:
            case AudIn2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (callerState) {
                    case Null:
                        if (reportToCallerFsm(callerState, StartDebug, TAG)) return; else break;
                    default:
                        onBtnWrongState(onClickBtnGreen, callerState); return;
                }
                break;
            case Record:
                switch (callerState) {
                    case DebugRecorded:
                    case Null:
                        if (reportToCallerFsm(callerState, StartDebug, TAG)) return; else break;
                    default:
                        onBtnWrongState(onClickBtnGreen, callerState); return;
                }
                break;
            case Normal:
                switch (callerState) {
                    case Idle:
                        if (reportToCallerFsm(callerState, OutConnectionStartedByLocalUser, TAG)) return; else break;
                    case InDetected:
                        if (reportToCallerFsm(callerState, InCallAcceptedByLocalUser, TAG)) return; else break;
                    default:
                        onBtnWrongState(onClickBtnGreen, callerState); return;
                }
                break;
            default:
                Log.e(TAG, "onClickBtnGreen opMode default"); return;
        }
        if (debug) Log.w(TAG, "onClickBtnGreen recursive call");
        onClickBtnGreen();
    }

    void onClickBtnRed() {
        CallerState callerState = getCallerFsmState();
        if (debug) Log.w(TAG, String.format(Locale.US,
                "onClickBtnRed opMode is %s, callerState is %s",
                opMode.getSettingName(), callerState.getName()));
        switch (opMode) {
            case Net2Net:
            case AudIn2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (callerState) {
                    case DebugLoopBack:
                        if (reportToCallerFsm(callerState, StopDebug, TAG)) return; else break;
                    default:
                        onBtnWrongState(onClickBtnRed, callerState); return;
                }
                break;
            case Record:
                switch (callerState) {
                    case DebugPlay:
                    case DebugRecord:
                        if (reportToCallerFsm(callerState, StopDebug, TAG)) return; else break;
                    default:
                        onBtnWrongState(onClickBtnRed, callerState); return;
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
                        onBtnWrongState(onClickBtnRed, callerState); return;
                }
                break;
            default:
                Log.e(TAG, "onClickBtnRed opMode default"); return;
        }
        if (debug) Log.w(TAG, "onClickBtnRed recursive call");
        onClickBtnRed();
    }

}
