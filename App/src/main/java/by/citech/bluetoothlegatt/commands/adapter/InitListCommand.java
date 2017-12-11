package by.citech.bluetoothlegatt.commands.adapter;

import by.citech.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class InitListCommand implements Command {
    private ControlAdapter controlAdapter;

    public InitListCommand(ControlAdapter controlAdapter) {
        this.controlAdapter = controlAdapter;
    }

    @Override
    public void execute() {
        controlAdapter.initializeListBluetoothDevice();
    }

    @Override
    public void undo() {

    }
}
