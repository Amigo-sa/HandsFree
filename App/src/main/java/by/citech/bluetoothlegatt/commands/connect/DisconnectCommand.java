package by.citech.bluetoothlegatt.commands.connect;

import by.citech.bluetoothlegatt.LeConnector;
import by.citech.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class DisconnectCommand implements Command {
    private LeConnector leConnector;

    public DisconnectCommand(LeConnector leConnector) {
        this.leConnector = leConnector;
    }

    @Override
    public void execute() {
        leConnector.disconnectBTDevice();
    }

    @Override
    public void undo() {

    }
}
