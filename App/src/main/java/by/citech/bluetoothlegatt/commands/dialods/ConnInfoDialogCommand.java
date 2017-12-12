package by.citech.bluetoothlegatt.commands.dialods;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import by.citech.R;
import by.citech.bluetoothlegatt.IVisible;
import by.citech.bluetoothlegatt.commands.Command;
import by.citech.dialog.DialogState;
import by.citech.dialog.DialogType;
import by.citech.exchange.IMsgToUi;
import by.citech.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ConnInfoDialogCommand implements Command {
    private BluetoothDevice device;
    private IMsgToUi iMsgToUi;
    private IVisible iVisible;

    public ConnInfoDialogCommand(BluetoothDevice device, IMsgToUi iMsgToUi, IVisible iVisible) {
        this.device = device;
        this.iMsgToUi = iMsgToUi;
        this.iVisible = iVisible;
    }

    @Override
    public void execute() {
        Map<DialogState, Runnable> map = new HashMap<>();
        map.put(DialogState.Idle, () -> iVisible.setVisibleMain());
        iMsgToUi.sendToUiDialog(true, DialogType.Connect, map, device.getName());
    }

    @Override
    public void undo() {
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                iMsgToUi.recallFromUiDialog(true, DialogType.Connect,null);
                t.cancel();
            }
        }, 2000);


    }
}
