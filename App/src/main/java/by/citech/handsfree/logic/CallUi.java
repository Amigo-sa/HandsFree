package by.citech.handsfree.logic;

import android.util.Log;
import java.util.ArrayList;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.debug.IDebugCtrl;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.enumeration.OpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class CallUi
        implements ICallUi, IBase, ISettingsCtrl, IPrepareObject, ICaller {

    private static final String STAG = Tags.CALL_UI;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private OpMode opMode;
    private ArrayList<ICallToUiListener> iToUis;
    private ArrayList<ICallToUiExchangeListener> iToUiExs;
    private ArrayList<IDebugCtrl> iDebugs;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        iToUis = new ArrayList<>();
        iToUiExs = new ArrayList<>();
        iDebugs = new ArrayList<>();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return iToUis != null && iToUiExs != null && iDebugs != null && opMode != null;
    }

    @Override
    public boolean takeSettings() {
        opMode = Settings.getInstance().getCommon().getOpMode();
        return true;
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
        } else {
            instance.prepareObject();
        }
        return instance;
    }

    //--------------------- getters and setters

    public CallUi addiDebugListener(IDebugCtrl iDebugCtrl) {
        iDebugs.add(iDebugCtrl);
        return this;
    }

    public CallUi addiCallUiListener(ICallToUiListener iCallToUiListener) {
        iToUis.add(iCallToUiListener);
        iToUiExs.add(iCallToUiListener);
        return this;
    }

    public CallUi addiCallUiExchangeListener(ICallToUiExchangeListener iCallToUiExchangeListener) {
        iToUiExs.add(iCallToUiExchangeListener);
        return this;
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
        if (iToUis != null) {
            iToUis.clear();
        }
        if (iToUiExs != null) {
            iToUiExs.clear();
        }
        if (iDebugs != null) {
            iDebugs.clear();
        }
        opMode = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- main

    private void onBtnRedWrongState(CallerState callerState) {
        Log.e(TAG, "onClickBtnRed " + callerState.getName());
    }

    private void onBtnGreenWrongState(CallerState callerState) {
        Log.e(TAG, "onClickBtnGreen " + callerState.getName());
    }

    @Override
    public void onClickBtnGreen() {
        if (!prepareObject()) {Log.e(TAG, "onClickBtnGreen not initiated"); return;}
        if (debug) Log.w(TAG, "onClickBtnGreen opMode is " + opMode.getSettingName());
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "onClickBtnGreen callerState is " + callerState.getName());
        switch (opMode) {
            case Net2Net:
                Log.e(TAG, "onClickBtnGreen opMode Net2Net not implemented yet"); break;
            case AudIn2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (callerState) {
                    case Null:
                        if (setCallerState(CallerState.Null, CallerState.DebugLoopBack)) {for (IDebugCtrl l : iDebugs) l.startDebug(); return;}
                        else break;
                    default:
                        onBtnGreenWrongState(callerState); return;
                }
                break;
            case Record:
                switch (callerState) {
                    case DebugRecorded:
                        if (setCallerState(CallerState.DebugRecorded, CallerState.DebugPlay)) {for (IDebugCtrl l : iDebugs) l.startDebug(); return;}
                        else break;
                    case Null:
                        if (setCallerState(CallerState.Null, CallerState.DebugRecord)) {for (IDebugCtrl l : iDebugs) l.startDebug(); return;}
                        else break;
                    default:
                        onBtnGreenWrongState(callerState); return;
                }
                break;
            case Normal:
                switch (callerState) {
                    case Idle:
                        if (setCallerState(CallerState.Idle, CallerState.OutcomingStarted)) {for (ICallToUiListener l : iToUis) l.callOutcomingStarted(); return;}
                        else break;
                    case IncomingDetected:
                        if (setCallerState(CallerState.IncomingDetected, CallerState.Call)) {for (ICallToUiExchangeListener l : iToUiExs) l.callIncomingAccepted(); return;}
                        else break;
                    default:
                        onBtnGreenWrongState(callerState); return;
                }
                break;
            default:
                Log.e(TAG, "onClickBtnGreen opMode default"); return;
        }
        if (debug) Log.w(TAG, "onClickBtnGreen recursive call");
        onClickBtnGreen();
    }

    @Override
    public void onClickBtnRed() {
        if (!prepareObject()) {Log.e(TAG, "onClickBtnRed not prepared, return"); return;}
        if (debug) Log.i(TAG, "onClickBtnRed opMode is " + opMode.getSettingName());
        CallerState callerState = getCallerState();
        if (debug) Log.i(TAG, "onClickBtnRed callerState is " + callerState.getName());
        switch (opMode) {
            case Net2Net: Log.e(TAG, "onClickBtnRed opMode Net2Net not implemented yet"); return;
            case AudIn2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
                switch (callerState) {
                    case DebugLoopBack:
                        if (setCallerState(CallerState.DebugLoopBack, CallerState.Null)) {for (IDebugCtrl l : iDebugs) l.stopDebug(); return;}
                        else break;
                    default:
                        onBtnRedWrongState(callerState); return;
                }
                break;
            case Record:
                switch (callerState) {
                    case DebugPlay:
                        if (setCallerState(CallerState.DebugPlay, CallerState.DebugRecorded)) {for (IDebugCtrl l : iDebugs) l.stopDebug(); return;}
                        else break;
                    case DebugRecord:
                        if (setCallerState(CallerState.DebugRecord, CallerState.DebugRecorded)) {for (IDebugCtrl l : iDebugs) l.stopDebug(); return;}
                        else break;
                    default:
                        onBtnRedWrongState(callerState); return;
                }
                break;
            case Normal:
                switch (callerState) {
                    case Call:
                        if (setCallerState(CallerState.Call, CallerState.Idle)) {for (ICallToUiExchangeListener l : iToUiExs) l.callEndedInternally(); return;}
                        else break;
                    case OutcomingStarted:
                        if (setCallerState(CallerState.OutcomingStarted, CallerState.Idle)) {for (ICallToUiListener l : iToUis) l.callOutcomingCanceled(); return;}
                        else break;
                    case OutcomingConnected:
                        if (setCallerState(CallerState.OutcomingConnected, CallerState.Idle)) {for (ICallToUiListener l : iToUis) l.callOutcomingCanceled(); return;}
                        else break;
                    case IncomingDetected:
                        if (setCallerState(CallerState.IncomingDetected, CallerState.Idle)) {for (ICallToUiListener l : iToUis) l.callIncomingRejected(); return;}
                        else break;
                    default:
                        onBtnRedWrongState(callerState); return;
                }
                break;
            default:
                Log.e(TAG, "onClickBtnRed opMode default"); return;
        }
        if (debug) Log.w(TAG, "onClickBtnRed recursive call");
        onClickBtnRed();
    }

}
