package by.citech.handsfree.bluetoothlegatt.commands.dataexchange;

import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.bluetoothlegatt.rwdata.LeDataTransmitter;

/**
 * Created by tretyak on 06.12.2017.
 */

public class ReceiveDataOn implements Command {
    private LeDataTransmitter leDataTransmitter;

    public ReceiveDataOn(LeDataTransmitter leDataTransmitter) {
        this.leDataTransmitter = leDataTransmitter;
    }

    @Override
    public void execute() {
        leDataTransmitter.onlyReceiveData();
    }

    @Override
    public void undo() {

    }
}
