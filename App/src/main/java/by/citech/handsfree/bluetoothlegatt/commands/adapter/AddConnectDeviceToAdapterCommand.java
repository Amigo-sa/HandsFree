package by.citech.handsfree.bluetoothlegatt.commands.adapter;

import android.bluetooth.BluetoothDevice;

import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 10.12.2017.
 */

public class AddConnectDeviceToAdapterCommand implements Command {
    private ControlAdapter controlAdapter;
    private BluetoothDevice bluetoothDevice;

    public AddConnectDeviceToAdapterCommand(ControlAdapter controlAdapter) {
        this.controlAdapter = controlAdapter;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public void execute() {
        controlAdapter.setBTDevice(bluetoothDevice);
        controlAdapter.setConnected(true);
    }

    @Override
    public void undo() {

    }
}
