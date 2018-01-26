package by.citech.handsfree.bluetoothlegatt.ui.uicommands.dialogs;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import by.citech.handsfree.ui.IBtToUiCtrl;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.dialog.EDialogState;
import by.citech.handsfree.dialog.EDialogType;
import by.citech.handsfree.ui.IMsgToUi;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ConnInfoDialogCommand implements Command {
    private BluetoothDevice device;
    private IMsgToUi iMsgToUi;
    private IBtToUiCtrl iBtToUiCtrl;

    public ConnInfoDialogCommand() {

    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
    }

    public void setiBtToUiCtrl(IBtToUiCtrl iBtToUiCtrl) {
        this.iBtToUiCtrl = iBtToUiCtrl;
    }

    @Override
    public void execute() {
        Map<EDialogState, Runnable> map = new HashMap<>();
        map.put(EDialogState.Idle, () -> iBtToUiCtrl.setVisibleMain());
        iMsgToUi.sendToUiDialog(true, EDialogType.Connect, map, device.getName());
    }

    @Override
    public void undo() {
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                iMsgToUi.recallFromUiDialog(true, EDialogType.Connect,null);
                t.cancel();
            }
        }, 2000);


    }
}
