package by.citech.handsfree.bluetoothlegatt.commands.characteristics;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.bluetoothlegatt.rwdata.Characteristics;

/**
 * Created by tretyak on 11.12.2017.
 */

public class CharacteristicsDisplayOnCommand implements Command {

    private Characteristics characteristics;
    private BluetoothLeService bluetoothLeService;

    public CharacteristicsDisplayOnCommand(Characteristics characteristics) {
        this.characteristics = characteristics;
    }

    public void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    @Override
    public void execute() {
        characteristics.displayGattServices(bluetoothLeService.getSupportedGattServices());
    }

    @Override
    public void undo() {

    }
}
