package by.citech.bluetoothlegatt.commands.dialods;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;

import by.citech.R;
import by.citech.bluetoothlegatt.commands.Command;
import by.citech.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ReconnectDialogCommand implements Command {
    private AlertDialog.Builder adb;
    private BluetoothDevice device;
    private ConnectorBluetooth connectorBluetooth;
    private AlertDialog alertDialog;

    public ReconnectDialogCommand(AlertDialog.Builder adb, BluetoothDevice device, AlertDialog alertDialog, ConnectorBluetooth connectorBluetooth) {
        this.adb = adb;
        this.device = device;
        this.alertDialog = alertDialog;
        this.connectorBluetooth = connectorBluetooth;
    }

    @Override
    public void execute() {
        adb.setTitle(device.getName());
        adb.setMessage(R.string.click_other_device_message);
        adb.setIcon(android.R.drawable.ic_dialog_info);
        adb.setPositiveButton(R.string.connect, (dialog, which) -> {
            connectorBluetooth.disconnect();
            connectorBluetooth.connecting();
            dialog.dismiss();
        });
        adb.setNegativeButton(R.string.cancel, (dialog, identifier) -> dialog.dismiss());
        alertDialog = adb.create();
        alertDialog.show();
    }

    @Override
    public void undo() {

    }
}
