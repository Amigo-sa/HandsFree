package by.citech.bluetoothlegatt.commands.button;

import by.citech.bluetoothlegatt.commands.Command;
import by.citech.logic.IBluetoothListener;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ButtonChangeViewOffCommand implements Command {
    private IBluetoothListener iBluetoothListener;

    public ButtonChangeViewOffCommand(IBluetoothListener iBluetoothListener) {
        this.iBluetoothListener = iBluetoothListener;
    }

    @Override
    public void execute() {
        iBluetoothListener.withoutDeviceView();
    }

    @Override
    public void undo() {

    }
}
