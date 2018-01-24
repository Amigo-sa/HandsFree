package by.citech.handsfree.bluetoothlegatt.commands.connect;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeCore;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class DisconnectCommand implements Command {
    private BluetoothLeCore mBluetoothLeService;

    public DisconnectCommand() {

    }

    public void setmBluetoothLeService(BluetoothLeCore mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    @Override
    public void execute() {
        // производим отключение от устройства
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
        }
    }

    @Override
    public void undo() {

    }
}
