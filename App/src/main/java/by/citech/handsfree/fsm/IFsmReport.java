package by.citech.handsfree.fsm;

public interface IFsmReport<S extends Enum<S> & IFsmState<S>> {
    S getDestination();
}

