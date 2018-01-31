package by.citech.handsfree.network.fsm;

import by.citech.handsfree.fsm.IFsmReport;

import static by.citech.handsfree.network.fsm.ENetState.*;

public enum ENetReport implements IFsmReport<ENetState> {

    RP_TurningOn        (ST_TurnedOff),
    RP_TurningOff       (ST_TurnedOff),
    RP_NetPrepared      (ST_NetPrepared),
    RP_NetPrepareFail   (ST_NetPrepareFail),
    RP_NetConnectedOut  (ST_Connected),
    RP_NetConnectedIn   (ST_Connected),
    RP_NetOutFail       (ST_NetPrepared),
    RP_ConnectOut       (ST_Connecting),
    RP_Disconnect       (ST_Disconnecting),
    RP_NetDisconnected  (ST_NetPrepared),
    RP_ExchangeEnable   (ST_Exchange),
    RP_ExchangeDisable  (ST_Connected);

    private ENetState destination;
    @Override public ENetState getDestination() {return destination;}
    ENetReport(ENetState state) {this.destination = state;}

}
