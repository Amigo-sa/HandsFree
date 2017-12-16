package by.citech.handsfree.bluetoothlegatt.commands.dialogs;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;

import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.dialog.DialogState;
import by.citech.handsfree.dialog.DialogType;
import by.citech.handsfree.exchange.IMsgToUi;
import by.citech.handsfree.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 07.12.2017.
 */

public class ConnectDialogCommand implements Command {
    private BluetoothDevice device;
    private ConnectorBluetooth connectorBluetooth;
    private IMsgToUi iMsgToUi;

    public ConnectDialogCommand(BluetoothDevice device, ConnectorBluetooth connectorBluetooth, IMsgToUi iMsgToUi) {
        this.device = device;
        this.connectorBluetooth = connectorBluetooth;
        this.iMsgToUi = iMsgToUi;
    }

    @Override
    public void execute() {
        Map<DialogState, Runnable> map = new HashMap<>();
        map.put(DialogState.Cancel, () -> connectorBluetooth.disconnect());
        iMsgToUi.sendToUiDialog(true, DialogType.Connecting, map, device.getName());
    }

    @Override
    public void undo() {
        iMsgToUi.recallFromUiDialog(true, DialogType.Connecting, DialogState.Idle);
    }

}