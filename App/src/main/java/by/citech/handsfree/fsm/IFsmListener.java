package by.citech.handsfree.fsm;

public interface IFsmListener {
    void onFsmStateChange(IFsmState from, IFsmState to, IFsmReport report);
}
