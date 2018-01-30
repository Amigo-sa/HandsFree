package by.citech.handsfree.fsm;

import java.util.EnumSet;

public interface IFsmState<S extends Enum<S>> {
    String getName();
    EnumSet<S> available();
    EnumSet<S> availableFromAny();
}
