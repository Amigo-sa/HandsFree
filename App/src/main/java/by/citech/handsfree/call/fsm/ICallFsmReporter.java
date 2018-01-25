package by.citech.handsfree.call.fsm;

public interface ICallFsmReporter {

    default ECallState getCallerFsmState() {
        return CallFsm.getInstance().getState();
    }

    default boolean reportToCallerFsm(ECallState fromWhichState, ECallReport whatHappened, String fromWho) {
        return CallFsm.getInstance().processReport(whatHappened, fromWhichState, fromWho);
    }

}
