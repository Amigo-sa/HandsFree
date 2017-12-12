package by.citech.bluetoothlegatt.commands.dialods;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;

import by.citech.R;
import by.citech.bluetoothlegatt.commands.Command;
import by.citech.dialog.DialogState;
import by.citech.dialog.DialogType;
import by.citech.exchange.IMsgToUi;
import by.citech.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 08.12.2017.
 */

public class ReconnectDialogCommand implements Command {
    private BluetoothDevice device;
    private ConnectorBluetooth connectorBluetooth;
    private IMsgToUi iMsgToUi;

    public ReconnectDialogCommand(BluetoothDevice device, ConnectorBluetooth connectorBluetooth, IMsgToUi iMsgToUi) {
        this.device = device;
        this.connectorBluetooth = connectorBluetooth;
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
