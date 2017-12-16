package by.citech.handsfree.bluetoothlegatt.commands.connect;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class DisconnectCommand implements Command {
    private BluetoothLeService mBluetoothLeService;

    public DisconnectCommand() {

    }

    public void setmBluetoothLeService(BluetoothLeService mBluetoothLeService) {
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
