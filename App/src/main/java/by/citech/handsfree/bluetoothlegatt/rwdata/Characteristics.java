package by.citech.handsfree.bluetoothlegatt.rwdata;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import by.citech.handsfree.data.SampleGattAttributes;
import by.citech.handsfree.logic.IBluetoothListener;
import by.citech.handsfree.settings.Settings;

/**
 * Created by tretyak on 16.11.2017.
 */

public class Characteristics {

    private final static String TAG = "WSD_Characteristics";
    // список характеристик устройства
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private IBluetoothListener mIBluetoothListener;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public Characteristics(IBluetoothListener mIBluetoothListener) {
        this.mIBluetoothListener = mIBluetoothListener;
    }

    public boolean isEmpty(){
        if (mGattCharacteristics != null)
            return false;
        return true;
    }

    //Собираем все имеющиеся характеристики устройства в коллекции
    public void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        // обьявляем переменные для будущих сервисов и характеристик
        String uuid = null;  // уникальный идентификатор сервиса или характеристики
        String unknownServiceString = mIBluetoothListener.getUnknownServiceString();// если имя атрибуда сервиса не известно пишем это
        String unknownCharaString = mIBluetoothListener.unknownCharaString(); // если имя атрибуда характеристики не известно пишем это
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>(); // список доступных данных сервисов периферийного устройства
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>(); // список доступных данных характеристик периферийного устройства
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {  // прогоняем список всех сервисов устройства
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString(); // получаем идентификатор каждого сервиса
            // По таблице соответствия uuid имени сервиса на дисплей выводим именно имя из таблицы
            // если соответствия в таблице нет то выводим на дисплей unknown_service
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            // добавляем сервис в список
            gattServiceData.add(currentServiceData);
            // создаём список данных характеристики
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            // получаем все характеристики из сервиса(неименованные)
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            // создаём новый список характеристик
            ArrayList<BluetoothGattCharacteristic> charas =  new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                //добавляем характеристики в новый список
                charas.add(gattCharacteristic);
                // создаём карту данных характеристики
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                // получаем идентификатор каждой характеристики
                uuid = gattCharacteristic.getUuid().toString();
                // именуем все характеристики какие возможно согласно таблице uuid - SampleGattAttributes
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                // добавляем именнованные характеристики в список
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    // получаем характеристику для включения нотификации на периферийном устройстве(сервере)
    public BluetoothGattCharacteristic getNotifyCharacteristic(){
        if (Settings.debug) Log.i(TAG,"getNotifyCharacteristic()");
        if (mGattCharacteristics.size() == 4)
            return mGattCharacteristics.get(3).get(2);
        return null;
    };

    // получаем характеристику для включения записи на периферийном устройстве(сервере)
    public BluetoothGattCharacteristic getWriteCharacteristic(){
        if (Settings.debug) Log.i(TAG,"getWriteCharacteristic()");
        if (mGattCharacteristics.size() == 4)
            return mGattCharacteristics.get(3).get(1);
        return null;
    };

}