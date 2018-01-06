package by.citech.handsfree.bluetoothlegatt.commands.connect;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.settings.Settings;

/**
 * Created by tretyak on 06.12.2017.
 */

public class ConnectCommand implements Command {
    private BluetoothDevice mBTDevice;
    private BluetoothLeService mBluetoothLeService;

    public ConnectCommand() {

    }

    public void setmBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
    }

    public void setmBluetoothLeService(BluetoothLeService mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    @Override
    public void execute() {
        // если сервис привязан производим соединение
        //long start_time = System.currentTimeMillis();
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mBTDevice.getAddress());
            //long end_time = System.currentTimeMillis();
           // if (Settings.debug) Log.i("ConnectCommand", "Connecting await time = " + (end_time - start_time));
        }
    }

    @Override
    public void undo() {

    }
}
