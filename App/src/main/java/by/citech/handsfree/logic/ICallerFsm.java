package by.citech.handsfree.logic;

public interface ICallerFsm {

    default ECallerState getCallerFsmState() {
        return CallerFsm.getInstance().getState();
    }

    default boolean reportToCallerFsm(ECallerState fromWhichState, ECallReport whatHappened, String fromWho) {
        return CallerFsm.getInstance().processReport(whatHappened, fromWhichState, fromWho);
    }

}
