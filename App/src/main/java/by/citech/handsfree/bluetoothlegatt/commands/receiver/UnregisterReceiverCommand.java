package by.citech.handsfree.bluetoothlegatt.commands.receiver;

import by.citech.handsfree.bluetoothlegatt.IReceive;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.logic.ConnectorBluetooth;

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
        iReceive.unregisterReceiver(connectorBluetooth.getBroadcastReceiver());
    }

    @Override
    public void undo() {

    }
}
