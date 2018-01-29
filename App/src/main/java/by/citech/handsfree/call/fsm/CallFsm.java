package by.citech.handsfree.call.fsm;

import by.citech.handsfree.fsm.FsmCore;
import by.citech.handsfree.fsm.IFsmListener;
import by.citech.handsfree.fsm.IFsmReport;
import by.citech.handsfree.fsm.IFsmState;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.call.fsm.ECallReport.*;
import static by.citech.handsfree.call.fsm.ECallState.*;

public class CallFsm extends FsmCore {

    //--------------------- singleton

    private static volatile CallFsm instance = null;

    private CallFsm() {
        super(Tags.CallFsm);

        toMap(RP_TurningOff,            ST_TurnedOff);
        toMap(RP_TurningOn,             ST_TurnedOn);

//      toMap(RP_BtReady,               ST_BtReady);
//      toMap(RP_NetReady,              ST_BtReady);
//      toMap(RP_BtError,               ST_NetReady);
//      toMap(RP_NetError,              ST_BtReady);

        toMap(RP_InConnected,           ST_InConnected);
        toMap(RP_InFailed,              ST_Ready);
        toMap(RP_InCanceledRemote,      ST_Ready);
        toMap(RP_InRejectedLocal,       ST_Ready);
        toMap(RP_InAcceptedLocal,       ST_Call);
        toMap(RP_OutStartedLocal,       ST_OutStarted);
        toMap(RP_OutInvalidCoordinates, ST_Ready);
        toMap(RP_OutFailed,             ST_Ready);
        toMap(RP_OutCanceledLocal,      ST_Ready);
        toMap(RP_OutConnected,          ST_OutConnected);
        toMap(RP_OutRejectedRemote,     ST_Ready);
        toMap(RP_OutAcceptedRemote,     ST_Call);
        toMap(RP_CallEndedLocal,        ST_Ready);
        toMap(RP_CallEndedRemote,       ST_Ready);
        toMap(RP_CallFailedExternally,  ST_Ready);
        toMap(RP_CallFailedInternally,  ST_Ready);

        currState = ST_TurnedOff;
        processReport(RP_TurningOn, getFsmCurrentState(), Tags.ConnectionFsm);
    }

    public static CallFsm getInstance() {
        if (instance == null) {
            synchronized (CallFsm.class) {
                if (instance == null) {instance = new CallFsm();}}}
        return instance;
    }

    //--------------------- ICallFsmReporter

    synchronized boolean processReport(IFsmReport report, IFsmState from, String msg) {
        return checkFsmReport(report, from, msg) && processFsmReport(report, from);
    }

    //--------------------- processing

    @Override
    synchronized protected boolean processFsmReport(IFsmReport why, IFsmState from) {
        if (debug) Timber.i("processFsmReport");
        ECallState fromCasted = (ECallState) from;
        ECallReport whyCasted = (ECallReport) why;
        switch (whyCasted) {
            case RP_BtError:
                switch (fromCasted) {
                    case ST_BtReady:
                        return processFsmStateChange(why, from, ST_TurnedOn);
                    default:
                        return processFsmStateChange(why, from, ST_NetReady);
                }
            case RP_BtReady:
                switch (fromCasted) {
                    case ST_TurnedOn:
                        return processFsmStateChange(why, from, ST_BtReady);
                    case ST_NetReady:
                        return processFsmStateChange(why, from, ST_Ready);
                    default:
                        return true;
                }
            case RP_NetError:
                switch (fromCasted) {
                    case ST_NetReady:
                        return processFsmStateChange(why, from, ST_TurnedOn);
                    default:
                        return processFsmStateChange(why, from, ST_BtReady);
                }
            case RP_NetReady:
                switch (fromCasted) {
                    case ST_TurnedOn:
                        return processFsmStateChange(why, from, ST_NetReady);
                    case ST_BtReady:
                        return processFsmStateChange(why, from, ST_Ready);
                    default:
                        return true;
                }
            default:
                return processFsmStateChange(why, from, fromMap(why));
        }
    }

    //--------------------- interfaces

    public interface ICallFsmReporter {

        default IFsmState getCallFsmState() {
            return getInstance().getFsmCurrentState();
        }

        default boolean reportToCallFsm(IFsmReport whatHappened, IFsmState fromWhichState, String fromWho) {
            return getInstance().processReport(whatHappened, fromWhichState, fromWho);
        }

    }

    public interface ICallFsmListenerRegister {

        default boolean registerCallFsmListener(ICallFsmListener listener, String who) {
            return getInstance().registerFsmListener(listener, who);
        }

        default boolean unregisterCallFsmListener(ICallFsmListener listener, String who) {
            return getInstance().unregisterFsmListener(listener, who);
        }

    }

    public interface ICallFsmListener extends IFsmListener {}

}
