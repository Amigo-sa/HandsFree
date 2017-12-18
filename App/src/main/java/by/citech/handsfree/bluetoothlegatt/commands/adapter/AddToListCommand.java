package by.citech.handsfree.bluetoothlegatt.commands.adapter;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.settings.Settings;

/**
 * Created by tretyak on 06.12.2017.
 */

public class AddToListCommand implements Command {
    private ControlAdapter controlAdapter;
    private BluetoothDevice device;

    public AddToListCommand(ControlAdapter controlAdapter) {
        this.controlAdapter = controlAdapter;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public void execute() {
        if (Settings.debug) Log.w("AddToListCommand", "add device command = " + device);
        controlAdapter.addConnectDeviceToList(device);
        device = null;
    }

    @Override
    public void undo() {

    }
}
