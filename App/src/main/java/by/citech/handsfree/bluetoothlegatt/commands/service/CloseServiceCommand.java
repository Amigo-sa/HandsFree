package by.citech.handsfree.bluetoothlegatt.commands.service;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeCore;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class CloseServiceCommand implements Command {
    private BluetoothLeCore bluetoothLeService;

    public CloseServiceCommand() {
    }

    public void setBluetoothLeService(BluetoothLeCore bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    @Override
    public void execute() {
        if (bluetoothLeService != null) {
            bluetoothLeService.close();
            bluetoothLeService = null;
        }
    }

    @Override
    public void undo() {

    }

}
