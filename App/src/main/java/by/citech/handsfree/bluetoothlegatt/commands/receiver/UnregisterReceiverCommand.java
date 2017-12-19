package by.citech.handsfree.bluetoothlegatt.commands.receiver;

import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.LeBroadcastReceiver;
import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.logic.ConnectorBluetooth;
import by.citech.handsfree.settings.Settings;

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

        try {
            if (iBroadcastReceiver != null) {
                iBroadcastReceiver.unregisterReceiver(connectorBluetooth.getBroadcastReceiver());
            }
        } catch (IllegalArgumentException e) {
            if (Settings.debug) Log.e("Command", "now BroadcastReceiver are unregistered");
        }

    }

    @Override
    public void undo() {

    }
}
