package by.citech.bluetoothlegatt.commands.button;

import by.citech.bluetoothlegatt.commands.Command;
import by.citech.logic.IBluetoothListener;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ButtonChangeViewOnCommand implements Command {
    private IBluetoothListener iBluetoothListener;

    public ButtonChangeViewOnCommand(IBluetoothListener iBluetoothListener) {
        this.iBluetoothListener = iBluetoothListener;
    }

    @Override
    public void execute() {
        iBluetoothListener.withDeviceView();
    }

    @Override
    public void undo() {

    }
}
