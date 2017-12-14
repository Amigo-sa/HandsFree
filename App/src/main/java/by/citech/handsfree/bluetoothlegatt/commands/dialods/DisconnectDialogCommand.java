package by.citech.handsfree.bluetoothlegatt.commands.dialods;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;

import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.dialog.DialogState;
import by.citech.handsfree.dialog.DialogType;
import by.citech.handsfree.exchange.IMsgToUi;
import by.citech.handsfree.logic.ConnectorBluetooth;

/**
 * Created by tretyak on 08.12.2017.
 */

public class DisconnectDialogCommand implements Command {
    private BluetoothDevice device;
    private ConnectorBluetooth connectorBluetooth;
    private IMsgToUi iMsgToUi;

    public DisconnectDialogCommand(BluetoothDevice device, ConnectorBluetooth connectorBluetooth, IMsgToUi iMsgToUi) {
        this.device = device;
        this.connectorBluetooth = connectorBluetooth;
        this.iMsgToUi = iMsgToUi;
    }

    @Override
    public void execute() {
        Map<DialogState, Runnable> map = new HashMap<>();
        map.put(DialogState.Proceed, () -> connectorBluetooth.disconnect());
        iMsgToUi.sendToUiDialog(true, DialogType.Disconnecting, map, device.getName());
    }

    @Override
    public void undo() {
        iMsgToUi.recallFromUiDialog(true, DialogType.Disconnecting, DialogState.Cancel);
    }
}
