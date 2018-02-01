package by.citech.handsfree.network.fsm;

import java.util.EnumSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.eCopy;
import static by.citech.handsfree.util.CollectionHelper.eSet;

public enum ENetState implements IFsmState<ENetState> {

    ST_TurnedOff,
    ST_TurnedOn,
    ST_NetPrepareFail,
    ST_NetPrepared,
    ST_Connecting,
    ST_Disconnecting,
    ST_Connected,
    ST_Exchange,
    ST_Failure;

    static {
        availableFromAny = s(ST_Failure, ST_TurnedOff);
        ST_TurnedOff     .a(ST_TurnedOn);
        ST_TurnedOn      .a(ST_NetPrepareFail, ST_NetPrepared);
        ST_NetPrepareFail.a(ST_NetPrepared);
        ST_NetPrepared   .a(ST_Connecting);
        ST_Connecting    .a(ST_Connecting, ST_Disconnecting, ST_Connected, ST_NetPrepared);
        ST_Disconnecting .a(ST_NetPrepared);
        ST_Connected     .a(ST_NetPrepared, ST_Disconnecting, ST_Exchange);
        ST_Exchange      .a(ST_NetPrepared, ST_Disconnecting, ST_Connected);
        ST_Failure       .a();
    }

    //--------------------- constructor

    ENetState(ENetState... states) {a(states);}
    private static EnumSet<ENetState> availableFromAny;
    private EnumSet<ENetState> available;
    void a(ENetState... states) {available = s(states);}

    //--------------------- IFsmState

    @Override public String getName() {return this.name();}
    @Override public EnumSet<ENetState> available() {return c(available);}
    @Override public EnumSet<ENetState> availableFromAny() {return c(availableFromAny);}

    //--------------------- additional

    private static EnumSet<ENetState> s(ENetState... states) {return eSet(ENetState.class, states);}
    private static EnumSet<ENetState> c(EnumSet<ENetState> set) {return eCopy(set);}

}
