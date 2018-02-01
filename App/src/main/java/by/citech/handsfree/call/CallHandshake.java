package by.citech.handsfree.call;

import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.exchange.IRx;
import by.citech.handsfree.exchange.ITx;
import by.citech.handsfree.parameters.Messages;
import by.citech.handsfree.parameters.Tags;

public class CallHandshake implements
        IRx<String>, ITx<String>,
        CallFsm.ICallFsmReporter,
        CallFsm.ICallFsmListener {

    private static final String TAG = Tags.CallHandshake;

    private IRx<String> receiver;

    //--------------------- singleton

    private static volatile CallHandshake instance = null;

    private CallHandshake() {
    }

    public static CallHandshake getInstance() {
        if (instance == null) {
            synchronized (CallHandshake.class) {
                if (instance == null) {instance = new CallHandshake();}}}
        return instance;
    }

    //--------------------- receiver

    @Override
    public void onRx(String received) {
        ECallState state = getCallFsmState();
        if (state == ECallState.ST_OutConnected
                && received.matches(Messages.RESPONSE_ACCEPT))
            reportToCallFsm(ECallReport.RP_OutAcceptedRemote, state, TAG);
    }

    @Override
    public void onRxFinished() {}

    //--------------------- transmitter

    @Override
    public void registerRx(IRx<String> receiver) {this.receiver = receiver;}
    @Override
    public void unregisterRx(IRx<String> receiver) {this.receiver = null;}

    //--------------------- ICallFsmReporter

    @Override
    public void onFsmStateChange(ECallState from, ECallState to, ECallReport report) {
        if (report == ECallReport.RP_InAcceptedLocal) sendResponseAccept();
    }

    //--------------------- responses

    private void sendResponseAccept() {
        send(Messages.RESPONSE_ACCEPT);
    }

    private void send(String message) {
        if (receiver != null) receiver.onRx(message);
    }

}
