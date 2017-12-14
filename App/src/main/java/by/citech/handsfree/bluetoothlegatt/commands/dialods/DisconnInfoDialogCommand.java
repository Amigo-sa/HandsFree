package by.citech.handsfree.bluetoothlegatt.commands.dialods;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import by.citech.handsfree.bluetoothlegatt.IVisible;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.dialog.DialogState;
import by.citech.handsfree.dialog.DialogType;
import by.citech.handsfree.exchange.IMsgToUi;

/**
 * Created by tretyak on 08.12.2017.
 */

public class DisconnInfoDialogCommand implements Command {
    private BluetoothDevice device;
    private IMsgToUi iMsgToUi;
    private IVisible iVisible;

    public DisconnInfoDialogCommand(BluetoothDevice device, IMsgToUi iMsgToUi, IVisible iVisible) {
        this.device = device;
        this.iMsgToUi = iMsgToUi;
        this.iVisible = iVisible;
    }

    @Override
    public void execute() {
        Map<DialogState, Runnable> map = new HashMap<>();
        map.put(DialogState.Idle, () -> iVisible.setVisibleList());
        iMsgToUi.sendToUiDialog(true, DialogType.Disconnect, map, device.getName());
    }

    @Override
    public void undo() {

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                iMsgToUi.recallFromUiDialog(true, DialogType.Disconnect,null);
                t.cancel();
            }
        }, 2000);

    }
}
