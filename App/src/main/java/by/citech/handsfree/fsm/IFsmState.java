package by.citech.handsfree.fsm;

import java.util.HashSet;

public interface IFsmState {
    String getName();
    HashSet<IFsmState> available();
    HashSet<IFsmState> availableFromAny();
}
