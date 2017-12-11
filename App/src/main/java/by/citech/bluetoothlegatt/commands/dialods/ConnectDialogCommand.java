package by.citech.bluetoothlegatt.commands.dialods;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import by.citech.R;
import by.citech.bluetoothlegatt.commands.Command;
import by.citech.dialog.DialogState;
import by.citech.dialog.DialogType;
import by.citech.exchange.IMsgToUi;
import by.citech.logic.ConnectorBluetooth;
import by.citech.param.Settings;

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

        iMsgToUi.sendToUiDialog(true, DialogType.Connect, map, device.getName());

    }


    public void undo() {
        iMsgToUi.recallFromUiDialog(DialogType.Connect);
    }

}
