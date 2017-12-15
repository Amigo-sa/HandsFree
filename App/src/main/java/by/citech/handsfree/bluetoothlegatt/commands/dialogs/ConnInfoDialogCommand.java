package by.citech.handsfree.bluetoothlegatt.commands.dialogs;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import by.citech.handsfree.gui.IBtToUiCtrl;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.dialog.DialogState;
import by.citech.handsfree.dialog.DialogType;
import by.citech.handsfree.exchange.IMsgToUi;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ConnInfoDialogCommand implements Command {
    private BluetoothDevice device;
    private IMsgToUi iMsgToUi;
    private IBtToUiCtrl iBtToUiCtrl;

    public ConnInfoDialogCommand(BluetoothDevice device, IMsgToUi iMsgToUi, IBtToUiCtrl iBtToUiCtrl) {
        this.device = device;
        this.iMsgToUi = iMsgToUi;
        this.iBtToUiCtrl = iBtToUiCtrl;
    }

    @Override
    public void execute() {
        Map<DialogState, Runnable> map = new HashMap<>();
        map.put(DialogState.Idle, () -> iBtToUiCtrl.setVisibleMain());
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
