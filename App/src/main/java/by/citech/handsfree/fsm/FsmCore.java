package by.citech.handsfree.fsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

abstract public class FsmCore {

    protected static final boolean debug = Settings.debug;
    private final String fsmName;

    protected Map<IFsmReport, IFsmState> changeMap;
    protected Collection<IFsmListener> listeners;
    protected IFsmState prevState, currState;
    protected IFsmReport prevReport, currReport;

    {
        changeMap = new HashMap<>();
        listeners = new ConcurrentLinkedQueue<>();
    }

    public FsmCore(String fsmName) {
        this.fsmName = fsmName;
    }

    //--------------------- transitions map

    protected void toMap(IFsmReport report, IFsmState state) {
        changeMap.put(report, state);
    }

    //--------------------- abstract

    abstract protected boolean implementedProcessFsmReport(IFsmReport report, IFsmState from);

    //--------------------- reporter

    synchronized protected IFsmState getFsmCurrentState() {
        return currState;
    }

    synchronized protected boolean processFsmReport(IFsmReport report, IFsmState from, String msg) {
        if (debug) Timber.w("%s processFsmReport: report <%s> from <%s>, message: <%s>", fsmName, report, from, msg);
        if (report == null || from == null || msg == null) {
            if (debug) Timber.e("%s processFsmReport %s", fsmName, StatusMessages.ERR_PARAMETERS);
            return false;
        } else return true;
    }

    //--------------------- listener

    synchronized protected void onFsmStateChange(IFsmState from, IFsmState to, IFsmReport why) {
        if (debug) Timber.w("%s onFsmStateChange: <%s> ==> <%s>, report: <%s>", fsmName, from, to, why);
        for (IFsmListener listener : listeners) listener.onFsmStateChange(from, to, why);
    }

    //--------------------- register and unregister

    synchronized protected boolean registerFsmListener(IFsmListener listener, String who, IFsmState... states) {
        if (states == null || states.length == 0) return registerFsmListener(listener, who);
        else return false; // TODO: доделать логику слушателя только выбранных сообщений
    }

    synchronized protected boolean registerFsmListener(IFsmListener listener, String who) {
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

    synchronized protected boolean unregisterFsmListener(IFsmListener listener, String who) {
        boolean isRemoved;
        isRemoved = listeners.remove(listener);
        if (isRemoved) {if (debug) Timber.w("%s unregister success: <%s>, count: <%d>", fsmName, who, listeners.size());}
        else           {if (debug) Timber.e("%s unregister fail: <%s>, count: still <%d>", fsmName, who, listeners.size());}
        return isRemoved;
    }

    //--------------------- processing

    synchronized protected boolean processFsmStateChange(IFsmReport why, IFsmState from, IFsmState to) {
        return processFsmStateChange(from, to, why, false);
    }

    synchronized protected boolean processFsmStateChange(IFsmState from, IFsmState to, IFsmReport why, boolean isForce) {
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
