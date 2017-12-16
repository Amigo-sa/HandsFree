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
    private boolean isPrepared;

    {
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        iCallToUiListeners = new ArrayList<>();
        iCallToUiExchangeListeners = new ArrayList<>();
        iDebugCtrls = new ArrayList<>();
        isPrepared = true;
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return isPrepared && iCallToUiListeners != null && iCallToUiExchangeListeners != null && iDebugCtrls != null;
    }

    @Override
    public boolean takeSettings() {
        opMode = Settings.getInstance().getCommon().getOpMode();
        return true;
    }

    //--------------------- non-settings

    private ArrayList<ICallToUiListener> iCallToUiListeners;
    private ArrayList<ICallToUiExchangeListener> iCallToUiExchangeListeners;
    private ArrayList<IDebugCtrl> iDebugCtrls;

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
        iDebugCtrls.add(iDebugCtrl);
        return this;
    }

    public CallUi addiCallUiListener(ICallToUiListener iCallToUiListener) {
        iCallToUiListeners.add(iCallToUiListener);
        iCallToUiExchangeListeners.add(iCallToUiListener);
        return this;
    }

    public CallUi addiCallUiExchangeListener(ICallToUiExchangeListener iCallToUiExchangeListener) {
        iCallToUiExchangeListeners.add(iCallToUiExchangeListener);
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
        if (iCallToUiListeners != null) {
            iCallToUiListeners.clear();
            iCallToUiListeners = null;
        }
        if (iCallToUiExchangeListeners != null) {
            iCallToUiExchangeListeners.clear();
            iCallToUiExchangeListeners = null;
        }
        if (iDebugCtrls != null) {
            iDebugCtrls.clear();
            iDebugCtrls = null;
        }
        opMode = null;
        return true;
    }

    //--------------------- main

    @Override
    public void onClickBtnGreen() {
        if (debug) Log.i(TAG, "onClickBtnGreen");
        if (!isPrepared) {
            Log.e(TAG, "onClickBtnGreen not initiated");
            return;
        }
        if (debug) Log.w(TAG, "onClickBtnGreen opMode is " + opMode.getSettingName());
        switch (opMode) {
            case AudIn2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
            case Net2Net:
                switch (getCallerState()) {
                    case Null:
                        if (setCallerState(CallerState.Null, CallerState.DebugLoopBack))
                            for (IDebugCtrl listener : iDebugCtrls)
                                listener.startDebug();
                        break;
                    default:
                        if (debug) Log.e(TAG, "onClickBtnGreen " + getCallerStateName());
                        break;
                }
                break;
            case Record:
                switch (getCallerState()) {
                    case DebugRecorded:
                        if (setCallerState(CallerState.DebugRecorded, CallerState.DebugPlay))
                            for (IDebugCtrl listener : iDebugCtrls)
                                listener.startDebug();
                        break;
                    case Null:
                        if (setCallerState(CallerState.Null, CallerState.DebugRecord))
                            for (IDebugCtrl listener : iDebugCtrls)
                                listener.startDebug();
                        break;
                    default:
                        if (debug) Log.e(TAG, "onClickBtnGreen " + getCallerStateName());
                        break;
                }
                break;
            case Normal:
            default:
                switch (getCallerState()) {
                    case Idle:
                        if (debug) Log.i(TAG, "onClickBtnGreen Idle");
                        if (setCallerState(CallerState.Idle, CallerState.OutcomingStarted))
                            for (ICallToUiListener listener : iCallToUiListeners)
                                listener.callOutcomingStarted();
                        break;
                    case IncomingDetected:
                        if (debug) Log.i(TAG, "onClickBtnGreen IncomingDetected");
                        if (setCallerState(CallerState.IncomingDetected, CallerState.Call))
                            for (ICallToUiExchangeListener listener : iCallToUiExchangeListeners)
                                listener.callIncomingAccepted();
                        break;
                    default:
                        if (debug) Log.e(TAG, "onClickBtnGreen " + getCallerStateName());
                        break;
                }
                break;
        }
    }

    @Override
    public void onClickBtnRed() {
        if (debug) Log.i(TAG, "onClickBtnRed");
        if (!isPrepared) {
            Log.e(TAG, "onClickBtnRed not initiated");
            return;
        }
        switch (opMode) {
            case AudIn2Bt:
            case Bt2AudOut:
            case AudIn2AudOut:
            case Bt2Bt:
            case Net2Net:
                switch (getCallerState()) {
                    case DebugLoopBack:
                        if (setCallerState(CallerState.DebugLoopBack, CallerState.Null))
                            for (IDebugCtrl listener : iDebugCtrls)
                                listener.stopDebug();
                        break;
                    default:
                        if (debug) Log.e(TAG, "onClickBtnRed " + getCallerStateName());
                        break;
                }
                break;
            case Record:
                switch (getCallerState()) {
                    case DebugPlay:
                        if (setCallerState(CallerState.DebugPlay, CallerState.DebugRecorded))
                            for (IDebugCtrl listener : iDebugCtrls)
                                listener.stopDebug();
                        break;
                    case DebugRecord:
                        if (setCallerState(CallerState.DebugRecord, CallerState.DebugRecorded))
                            for (IDebugCtrl listener : iDebugCtrls)
                                listener.stopDebug();
                        break;
                    default:
                        if (debug) Log.e(TAG, "onClickBtnRed " + getCallerStateName());
                        break;
                }
                break;
            case Normal:
            default:
                switch (getCallerState()) {
                    case Call:
                        if (debug) Log.i(TAG, "onClickBtnRed Call");
                        if (setCallerState(CallerState.Call, CallerState.Idle))
                            for (ICallToUiExchangeListener listener : iCallToUiExchangeListeners)
                                listener.callEndedInternally();
                        break;
                    case OutcomingStarted:
                        if (debug) Log.i(TAG, "onClickBtnRed OutcomingStarted");
                        if (setCallerState(CallerState.OutcomingStarted, CallerState.Idle))
                            for (ICallToUiListener listener : iCallToUiListeners)
                                listener.callOutcomingCanceled();
                        break;
                    case OutcomingConnected:
                        if (debug) Log.i(TAG, "onClickBtnRed OutcomingConnected");
                        if (setCallerState(CallerState.OutcomingConnected, CallerState.Idle))
                            for (ICallToUiListener listener : iCallToUiListeners)
                                listener.callOutcomingCanceled();
                        break;
                    case IncomingDetected:
                        if (debug) Log.i(TAG, "onClickBtnRed IncomingDetected");
                        if (setCallerState(CallerState.IncomingDetected, CallerState.Idle))
                            for (ICallToUiListener listener : iCallToUiListeners)
                                listener.callIncomingRejected();
                        break;
                    default:
                        if (debug) Log.e(TAG, "onClickBtnRed " + getCallerStateName());
                        break;
                }
                break;
        }
    }

    private void report(Runnable runnable) {
        if (debug) Log.i(TAG, "report");
        if (runnable == null) {
            Log.e(TAG, "report runnable is null");
            return;
        }

    }

}
