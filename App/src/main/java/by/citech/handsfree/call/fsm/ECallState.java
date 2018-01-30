package by.citech.handsfree.call.fsm;

import java.util.EnumSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.eCopy;
import static by.citech.handsfree.util.CollectionHelper.eSet;

public enum ECallState implements IFsmState<ECallState> {

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

    static {
        availableFromAny = s(ST_TurnedOff, ST_Failure);
        ST_TurnedOff   .a(ST_TurnedOn);
        ST_TurnedOn    .a(ST_NetReady, ST_BtReady);
        ST_NetReady    .a(ST_TurnedOn, ST_BtReady, ST_Ready);
        ST_BtReady     .a(ST_TurnedOn, ST_NetReady, ST_Ready);
        ST_Ready       .a(ST_NetReady, ST_BtReady, ST_OutStarted, ST_InConnected);
        ST_OutStarted  .a(ST_Ready, ST_NetReady, ST_BtReady, ST_OutConnected);
        ST_OutConnected.a(ST_Ready, ST_NetReady, ST_BtReady, ST_Call);
        ST_InConnected .a(ST_Ready, ST_NetReady, ST_BtReady, ST_Call);
        ST_Call        .a(ST_Ready, ST_NetReady, ST_BtReady);
        ST_Failure     .a(ST_TurnedOn);
    }

    //--------------------- constructor

    ECallState(ECallState... states) {a(states);}
    private static EnumSet<ECallState> availableFromAny;
    private EnumSet<ECallState> available;
    void a(ECallState... states) {available = s(states);}

    //--------------------- IFsmState

    @Override public String getName() {return this.name();}
    @Override public EnumSet<ECallState> available() {return c(available);}
    @Override public EnumSet<ECallState> availableFromAny() {return c(availableFromAny);}

    //--------------------- additional

    private static EnumSet<ECallState> s(ECallState... states) {return eSet(ECallState.class, states);}
    private static EnumSet<ECallState> c(EnumSet<ECallState> set) {return eCopy(set);}

}
