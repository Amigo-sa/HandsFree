package by.citech.handsfree.call.fsm;

public interface ICallFsmListener {
    void onCallerStateChange(ECallState from, ECallState to, ECallReport why);
}
