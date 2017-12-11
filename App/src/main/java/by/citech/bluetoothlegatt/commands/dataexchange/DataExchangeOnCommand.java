package by.citech.bluetoothlegatt.commands.dataexchange;

import by.citech.bluetoothlegatt.commands.Command;
import by.citech.bluetoothlegatt.rwdata.LeDataTransmitter;

/**
 * Created by tretyak on 06.12.2017.
 */

public class DataExchangeOnCommand implements Command {
    private LeDataTransmitter leDataTransmitter;

    public DataExchangeOnCommand(LeDataTransmitter leDataTransmitter) {
        this.leDataTransmitter = leDataTransmitter;
    }

    @Override
    public void execute() {
        leDataTransmitter.enableTransmitData();
    }

    @Override
    public void undo() {

    }
}
