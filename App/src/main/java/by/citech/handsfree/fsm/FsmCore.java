package by.citech.handsfree.fsm;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

abstract public class FsmCore<L extends IFsmListener<S, R>, S extends IFsmState, R extends IFsmReport> {

    private static final boolean debug = Settings.debug;
    private final String FSM_NAME;

    private Collection<L> listeners;
    private S prevState, currState;
    private R prevReport, currReport;

    {
        listeners = new ConcurrentLinkedQueue<>();
    }

    public FsmCore(String FSM_NAME) {
        this.FSM_NAME = FSM_NAME;
    }

    synchronized protected void onFsmStateChange(S from, S to, R why) {
        if (debug) Timber.w("%s onFsmStateChange: <%s> ==> <%s>, report: <%s>", FSM_NAME, from, to, why);
        for (L listener : listeners) listener.onFsmStateChange(from, to, why);
    }

    //--------------------- reporter

    synchronized S getState() {
        return currState;
    }

    synchronized boolean processReport(R report, S from, String msg) {
        if (debug) Timber.w("%s processReport: report <%s> from <%s>, message: <%s>", FSM_NAME, report, from, msg);
        if (report == null || from == null || msg == null) {
            if (debug) Timber.e("%s processReport %s", FSM_NAME, StatusMessages.ERR_PARAMETERS);
            return false;
        } else {
            return true;
        }
    }

    //--------------------- register and unregister

    synchronized protected boolean register(L listener, String who) {
        boolean isAdded;
        if (listener == null) {
            if (debug) Timber.w("%s register fail, null listener: <%s>", FSM_NAME, who);
            return false;
        } else if (listeners.contains(listener)) {
            if (debug) Timber.w("%s register fail, already registered: <%s>", FSM_NAME, who);
            isAdded = true;
        } else {
            isAdded = listeners.add(listener);
            if (isAdded) {if (debug) Timber.i("%s register success: <%s>, count: <%d>", FSM_NAME, who, listeners.size());}
            else         {if (debug) Timber.e("%s register fail: <%s>, count: still <%d>", FSM_NAME, who, listeners.size());}
        }
        if (isAdded) listener.onFsmStateChange(prevState, currState, prevReport);
        return isAdded;
    }

    synchronized protected boolean unregister(L listener, String who) {
        boolean isRemoved;
        isRemoved = listeners.remove(listener);
        if (isRemoved) {if (debug) Timber.w("%s unregister success: <%s>, count: <%d>", FSM_NAME, who, listeners.size());}
        else           {if (debug) Timber.e("%s unregister fail: <%s>, count: still <%d>", FSM_NAME, who, listeners.size());}
        return isRemoved;
    }

    //--------------------- processing

    synchronized protected boolean process(S from, S to, R why) {
        return process(from, to, why, false);
    }

    synchronized protected boolean process(S from, S to, R why, boolean isForce) {
        if (currState == from || isForce) {
            if (from.availableFromAny().contains(to) || from.available().contains(to) || isForce) {
                prevReport = currReport;
                currReport = why;
                prevState = currState;
                currState = to;
                onFsmStateChange(from, to, why);
                return true;
            } else if (debug) Timber.e("%s process: <%s> not available from <%s>", FSM_NAME, to, from);
        } else if (debug) Timber.e("%s process: currState is <%s>, not <%s>", FSM_NAME, currState, from);
        return false;
    }

}
