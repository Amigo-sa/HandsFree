package by.citech.bluetoothlegatt.commands.adapter;

import by.citech.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.bluetoothlegatt.commands.Command;

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
