package by.citech.handsfree.logic;

import android.util.Log;
import java.util.ArrayList;

import by.citech.handsfree.debug.IDebugListener;
import by.citech.handsfree.gui.ICallToUiExchangeListener;
import by.citech.handsfree.gui.ICallToUiListener;
import by.citech.handsfree.gui.IUiToCallListener;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.enumeration.OpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class CallUi
        implements IUiToCallListener, IBase, ISettingsCtrl {

    private static final String TAG = Tags.CALL_UI;
    private static final boolean debug = Settings.debug;

    //--------------------- settings

    private OpMode opMode;
    private boolean isInitiated;

    {
        initSettings();
    }

    @Override
    public void initSettings() {
        takeSettings();
        iCallToUiListeners = new ArrayList<>();
        iCallToUiExchangeListeners = new ArrayList<>();
        iDebugListeners = new ArrayList<>();
        isInitiated = true;
    }

    @Override
    public void takeSettings() {
        opMode = Settings.opMode;
    }

    //--------------------- non-settings

    private ArrayList<ICallToUiListener> iCallToUiListeners;
    private ArrayList<ICallToUiExchangeListener> iCallToUiExchangeListeners;
    private ArrayList<IDebugListener> iDebugListeners;

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
        } else if (!instance.isInitiated) {
            instance.initSettings();
        }
        return instance;
    }

    //--------------------- getters and setters

    public CallUi addiDebugListener(IDebugListener iDebugListener) {
        iDebugListeners.add(iDebugListener);
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

    //--------------------- common

    private String getCallerStateName() {
        return Caller.getInstance().getCallerState().getName();
    }

    private CallerState getCallerState() {
        return Caller.getInstance().getCallerState();
    }

    private boolean setCallerState(CallerState fromCallerState, CallerState toCallerState) {
         return Caller.getInstance().setState(fromCallerState, toCallerState);
    }

    //--------------------- base

    @Override
    public void baseStart(IBaseAdder iBaseAdder) {
        if (debug) Log.i(TAG, "baseStart");
        if (iBaseAdder == null) {
            Log.e(TAG, "baseStart iBaseAdder is null");
            return;
        } else {
            iBaseAdder.addBase(this);
        }
        if (!isInitiated) {
            initSettings();
        }
    }

    @Override
    public void baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        if (iCallToUiListeners != null) {
            iCallToUiListeners.clear();
            iCallToUiListeners = null;
        }
        if (iCallToUiExchangeListeners != null) {
            iCallToUiExchangeListeners.clear();
            iCallToUiExchangeListeners = null;
        }
        if (iDebugListeners != null) {
            iDebugListeners.clear();
            iDebugListeners = null;
        }
        opMode = null;
        isInitiated = false;
    }

    //--------------------- main

    @Override
    public void onClickBtnGreen() {
        if (debug) Log.i(TAG, "onClickBtnGreen");
        if (!isInitiated) {
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
                            for (IDebugListener listener : iDebugListeners)
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
                            for (IDebugListener listener : iDebugListeners)
                                listener.startDebug();
                        break;
                    case Null:
                        if (setCallerState(CallerState.Null, CallerState.DebugRecord))
                            for (IDebugListener listener : iDebugListeners)
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
        if (!isInitiated) {
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
                            for (IDebugListener listener : iDebugListeners)
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
                            for (IDebugListener listener : iDebugListeners)
                                listener.stopDebug();
                        break;
                    case DebugRecord:
                        if (setCallerState(CallerState.DebugRecord, CallerState.DebugRecorded))
                            for (IDebugListener listener : iDebugListeners)
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

}
