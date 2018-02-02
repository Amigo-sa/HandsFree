package by.citech.handsfree.bluetoothlegatt.ui.uicommands.dialogs;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.bluetoothlegatt.ui.BluetoothUi;
import by.citech.handsfree.dialog.EDialogState;
import by.citech.handsfree.dialog.EDialogType;
import by.citech.handsfree.ui.IMsgToUi;

/**
 * Created by tretyak on 02.02.2018.
 */

public class ChoseDialogCommand implements Command {
    private IMsgToUi iMsgToUi;
    private BluetoothDevice device;
    private BluetoothUi bluetoothUi;

    public ChoseDialogCommand(BluetoothUi bluetoothUi) {
        this.bluetoothUi = bluetoothUi;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
    }

    @Override
    public void execute() {
        Map<EDialogState, Runnable> map = new HashMap<>();
        Log.i("ChoseDialogCommand", "execute()");
        map.put(EDialogState.Proceed, () -> {
            bluetoothUi.clickBtnChoseProceed();});
        iMsgToUi.sendToUiDialog(true, EDialogType.Chose, map, device.getName());
    }

    @Override
    public void undo() {
        iMsgToUi.recallFromUiDialog(true, EDialogType.Chose, EDialogState.Idle);
    }
}
