package by.citech.handsfree.bluetoothlegatt.fsm;

import java.util.HashSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.s;

public enum EBtState implements IFsmState {

    StateTurnedOff,
    StateTurnedOn,
    StateBtPrepared,
    StateBtPrepareFail,
    StateBtEnabling,
    StateBtEnabled,
    StateBtDisabled,
    StateDeviceChosen,
    StateDeviceNotChosen,
    StateSearching,
    StateFound,
    StateConnecting,
    StateDisconnecting,
    StateDisconnected,
    StateIncompatible,
    StateConnected,
    StateExchangeDisabling,
    StateExchangeEnabling,
    StateExchangeEnabled,
    Failure;

    @Override public String getName() {return this.name();}
    @Override public HashSet<IFsmState> available() {return availableSt(this);}
    @Override public HashSet<IFsmState> availableFromAny() {return s(Failure, StateTurnedOff);}

    public static HashSet<IFsmState> availableSt(EBtState state) {
        switch (state) {
            case StateTurnedOff:
                return s(StateTurnedOn);
            case StateTurnedOn:
                return s(StateBtPrepared, StateBtPrepareFail);
            case StateBtPrepared:
                return s(StateBtEnabling);
            case StateBtPrepareFail:
                return s();
            case StateBtEnabling:
                return s(StateBtEnabling, StateBtEnabled, StateBtDisabled);
            case StateBtEnabled:
                return s(StateDeviceChosen, StateDeviceNotChosen);
            case StateBtDisabled:
                return s(StateBtEnabling);
            case StateDeviceChosen:
                return s(StateDeviceChosen);
            case StateDeviceNotChosen:
                return s(StateDeviceChosen);
            case StateSearching:
                return s(StateDeviceChosen, StateFound);
            case StateFound:
                return s(StateSearching, StateConnecting);
            case StateConnecting:
                return s(StateDisconnected, StateIncompatible, StateConnected);
            case StateIncompatible:
                return s(StateDeviceChosen);
            case StateDisconnected:
                return s(StateSearching, StateConnecting);
            case StateConnected:
                return s(StateDisconnected, StateDisconnecting, StateExchangeEnabling);
            case StateDisconnecting:
                return s(StateDisconnected, StateDisconnecting, StateSearching);
            case StateExchangeEnabling:
                return s(StateDisconnected, StateDisconnecting, StateConnected, StateExchangeEnabling, StateExchangeEnabled);
            case StateExchangeEnabled:
                return s(StateDisconnected, StateDisconnecting, StateConnected, StateExchangeDisabling);
            case StateExchangeDisabling:
                return s(StateDisconnected, StateDisconnecting, StateConnected, StateExchangeDisabling);
            case Failure:
                return s();
            default:
                return s();
        }
    }

}
