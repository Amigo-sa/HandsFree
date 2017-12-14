package by.citech.handsfree.bluetoothlegatt.commands.adapter;

import android.bluetooth.BluetoothDevice;

import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 10.12.2017.
 */

public class ClearConnectDeviceFromAdapterCommand implements Command {
    private ControlAdapter controlAdapter;
    private BluetoothDevice bluetoothDevice;

    public ClearConnectDeviceFromAdapterCommand(ControlAdapter controlAdapter, BluetoothDevice bluetoothDevice) {
        this.controlAdapter = controlAdapter;
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public void execute() {
        controlAdapter.setBTDevice(bluetoothDevice);
        controlAdapter.setConnected(false);
    }

    @Override
    public void undo() {

    }
}
