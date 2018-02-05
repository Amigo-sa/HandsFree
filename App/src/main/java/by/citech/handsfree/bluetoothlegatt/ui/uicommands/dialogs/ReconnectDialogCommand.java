package by.citech.handsfree.bluetoothlegatt.ui.uicommands.dialogs;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;

import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.dialog.EDialogState;
import by.citech.handsfree.dialog.EDialogType;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.bluetoothlegatt.ConnectorBluetooth;

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
        Map<EDialogState, Runnable> map = new HashMap<>();
        map.put(EDialogState.Proceed, () -> connectorBluetooth.requestDisconnect());
        iMsgToUi.sendToUiDialog(true, EDialogType.Reconnect, map, device.getName());
    }

    @Override
    public void undo() {
        iMsgToUi.recallFromUiDialog(true, EDialogType.Reconnect, EDialogState.Idle);
    }
}
