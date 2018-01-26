package by.citech.handsfree.bluetoothlegatt.commands.blecommands.dataexchange;

import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.bluetoothlegatt.rwdata.LeDataTransmitter;

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
