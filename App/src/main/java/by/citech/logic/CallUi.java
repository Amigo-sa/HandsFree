package by.citech.logic;

import android.util.Log;
import java.util.ArrayList;

import by.citech.debug.IDebugListener;
import by.citech.gui.ICallUiExchangeListener;
import by.citech.gui.ICallUiListener;
import by.citech.gui.IUiBtnGreenRedListener;
import by.citech.param.DebugMode;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class CallUi
        implements IUiBtnGreenRedListener {

    private static final String TAG = Tags.CALL_UI;
    private static final boolean debug = Settings.debug;
    private static final DebugMode debugMode = Settings.debugMode;

    private ArrayList<ICallUiListener> iCallUiListeners;
    private ArrayList<ICallUiExchangeListener> iCallUiExchangeListeners;
    private ArrayList<IDebugListener> iDebugListeners;

    //--------------------- singleton

    private static volatile CallUi instance = null;

    private CallUi() {
        iCallUiListeners = new ArrayList<>();
        iCallUiExchangeListeners = new ArrayList<>();
        iDebugListeners = new ArrayList<>();
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

    //--------------------- getters and setters

    public CallUi addiDebugListener(IDebugListener iDebugListener) {
        iDebugListeners.add(iDebugListener);
        return this;
    }

    public CallUi addiCallUiListener(ICallUiListener iCallUiListener) {
        iCallUiListeners.add(iCallUiListener);
        iCallUiExchangeListeners.add(iCallUiListener);
        return this;
    }

    public CallUi addiCallUiExchangeListener(ICallUiExchangeListener iCallUiExchangeListener) {
        iCallUiExchangeListeners.add(iCallUiExchangeListener);
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

    //--------------------- main

    @Override
    public void onClickBtnGreen() {
        if (debug) Log.i(TAG, "onClickBtnGreen");
        switch (debugMode) {
            case MicToAudio:
            case LoopbackBtToBt:
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
                            for (ICallUiListener listener : iCallUiListeners)
                                listener.callOutcomingStarted();
                        break;
                    case IncomingDetected:
                        if (debug) Log.i(TAG, "onClickBtnGreen IncomingDetected");
                        if (setCallerState(CallerState.IncomingDetected, CallerState.Call))
                            for (ICallUiExchangeListener listener : iCallUiExchangeListeners)
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
        switch (debugMode) {
            case MicToAudio:
            case LoopbackBtToBt:
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
                            for (ICallUiExchangeListener listener : iCallUiExchangeListeners)
                                listener.callEndedInternally();
                        break;
                    case OutcomingStarted:
                        if (debug) Log.i(TAG, "onClickBtnRed OutcomingStarted");
                        if (setCallerState(CallerState.OutcomingStarted, CallerState.Idle))
                            for (ICallUiListener listener : iCallUiListeners)
                                listener.callOutcomingCanceled();
                        break;
                    case OutcomingConnected:
                        if (debug) Log.i(TAG, "onClickBtnRed OutcomingConnected");
                        if (setCallerState(CallerState.OutcomingConnected, CallerState.Idle))
                            for (ICallUiListener listener : iCallUiListeners)
                                listener.callOutcomingCanceled();
                        break;
                    case IncomingDetected:
                        if (debug) Log.i(TAG, "onClickBtnRed IncomingDetected");
                        if (setCallerState(CallerState.IncomingDetected, CallerState.Idle))
                            for (ICallUiListener listener : iCallUiListeners)
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
