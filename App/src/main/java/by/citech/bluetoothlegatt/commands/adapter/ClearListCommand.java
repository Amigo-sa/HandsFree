package by.citech.bluetoothlegatt.commands.adapter;

import by.citech.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class ClearListCommand implements Command {
    private ControlAdapter controlAdapter;

    public ClearListCommand(ControlAdapter controlAdapter) {
        this.controlAdapter = controlAdapter;
    }

    @Override
    public void execute() {
        controlAdapter.clearAllDevicesFromList();
    }

    @Override
    public void undo() {

    }
}
