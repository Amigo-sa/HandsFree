package by.citech.handsfree.bluetoothlegatt.commands.adapter;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.settings.Settings;

/**
 * Created by tretyak on 06.12.2017.
 */

public class InitListCommand implements Command {
    private ControlAdapter controlAdapter;
    private BluetoothDevice device;

    public InitListCommand(ControlAdapter controlAdapter) {
        this.controlAdapter = controlAdapter;
    }

    public void setDevice(BluetoothDevice device) {
        if (Settings.debug) Log.w("InitListCommand", "set init device = " + device);
        this.device = device;
    }

    @Override
    public void execute() {
        if (Settings.debug) Log.w("InitListCommand", "initialize command device = " + device);
        controlAdapter.initializeListBluetoothDevice(device);
        device = null;
    }

    @Override
    public void undo() {

    }
}
