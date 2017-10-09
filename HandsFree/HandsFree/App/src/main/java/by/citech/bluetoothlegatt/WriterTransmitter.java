package  by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import by.citech.data.StorageData;

/**
 * Created by tretyak on 02.10.2017.
 */

public class WriterTransmitter extends Thread {

    //private Resource res;
    private StorageData stData;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private WriterTransmitterCallbackListener listener;
    int cnt = 0;
    // колличество отправляемых на запись пакетов данных
    final int sends = 100;

    public WriterTransmitter(String name, StorageData stData, BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic) {
        super(name);
        //this.res = res;Resource res,
        this.stData = stData;
        this.mBluetoothGatt = mBluetoothGatt;
        this.characteristic = characteristic;
    }

    public void addWriteListener(WriterTransmitterCallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        //StringBuilder data = new StringBuilder();
        // посылаем на запись 16 байт данных(1 пакет)
        //data.append("FFFF0000FFFF0000");
        //byte[] stringByte = data.toString().getBytes();
        //byte[] dataByte = new byte[16];
        byte[] dataByte;



        while (isAllSendData()){
            dataByte = stData.getData();
            final StringBuilder stringBuilder = new StringBuilder(dataByte.length);
            for(byte byteChar : dataByte)
                stringBuilder.append(String.format("%02X ", byteChar));
            Log.w("Write DATA ", stringBuilder.toString());

            characteristic.setValue(dataByte);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBluetoothGatt.writeCharacteristic(characteristic);
            try {
                Thread.sleep(0,10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (listener != null)
            listener.doWriteCharacteristic("");
    }

    public void cancel() { interrupt(); }

    private boolean isAllSendData(){
        return stData.isOpen();
                //(sends - cnt++) <= 0;
    }

}
