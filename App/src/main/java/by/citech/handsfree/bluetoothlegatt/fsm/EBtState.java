package by.citech.handsfree.bluetoothlegatt.fsm;

import java.util.EnumSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.eCopy;
import static by.citech.handsfree.util.CollectionHelper.eSet;

public enum EBtState implements IFsmState<EBtState> {

    ST_TurnedOff,
    ST_TurnedOn,
    ST_BtPrepared,
    ST_BtPrepareFail,
    ST_BtEnabling,
    ST_BtEnabled,
    ST_BtDisabled,
    ST_DeviceChosen,
    ST_DeviceNotChosen,
    ST_Searching,
    ST_Found,
    ST_Connecting,
    ST_Disconnecting,
    ST_Disconnected,
    ST_Incompatible,
    ST_Connected,
    ST_ExchangeDisabling,
    ST_ExchangeEnabling,
    ST_ExchangeEnabled,
    ST_Failure;

    static {
        availableFromAny = s(ST_Failure, ST_TurnedOff);
        ST_TurnedOff        .a(ST_TurnedOn);
        ST_TurnedOn         .a(ST_BtPrepared, ST_BtPrepareFail);
        ST_BtPrepared       .a(ST_BtEnabling);
        ST_BtPrepareFail    .a();
        ST_BtEnabling       .a(ST_BtEnabling, ST_BtEnabled, ST_BtDisabled);
        ST_BtEnabled        .a(ST_DeviceChosen, ST_DeviceNotChosen);
        ST_BtDisabled       .a(ST_BtEnabling);
        ST_DeviceChosen     .a(ST_DeviceChosen);
        ST_DeviceNotChosen  .a(ST_DeviceChosen);
        ST_Searching        .a(ST_DeviceChosen, ST_Found);
        ST_Found            .a(ST_Searching, ST_Connecting);
        ST_Connecting       .a(ST_Disconnected, ST_Incompatible, ST_Connected);
        ST_Disconnecting    .a(ST_Disconnected, ST_Disconnecting, ST_Searching);
        ST_Disconnected     .a(ST_Searching, ST_Connecting);
        ST_Incompatible     .a(ST_DeviceChosen);
        ST_Connected        .a(ST_Disconnected, ST_Disconnecting, ST_ExchangeEnabling);
        ST_ExchangeEnabling .a(ST_Disconnected, ST_Disconnecting, ST_Connected, ST_ExchangeEnabling, ST_ExchangeEnabled);
        ST_ExchangeDisabling.a(ST_Disconnected, ST_Disconnecting, ST_Connected, ST_ExchangeDisabling);
        ST_ExchangeEnabled  .a(ST_Disconnected, ST_Disconnecting, ST_Connected, ST_ExchangeDisabling);
        ST_Failure          .a();
    }

    //--------------------- constructor

    EBtState(EBtState... states) {a(states);}
    private static EnumSet<EBtState> availableFromAny;
    private EnumSet<EBtState> available;
    void a(EBtState... states) {available = s(states);}

    //--------------------- IFsmState

    @Override public String getName() {return this.name();}
    @Override public EnumSet<EBtState> available() {return c(available);}
    @Override public EnumSet<EBtState> availableFromAny() {return c(availableFromAny);}

    //--------------------- additional

    private static EnumSet<EBtState> s(EBtState... states) {return eSet(EBtState.class, states);}
    private static EnumSet<EBtState> c(EnumSet<EBtState> set) {return eCopy(set);}

}
