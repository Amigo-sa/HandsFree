package by.citech.handsfree.bluetoothlegatt.commands.scanner;

import by.citech.handsfree.bluetoothlegatt.LeScanner;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

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
