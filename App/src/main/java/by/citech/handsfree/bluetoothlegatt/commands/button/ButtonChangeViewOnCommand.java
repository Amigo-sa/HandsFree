package by.citech.handsfree.bluetoothlegatt.commands.button;

import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.bluetoothlegatt.IBluetoothListener;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ButtonChangeViewOnCommand implements Command {
    private IBluetoothListener iBluetoothListener;

    public ButtonChangeViewOnCommand() {
    }

    public void setiBluetoothListener(IBluetoothListener iBluetoothListener) {
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
