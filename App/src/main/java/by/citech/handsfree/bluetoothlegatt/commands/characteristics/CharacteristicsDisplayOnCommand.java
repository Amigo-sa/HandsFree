package by.citech.handsfree.bluetoothlegatt.commands.characteristics;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeCore;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.bluetoothlegatt.rwdata.Characteristics;

/**
 * Created by tretyak on 11.12.2017.
 */

public class CharacteristicsDisplayOnCommand implements Command {

    private Characteristics characteristics;
    private BluetoothLeCore bluetoothLeService;

    public CharacteristicsDisplayOnCommand(Characteristics characteristics) {
        this.characteristics = characteristics;
    }

    public void setBluetoothLeService(BluetoothLeCore bluetoothLeService) {
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
