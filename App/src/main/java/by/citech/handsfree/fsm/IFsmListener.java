package by.citech.handsfree.fsm;

public interface IFsmListener<S extends IFsmState, R extends IFsmReport> {
    void onFsmStateChange(S from, S to, R report);
}
