package by.citech.handsfree.bluetoothlegatt.commands.blecommands.scanner;

import by.citech.handsfree.bluetoothlegatt.LeScanner;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class ScanOffCommand implements Command {
    private LeScanner leScanner;

    public ScanOffCommand(LeScanner leScanner) {
        this.leScanner = leScanner;
    }

    @Override
    public void execute() {
        leScanner.stopScanBluetoothDevice();
    }

    @Override
    public void undo() {

    }
}
