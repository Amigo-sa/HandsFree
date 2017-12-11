package by.citech.bluetoothlegatt.commands.dialods;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.R;
import by.citech.bluetoothlegatt.commands.Command;
import by.citech.logic.ConnectorBluetooth;
import by.citech.param.Settings;

/**
 * Created by tretyak on 07.12.2017.
 */

public class ConnectDialogCommand implements Command {
    private AlertDialog.Builder adb;
    private BluetoothDevice device;
    private ConnectorBluetooth connectorBluetooth;
    private AlertDialog alertDialog;

    public ConnectDialogCommand(AlertDialog.Builder adb, BluetoothDevice device, AlertDialog alertDialog, ConnectorBluetooth connectorBluetooth) {
        this.adb = adb;
        this.device = device;
        this.alertDialog = alertDialog;
        this.connectorBluetooth = connectorBluetooth;
    }

    @Override
    public void execute() {
        adb.setTitle(device.getName())
                .setMessage(R.string.connect_message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, (dialog, identifier) -> {
                    connectorBluetooth.disconnect();
                    dialog.cancel();
                });
        alertDialog = adb.create();
        alertDialog.show();
    }

    public void undo() {
        if (alertDialog != null)
            alertDialog.dismiss();
    }

}
