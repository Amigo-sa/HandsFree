package by.citech.handsfree.fsm;

import java.util.Collection;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

abstract public class FsmCore<
        R extends Enum<R> & IFsmReport<S>,
        S extends Enum<S> & IFsmState<S>> {

    protected static final boolean debug = Settings.debug;
    private final String fsmName;

    protected Collection<IFsmListener<R, S>> listeners;
    protected S prevState, currState;
    protected R prevReport, currReport;
    protected EnumMap<R, S> reportToStateMap;

    {
        listeners = new ConcurrentLinkedQueue<>();
    }

    public FsmCore(String fsmName) {
        this.fsmName = fsmName;
    }

    //--------------------- transitions map

    protected void toMap(R report, S state) {
        reportToStateMap.put(report, state);
    }

    protected S fromMap(R report) {
        return reportToStateMap.get(report);
    }

    //--------------------- abstract

    abstract protected boolean processFsmReport(R report, S from);

    //--------------------- reporter

    synchronized protected S getFsmCurrentState() {
        return currState;
    }

    synchronized protected boolean checkFsmReport(R report, S from, String msg) {
        if (debug) Timber.w("%s checkFsmReport: report <%s> from <%s>, message: <%s>", fsmName, report, from, msg);
        if (report == null || from == null || msg == null) {
            if (debug) Timber.e("%s checkFsmReport %s", fsmName, StatusMessages.ERR_PARAMETERS);
            return false;
        } else return true;
    }

    //--------------------- listener

    synchronized protected void onFsmStateChange(S from, S to, R report) {
        if (debug) Timber.w("%s onFsmStateChange: <%s> ==> <%s>, report: <%s>", fsmName, from, to, report);
        for (IFsmListener<R, S> listener : listeners) listener.onFsmStateChange(from, to, report);
    }

    //--------------------- register and unregister

    synchronized protected boolean registerFsmListener(IFsmListener<R, S> listener, String message, S... states) {
        if (states == null || states.length == 0) return registerFsmListener(listener, message);
        else return false; // TODO: доделать логику слушателя только выбранных сообщений
    }

    synchronized protected boolean registerFsmListener(IFsmListener<R, S> listener, String message) {
        boolean isAdded;
        if (listener == null) {
            if (debug) Timber.w("%s register fail, null listener: <%s>", fsmName, message);
            return false;
        } else if (listeners.contains(listener)) {
            if (debug) Timber.w("%s register fail, already registered: <%s>", fsmName, message);
            isAdded = true;
        } else {
            isAdded = listeners.add(listener);
            if (isAdded) {if (debug) Timber.i("%s register success: <%s>, count: <%d>", fsmName, message, listeners.size());}
            else         {if (debug) Timber.e("%s register fail: <%s>, count: still <%d>", fsmName, message, listeners.size());}
        }
        if (isAdded) listener.onFsmStateChange(prevState, currState, prevReport);
        return isAdded;
    }

    synchronized protected boolean unregisterFsmListener(IFsmListener<R, S> listener, String message) {
        boolean isRemoved;
        isRemoved = listeners.remove(listener);
        if (isRemoved) {if (debug) Timber.w("%s unregister success: <%s>, count: <%d>", fsmName, message, listeners.size());}
        else           {if (debug) Timber.e("%s unregister fail: <%s>, count: still <%d>", fsmName, message, listeners.size());}
        return isRemoved;
    }

    //--------------------- processing

    synchronized protected boolean processFsmStateChange(R why, S from, S to) {
        return processFsmStateChange(from, to, why, false);
    }

    synchronized protected boolean processFsmStateChange(S from, S to, R report, boolean isForce) {
        if (from == null || to == null || report == null) return false;
        if (currState == from || isForce) {
            if (from.availableFromAny().contains(to) || from.available().contains(to) || isForce) {
                prevReport = currReport;
                currReport = report;
                prevState = currState;
                currState = to;
                onFsmStateChange(from, to, report);
                return true;
            } else if (debug) Timber.e("%s process: <%s> not available from <%s>", fsmName, to, from);
        } else if (debug) Timber.e("%s process: currState is <%s>, not <%s>", fsmName, currState, from);
        return false;
    }


}
