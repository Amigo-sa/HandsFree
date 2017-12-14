package by.citech.handsfree.bluetoothlegatt.commands.adapter;

import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class AddToListCommand implements Command {
    private ControlAdapter controlAdapter;

    public AddToListCommand(ControlAdapter controlAdapter) {
        this.controlAdapter = controlAdapter;
    }

    @Override
    public void execute() {
        controlAdapter.addConnectDeviceToList();
    }

    @Override
    public void undo() {

    }
}
