package by.citech.bluetoothlegatt.commands.receiver;

import by.citech.bluetoothlegatt.IReceive;
import by.citech.bluetoothlegatt.LeBroadcastReceiver;
import by.citech.bluetoothlegatt.commands.Command;
import by.citech.logic.ConnectorBluetooth;

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
