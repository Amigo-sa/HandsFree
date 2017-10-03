package  by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

/**
 * Created by tretyak on 02.10.2017.
 */

public class WriterTransmitter extends Thread {

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private long delay;
    private WriterTransmitterCallbackListener listener;
    int cnt = 0;
    // колличество отправляемых на запись пакетов данных
    final int sends = 30;

    public WriterTransmitter(String name, BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic, long delay) {
        super(name);
        this.mBluetoothGatt = mBluetoothGatt;
        this.characteristic = characteristic;
        this.delay = delay;
    }

    public void addWriteListener(WriterTransmitterCallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        StringBuilder data = new StringBuilder();
        // посылаем на запись 16 байт данных(1 пакет)
        data.append("FFFF0000FFFF0000");
        byte[] dataByte = data.toString().getBytes();
        //byte[] dataByte = new byte[16];

        while (!isAllSendData()){
            characteristic.setValue(dataByte);
            Log.w("Shit", "characteristic = " + characteristic);
            mBluetoothGatt.writeCharacteristic(characteristic);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (listener != null)
            listener.doWriteCharacteristic("");
    }

    private boolean isAllSendData(){
        return (sends - cnt++) <= 0;
    }

}
