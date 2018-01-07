package by.citech.handsfree.bluetoothlegatt.commands.dialogs;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;

import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.dialog.DialogState;
import by.citech.handsfree.dialog.DialogType;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ReconnectDialogCommand implements Command {
    private BluetoothDevice device;
    private ConnectorBluetooth connectorBluetooth;
    private IMsgToUi iMsgToUi;

    public ReconnectDialogCommand(ConnectorBluetooth connectorBluetooth) {
        this.connectorBluetooth = connectorBluetooth;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
    }

    @Override
    public void execute() {
        Map<DialogState, Runnable> map = new HashMap<>();
        map.put(DialogState.Proceed, () -> {
            connectorBluetooth.disconnect();
            connectorBluetooth.connecting();
        });
        iMsgToUi.sendToUiDialog(true, DialogType.Reconnect, map, device.getName());
    }

    @Override
    public void undo() {
        iMsgToUi.recallFromUiDialog(true, DialogType.Reconnect, DialogState.Idle);
    }
}
