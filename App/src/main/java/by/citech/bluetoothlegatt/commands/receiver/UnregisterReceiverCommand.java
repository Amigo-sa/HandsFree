package by.citech.bluetoothlegatt.commands.receiver;

import by.citech.bluetoothlegatt.IReceive;
import by.citech.bluetoothlegatt.commands.Command;
import by.citech.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 11.12.2017.
 */

public class UnregisterReceiverCommand implements Command {
    private IReceive iReceive;
    private ConnectorBluetooth connectorBluetooth;

    public UnregisterReceiverCommand(IReceive iReceive, ConnectorBluetooth connectorBluetooth) {
        this.iReceive = iReceive;
        this.connectorBluetooth = connectorBluetooth;
    }

    @Override
    public void execute() {

    }

    @Override
    public void undo() {

    }
}
