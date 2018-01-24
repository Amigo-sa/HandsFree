package by.citech.handsfree.connection.fsm;

import android.support.annotation.CallSuper;

public interface IConnectionFsmReporter {

    @CallSuper
    default EConnectionState getConnectionFsmState() {
        return ConnectionFsm.getInstance().getState();
    }

    @CallSuper
    default boolean reportToConnectionFsm(EConnectionState fromWhichState, EConnectionReport whatHappened, String fromWho) {
        return ConnectionFsm.getInstance().processReport(whatHappened, fromWhichState, fromWho);
    }

}
