package by.citech.logic;

import android.util.Log;
import java.util.ArrayList;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class CallUi implements IUiBtnGreenRedListener {

    private ArrayList<ICallUiListener> iCallUiListeners;

    //--------------------- singleton

    private static volatile CallUi instance = null;

    private CallUi() {
        iCallUiListeners = new ArrayList<>();
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

    public CallUi addiCallUiListener(ICallUiListener iCallUiListener) {
        iCallUiListeners.add(iCallUiListener);
        return this;
    }

    //--------------------- common

    private String getStateName() {
        return Caller.getInstance().getState().getName();
    }

    private State getState() {
        return Caller.getInstance().getState();
    }

    private boolean setState(State fromState, State toState) {
         return Caller.getInstance().setState(fromState, toState);
    }

    //--------------------- main

    @Override
    public void onClickBtnGreen() {
        if (Settings.debug) Log.i(Tags.CALL_UI, "onClickBtnGreen");
        switch (getState()) {
            case Idle:
                if (Settings.debug) Log.i(Tags.CALL_UI, "onClickBtnGreen Idle");
                if (setState(State.Idle, State.OutcomingStarted))
                    for (ICallUiListener listener : iCallUiListeners) listener.callOutcomingStarted();
                break;
            case IncomingDetected:
                if (Settings.debug) Log.i(Tags.CALL_UI, "onClickBtnGreen IncomingDetected");
                if (setState(State.IncomingDetected, State.Call))
                    for (ICallUiListener listener : iCallUiListeners) listener.callIncomingAccepted();
                break;
            default:
                if (Settings.debug) Log.e(Tags.CALL_UI, "onClickBtnGreen " + getStateName());
        }
    }

    @Override
    public void onClickBtnRed() {
        if (Settings.debug) Log.i(Tags.CALL_UI, "onClickBtnRed");
        switch (getState()) {
            case Call:
                if (Settings.debug) Log.i(Tags.CALL_UI, "onClickBtnRed Call");
                if (setState(State.Call, State.Idle))
                    for (ICallUiListener listener : iCallUiListeners) listener.callEndedInternally();
                break;
            case OutcomingStarted:
                if (Settings.debug) Log.i(Tags.CALL_UI, "onClickBtnRed OutcomingStarted");
                if (setState(State.OutcomingStarted, State.Idle))
                    for (ICallUiListener listener : iCallUiListeners) listener.callOutcomingCanceled();
                break;
            case OutcomingConnected:
                if (Settings.debug) Log.i(Tags.CALL_UI, "onClickBtnRed OutcomingConnected");
                if (setState(State.OutcomingConnected, State.Idle))
                    for (ICallUiListener listener : iCallUiListeners) listener.callOutcomingCanceled();
                break;
            case IncomingDetected:
                if (Settings.debug) Log.i(Tags.CALL_UI, "onClickBtnRed IncomingDetected");
                if (setState(State.IncomingDetected, State.Idle))
                    for (ICallUiListener listener : iCallUiListeners) listener.callIncomingRejected();
                break;
            default:
                if (Settings.debug) Log.e(Tags.CALL_UI, "onClickBtnRed " + getStateName());

        }
    }
}
