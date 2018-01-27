package by.citech.handsfree.fsm;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

abstract public class FsmCore<L extends IFsmListener<S, R>, S extends IFsmState, R extends IFsmReport> {

    private static final boolean debug = Settings.debug;
    private final String fsmName;

    private Collection<L> listeners;
    private S prevState, currState;
    private R prevReport, currReport;

    {
        listeners = new ConcurrentLinkedQueue<>();
    }

    public FsmCore(String fsmName) {
        this.fsmName = fsmName;
    }

    //--------------------- reporter

    synchronized protected void onFsmStateChange(S from, S to, R why) {
        if (debug) Timber.w("%s onFsmStateChange: <%s> ==> <%s>, report: <%s>", fsmName, from, to, why);
        for (L listener : listeners) listener.onFsmStateChange(from, to, why);
    }

    synchronized S getFsmCurrentState() {
        return currState;
    }

    //--------------------- listener

    synchronized boolean processFsmReport(R report, S from, String msg) {
        if (debug) Timber.w("%s processFsmReport: report <%s> from <%s>, message: <%s>", fsmName, report, from, msg);
        if (report == null || from == null || msg == null) {
            if (debug) Timber.e("%s processFsmReport %s", fsmName, StatusMessages.ERR_PARAMETERS);
            return false;
        } else {
            return true;
        }
    }

    //--------------------- register and unregister

    @SafeVarargs
    final synchronized protected boolean registerFsmListener(L listener, String who, S... states) {
        if (states == null || states.length == 0) return registerFsmListener(listener, who);
        else return false; // TODO: доделать логику слушателя только выбранных сообщений
    }

    synchronized protected boolean registerFsmListener(L listener, String who) {
        boolean isAdded;
        if (listener == null) {
            if (debug) Timber.w("%s register fail, null listener: <%s>", fsmName, who);
            return false;
        } else if (listeners.contains(listener)) {
            if (debug) Timber.w("%s register fail, already registered: <%s>", fsmName, who);
            isAdded = true;
        } else {
            isAdded = listeners.add(listener);
            if (isAdded) {if (debug) Timber.i("%s register success: <%s>, count: <%d>", fsmName, who, listeners.size());}
            else         {if (debug) Timber.e("%s register fail: <%s>, count: still <%d>", fsmName, who, listeners.size());}
        }
        if (isAdded) listener.onFsmStateChange(prevState, currState, prevReport);
        return isAdded;
    }

    synchronized protected boolean unregisterFsmListener(L listener, String who) {
        boolean isRemoved;
        isRemoved = listeners.remove(listener);
        if (isRemoved) {if (debug) Timber.w("%s unregister success: <%s>, count: <%d>", fsmName, who, listeners.size());}
        else           {if (debug) Timber.e("%s unregister fail: <%s>, count: still <%d>", fsmName, who, listeners.size());}
        return isRemoved;
    }

    //--------------------- processing

    synchronized protected boolean processFsmStateTransition(S from, S to, R why) {
        return processFsmStateTransition(from, to, why, false);
    }

    synchronized protected boolean processFsmStateTransition(S from, S to, R why, boolean isForce) {
        if (currState == from || isForce) {
            if (from.availableFromAny().contains(to) || from.available().contains(to) || isForce) {
                prevReport = currReport;
                currReport = why;
                prevState = currState;
                currState = to;
                onFsmStateChange(from, to, why);
                return true;
            } else if (debug) Timber.e("%s process: <%s> not available from <%s>", fsmName, to, from);
        } else if (debug) Timber.e("%s process: currState is <%s>, not <%s>", fsmName, currState, from);
        return false;
    }

}
