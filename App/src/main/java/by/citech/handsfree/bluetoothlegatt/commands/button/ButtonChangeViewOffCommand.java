package by.citech.handsfree.bluetoothlegatt.commands.button;

import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.bluetoothlegatt.IBluetoothListener;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ButtonChangeViewOffCommand implements Command {
    private IBluetoothListener iBluetoothListener;

    public ButtonChangeViewOffCommand() {
    }

    public void setBluetoothListener(IBluetoothListener iBluetoothListener) {
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
