package by.citech.handsfree.bluetoothlegatt.commands.receiver;

import by.citech.handsfree.bluetoothlegatt.IReceive;
import by.citech.handsfree.bluetoothlegatt.LeBroadcastReceiver;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 11.12.2017.
 */

public class RegisterReceiverCommand implements Command {
    private IReceive iReceive;
    private ConnectorBluetooth connectorBluetooth;

    public RegisterReceiverCommand(IReceive iReceive, ConnectorBluetooth connectorBluetooth) {
        this.iReceive = iReceive;
        this.connectorBluetooth = connectorBluetooth;
    }

    @Override
    public void execute() {
        iReceive.registerReceiver(connectorBluetooth.getBroadcastReceiver(), LeBroadcastReceiver.makeGattUpdateIntentFilter());
    }

    @Override
    public void undo() {

    }
}
