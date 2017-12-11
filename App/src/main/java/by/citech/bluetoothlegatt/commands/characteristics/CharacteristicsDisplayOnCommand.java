package by.citech.bluetoothlegatt.commands.characteristics;

import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.bluetoothlegatt.commands.Command;
import by.citech.bluetoothlegatt.rwdata.Characteristics;

/**
 * Created by tretyak on 11.12.2017.
 */

public class CharacteristicsDisplayOnCommand implements Command {

    private Characteristics characteristics;
    private BluetoothLeService bluetoothLeService;

    public CharacteristicsDisplayOnCommand(Characteristics characteristics, BluetoothLeService bluetoothLeService) {
        this.characteristics = characteristics;
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
