package  by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import by.citech.data.StorageData;

/**
 * Created by tretyak on 02.10.2017.
 */

public class WriterTransmitter extends Thread {

    private Resource res;
    private StorageData stData;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private WriterTransmitterCallbackListener listener;
    int cnt = 0;
    private boolean isRunning;
    // колличество отправляемых на запись пакетов данных
    final int sends = 100;

    public WriterTransmitter(String name, Resource res, StorageData stData, BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic) {
        super(name);
        this.res = res;
        this.stData = stData;
        this.mBluetoothGatt = mBluetoothGatt;
        this.characteristic = characteristic;
    }

    public void addWriteListener(WriterTransmitterCallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        byte[] dataByte = new byte[ ]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        isRunning = true;
        while (isRunning){

            dataByte[0]++;
           // dataByte = stData.getData();
            if(stData.isOpen() && !stData.isEmptyStorage()) {
                characteristic.setValue(dataByte);
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                mBluetoothGatt.writeCharacteristic(characteristic);
                mBluetoothGatt.executeReliableWrite();

                /**
                 *   когда использую mBluetoothGatt.beginReliableWrite();  происходит
                 *   зависание на удалённом устройстве, в логе выдаётся ошибка:
                 *   E/bt_att: value resp op_code = ATT_RSP_PREPARE_WRITE len = 20
                 *   снимается зависание методом mBluetoothGatt.abortReliableWrite()
                 *
                 * */
//                mBluetoothGatt.beginReliableWrite();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    mBluetoothGatt.abortReliableWrite();
//                }

                final StringBuilder stringBuilder = new StringBuilder(dataByte.length);
                for (byte byteChar : dataByte)
                    stringBuilder.append(String.format("%02X ", byteChar));
                Log.w("Write DATA ", stringBuilder.toString());
            }
            try {
                Thread.sleep(5000);
                stData.putData(dataByte);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        if (listener != null)
            listener.doWriteCharacteristic("");
    }

    public void cancel() {isRunning = false; }

    private boolean isAllSendData(){
        return stData.isOpen();
                //(sends - cnt++) <= 0;
    }

}
