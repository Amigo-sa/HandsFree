package by.citech.handsfree.bluetoothlegatt.commands.connect;

import android.bluetooth.BluetoothDevice;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class ConnectCommand implements Command {
    private BluetoothDevice mBTDevice;
    private BluetoothLeService mBluetoothLeService;

    public ConnectCommand(BluetoothDevice mBTDevice, BluetoothLeService mBluetoothLeService) {
        this.mBTDevice = mBTDevice;
        this.mBluetoothLeService = mBluetoothLeService;
    }

    @Override
    public void execute() {
        // если сервис привязан производим соединение
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(mBTDevice.getAddress());
    }

    @Override
    public void undo() {

    }
}