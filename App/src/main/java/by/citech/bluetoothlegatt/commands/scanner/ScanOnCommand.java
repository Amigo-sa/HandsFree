package by.citech.bluetoothlegatt.commands.scanner;

import by.citech.bluetoothlegatt.LeScanner;
import by.citech.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class ScanOnCommand implements Command {
    private LeScanner leScanner;

    public ScanOnCommand(LeScanner leScanner){
        this.leScanner = leScanner;
    }

    @Override
    public void execute() {
        leScanner.startScanBluetoothDevice();
    }

    @Override
    public void undo() {

    }
}
