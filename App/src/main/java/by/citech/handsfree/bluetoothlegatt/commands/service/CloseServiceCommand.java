package by.citech.handsfree.bluetoothlegatt.commands.service;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class CloseServiceCommand implements Command {
    private BluetoothLeService bluetoothLeService;

    public CloseServiceCommand(BluetoothLeService bluetoothLeService) {
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