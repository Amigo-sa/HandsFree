package by.citech.handsfree.logic;

public interface ICallerFsm {

    default CallerState getCallerFsmState() {
        return CallerFsm.getInstance().getState();
    }

    default boolean reportToCallerFsm(CallerState fromWhichState, ECallReport whatHappened, String fromWho) {
        return CallerFsm.getInstance().processReport(whatHappened, fromWhichState, fromWho);
    }

}
