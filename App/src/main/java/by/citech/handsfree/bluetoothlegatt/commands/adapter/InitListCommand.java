package by.citech.handsfree.bluetoothlegatt.commands.adapter;

import by.citech.handsfree.bluetoothlegatt.adapters.ControlAdapter;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

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
