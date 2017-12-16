package by.citech.handsfree.bluetoothlegatt.commands.receiver;

import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.bluetoothlegatt.LeBroadcastReceiver;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 11.12.2017.
 */

public class RegisterReceiverCommand implements Command {
    private IBroadcastReceiver iBroadcastReceiver;
    private ConnectorBluetooth connectorBluetooth;

    public RegisterReceiverCommand(ConnectorBluetooth connectorBluetooth) {
        this.connectorBluetooth = connectorBluetooth;
    }

    public void setiBroadcastReceiver(IBroadcastReceiver iBroadcastReceiver) {
        this.iBroadcastReceiver = iBroadcastReceiver;
    }

    @Override
    public void execute() {
        iBroadcastReceiver.registerReceiver(connectorBluetooth.getBroadcastReceiver(), LeBroadcastReceiver.makeGattUpdateIntentFilter());
    }

    @Override
    public void undo() {

    }




}
