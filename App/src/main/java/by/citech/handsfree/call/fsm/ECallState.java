package by.citech.handsfree.call.fsm;

import java.util.HashSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.s;

public enum ECallState implements IFsmState {

    ST_TurnedOff,
    ST_TurnedOn,
    ST_NetReady,
    ST_BtReady,
    ST_Ready,
    ST_OutStarted,
    ST_OutConnected,
    ST_InConnected,
    ST_Call,
    ST_Failure;

    public String getName() {return this.name();}
    @Override public HashSet<IFsmState> available(){return availableSt(this);};
    @Override public HashSet<IFsmState> availableFromAny() {return s(ST_TurnedOff, ST_Failure);}

    public static HashSet<IFsmState> availableSt(ECallState state) {
        switch (state) {
            case ST_TurnedOff:
                return s(ST_TurnedOn);
            case ST_TurnedOn:
                return s(ST_NetReady, ST_BtReady);
            case ST_NetReady:
                return s(ST_TurnedOn, ST_BtReady, ST_Ready);
            case ST_BtReady:
                return s(ST_TurnedOn, ST_NetReady, ST_Ready);
            case ST_Ready:
                return s(ST_NetReady, ST_BtReady, ST_OutStarted, ST_InConnected);
            case ST_OutStarted:
                return s(ST_Ready, ST_NetReady, ST_BtReady, ST_OutConnected);
            case ST_OutConnected:
                return s(ST_Ready, ST_NetReady, ST_BtReady, ST_Call);
            case ST_InConnected:
                return s(ST_Ready, ST_NetReady, ST_BtReady, ST_Call);
            case ST_Call:
                return s(ST_Ready, ST_NetReady, ST_BtReady);
            case ST_Failure:
                return s(ST_TurnedOn);
            default:
                return s();
        }
    }

}
