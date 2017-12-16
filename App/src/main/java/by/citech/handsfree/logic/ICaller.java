package by.citech.handsfree.logic;

public interface ICaller {

    default boolean setCallerState(CallerState fromCallerState, CallerState toCallerState) {
        return Caller.getInstance().setState(fromCallerState, toCallerState);
    }

    default CallerState getCallerState() {
        return Caller.getInstance().getCallerState();
    }

    default String getCallerStateName() {
        return Caller.getInstance().getCallerState().getName();
    }

}
