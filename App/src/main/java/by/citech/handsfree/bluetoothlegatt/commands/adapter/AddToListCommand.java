package by.citech.handsfree.bluetoothlegatt.commands.adapter;

import android.bluetooth.BluetoothDevice;

import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

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
        controlAdapter.addConnectDeviceToList(device);
    }

    @Override
    public void undo() {

    }
}
