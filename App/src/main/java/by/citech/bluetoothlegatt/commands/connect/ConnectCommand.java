package by.citech.bluetoothlegatt.commands.connect;

import by.citech.bluetoothlegatt.LeConnector;
import by.citech.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class ConnectCommand implements Command {
    private LeConnector leConnector;

    public ConnectCommand(LeConnector leConnector) {
        this.leConnector = leConnector;
    }

    @Override
    public void execute() {
        leConnector.onConnectBTDevice();
    }

    @Override
    public void undo() {

    }
}
