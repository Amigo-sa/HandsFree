package by.citech.handsfree.activity.fsm;

import android.support.annotation.CallSuper;

public interface IActivityFsmReporter {

    @CallSuper
    default EActivityState getActivityFsmPrevActivityState() {
        return ActivityFsm.getInstance().getPrevActivityState();
    }

    @CallSuper
    default EActivityState getActivityFsmPrevState() {
        return ActivityFsm.getInstance().getPrevState();
    }

    @CallSuper
    default EActivityState getActivityFsmCurrState() {
        return ActivityFsm.getInstance().getCurrState();
    }

    @CallSuper
    default boolean reportToActivityFsm(EActivityState fromWhichState, EActivityReport whatHappened, String fromWho) {
        return ActivityFsm.getInstance().processReport(whatHappened, fromWhichState, fromWho);
    }

}
