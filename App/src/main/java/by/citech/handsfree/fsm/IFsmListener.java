package by.citech.handsfree.fsm;

public interface IFsmListener<
        R extends Enum<R> & IFsmReport<S>,
        S extends Enum<S> & IFsmState<S>> {
    void onFsmStateChange(S from, S to, R report);
}
