package by.citech.bluetoothlegatt.commands.dialods;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;

import java.util.Timer;
import java.util.TimerTask;

import by.citech.R;
import by.citech.bluetoothlegatt.IVisible;
import by.citech.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 08.12.2017.
 */

public class DisconnInfoDialogCommand implements Command {
    private AlertDialog.Builder adb;
    private BluetoothDevice device;
    private IVisible iVisible;


    public DisconnInfoDialogCommand(AlertDialog.Builder adb, BluetoothDevice device, IVisible iVisible) {
        this.adb = adb;
        this.device = device;
        this.iVisible = iVisible;

    }

    @Override
    public void execute() {
        adb.setTitle(device.getName())
                .setMessage(R.string.disconnected_message)
                .setIcon(android.R.drawable.ic_lock_power_off);
                //.setCancelable(true);

        final AlertDialog alertDialog = adb.create();
        alertDialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                alertDialog.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                iVisible.setVisibleList();
            }
        }, 2000);
    }

    @Override
    public void undo() {

    }
}
