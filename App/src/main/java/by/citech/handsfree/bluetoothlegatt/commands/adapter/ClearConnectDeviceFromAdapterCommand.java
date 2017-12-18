package by.citech.handsfree.bluetoothlegatt.commands.adapter;

import android.bluetooth.BluetoothDevice;

import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 10.12.2017.
 */

public class ClearConnectDeviceFromAdapterCommand implements Command {
    private ControlAdapter controlAdapter;

    public ClearConnectDeviceFromAdapterCommand(ControlAdapter controlAdapter) {
        this.controlAdapter = controlAdapter;
    }

    @Override
    public void execute() {
        controlAdapter.setConnected(false);
    }

    @Override
    public void undo() {

    }
}
