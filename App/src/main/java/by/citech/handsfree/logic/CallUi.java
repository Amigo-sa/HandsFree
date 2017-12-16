package by.citech.handsfree.logic;

import android.util.Log;
import java.util.ArrayList;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.debug.IDebugCtrl;
import by.citech.handsfree.gui.ICallToUiExchangeListener;
import by.citech.handsfree.gui.ICallToUiListener;
import by.citech.handsfree.gui.IUiToCallListener;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.enumeration.OpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class CallUi
        implements IUiToCallListener, IBase, ISettingsCtrl, IPrepareObject, ICaller {

    private static final String TAG = Tags.CALL_UI;
    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private OpMode opMode;

    {
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

    //--------------------- non-settings

    private ArrayList<ICallToUiListener> iToUis;
    private ArrayList<ICallToUiExchangeListener> iToUiExs;
    private ArrayList<IDebugCtrl> iDebugs;

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
        IBase.super.baseStop();
        if (debug) Log.i(TAG, "baseStop");
        if (iToUis != null) {
            iToUis.clear();
            iToUis = null;
        }
        if (iToUiExs != null) {
            iToUiExs.clear();
            iToUiExs = null;
        }
        if (iDebugs != null) {
            iDebugs.clear();
            iDebugs = null;
        }
        opMode = null;
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
        if (debug) Log.i(TAG, "onClickBtnGreen");
        if (!prepareObject()) {Log.e(TAG, "onClickBtnGreen not initiated");return;}
        CallerState callerState = getCallerState();
        if (debug) Log.w(TAG, "onClickBtnGreen opMode is " + opMode.getSettingName());
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
            default:
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
        }
        if (debug) Log.w(TAG, "onClickBtnGreen recursive call");
        onClickBtnGreen();
    }

    @Override
    public void onClickBtnRed() {
        if (debug) Log.i(TAG, "onClickBtnRed");
        if (!prepareObject()) {Log.e(TAG, "onClickBtnRed not prepared, return"); return;}
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
            default:
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
        }
        if (debug) Log.w(TAG, "onClickBtnRed recursive call");
        onClickBtnRed();
    }

}
