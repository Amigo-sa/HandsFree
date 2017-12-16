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

    public ClearConnectDeviceFromAdapterCommand(ControlAdapter controlAdapter) {
        this.controlAdapter = controlAdapter;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public void execute() {
        //if (Settings.debug) Log.i("LIST", "Device set to null connected set to false");
        controlAdapter.setBTDevice(null);
        controlAdapter.setConnected(false);
    }

    @Override
    public void undo() {

    }
}
