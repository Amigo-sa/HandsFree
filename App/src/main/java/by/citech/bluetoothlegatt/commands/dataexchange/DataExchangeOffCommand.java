package by.citech.bluetoothlegatt.commands.dataexchange;

import by.citech.bluetoothlegatt.commands.Command;
import by.citech.bluetoothlegatt.rwdata.LeDataTransmitter;

/**
 * Created by tretyak on 06.12.2017.
 */

public class DataExchangeOffCommand implements Command {
    private LeDataTransmitter leDataTransmitter;

    public DataExchangeOffCommand(LeDataTransmitter leDataTransmitter) {
        this.leDataTransmitter = leDataTransmitter;
    }

    @Override
    public void execute() {
        leDataTransmitter.disableTransmitData();
    }

    @Override
    public void undo() {

    }
}
