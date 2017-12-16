package by.citech.handsfree.bluetoothlegatt.commands.receiver;

import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 11.12.2017.
 */

public class UnregisterReceiverCommand implements Command {
    private IBroadcastReceiver iBroadcastReceiver;
    private ConnectorBluetooth connectorBluetooth;

    public UnregisterReceiverCommand(ConnectorBluetooth connectorBluetooth) {
        this.connectorBluetooth = connectorBluetooth;
    }

    public void setiBroadcastReceiver(IBroadcastReceiver iBroadcastReceiver) {
        this.iBroadcastReceiver = iBroadcastReceiver;
    }

    @Override
    public void execute() {
        iBroadcastReceiver.unregisterReceiver(connectorBluetooth.getBroadcastReceiver());
    }

    @Override
    public void undo() {

    }
}
