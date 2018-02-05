package by.citech.handsfree.bluetoothlegatt.fsm;

import java.util.EnumSet;

import by.citech.handsfree.fsm.IFsmState;

import static by.citech.handsfree.util.CollectionHelper.eCopy;
import static by.citech.handsfree.util.CollectionHelper.eSet;

public enum EBtState implements IFsmState<EBtState> {

//  ST_DeviceChosen,
//  ST_DeviceNotChosen,
//  ST_Searching,
//  ST_Found,
//  ST_Disconnected,
//  ST_ExchangeDisabling,
//  ST_ExchangeEnabling,
//  ST_BtDisabled,
//  ST_BtEnabling,
    ST_TurnedOff,
    ST_TurnedOn,
    ST_BtPrepared,
    ST_BtPrepareFail,
    ST_BtEnabled,
    ST_Connecting,
    ST_Disconnecting,
    ST_Incompatible,
    ST_Connected,
    ST_Exchange,
    ST_Failure;

    static {
        availableFromAny = s(ST_Failure, ST_TurnedOff);
//      ST_DeviceChosen      .a(ST_DeviceChosen, ST_Connecting);
//      ST_DeviceNotChosen   .a(ST_Connecting);
//      ST_Searching         .a(ST_DeviceChosen, ST_Found);
//      ST_Found             .a(ST_Searching, ST_Connecting);
//      ST_Disconnected      .a(ST_Searching, ST_Connecting, ST_DeviceChosen);
//      ST_ExchangeEnabling  .a(ST_BtEnabled, ST_Disconnecting, ST_Connected, ST_ExchangeEnabling, ST_Exchange);
//      ST_ExchangeDisabling .a(ST_BtEnabled, ST_Disconnecting, ST_Connected, ST_ExchangeDisabling);
//      ST_BtDisabled        .a(ST_BtEnabling);
//      ST_BtEnabling        .a(ST_BtEnabling, ST_BtEnabled, ST_BtPrepared);
        ST_TurnedOff         .a(ST_TurnedOn);
        ST_TurnedOn          .a(ST_BtPrepared, ST_BtPrepareFail);
        ST_BtPrepared        .a(ST_BtEnabled);
        ST_BtPrepareFail     .a();
        ST_BtEnabled         .a(ST_Connecting);
        ST_Connecting        .a(ST_BtEnabled, ST_Incompatible, ST_Connected);
        ST_Disconnecting     .a(ST_BtEnabled, ST_Disconnecting);
        ST_Incompatible      .a(ST_BtEnabled, ST_Disconnecting);
        ST_Connected         .a(ST_BtEnabled, ST_Disconnecting, ST_Exchange);
        ST_Exchange          .a(ST_BtEnabled, ST_Disconnecting);
        ST_Failure           .a();
    }

    //--------------------- constructor

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
