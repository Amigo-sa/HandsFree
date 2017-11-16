/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package by.citech.bluetoothlegatt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import by.citech.data.SampleGattAttributes;
import by.citech.data.StorageData;
import by.citech.logic.Resource;
import by.citech.param.Settings;
import by.citech.param.Tags;
import by.citech.util.Decode;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private  WriterTransmitter wrt;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    //Создаём сообщения для BroadcastReceiver-а которые будут отправляться в качестве Callback-а
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_WRITE = "com.example.bluetooth.le.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_WDATA = "com.example.bluetooth.le.EXTRA_WDATA";

    // UUID устройства HEART_RATE_MEASUREMENT
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    // UUID устройства CIT_HANDS_FREE
    public final static UUID UUID_CIT_HANDS_FREE =
            UUID.fromString(SampleGattAttributes.CIT_HANDS_FREE);
    // UUID характеристики READ_BYTES
    public final static UUID READ_BYTES =
            UUID.fromString(SampleGattAttributes.READ_BYTES);

    private StorageData<byte[]> storageBtToNet;
    private StorageData<byte[][]> storageNetToBt;

    private boolean loopback = true;
    public Resource res;
    private String wrData;

    public void setStorageBtToNet(StorageData<byte[]> storageBtToNet) {
        this.storageBtToNet = storageBtToNet;
    }

    public void setStorageNetToBt(StorageData<byte[][]> storageNetToBt) {
        this.storageNetToBt = storageNetToBt;
    }

    public void initStore(){
        res = new Resource(true,20);
        loopback = true;
        // storageNetToBt = new StorageData();
    }

    public void closeStore(){
        loopback = false;
    }
    // Методы для работы с потоком записи
    public WriterTransmitter getWriteThread(){
        return wrt;
    }
    public void stopDataTransfer(){wrt.cancel();}

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

//            final StringBuilder stringBuilder = new StringBuilder(characteristic.getValue().length);
//            for(byte byteChar : characteristic.getValue())
//                stringBuilder.append(String.format("%02X ", byteChar));
//             wrData = stringBuilder.toString();

            if(status==BluetoothGatt.GATT_SUCCESS)
            {
                Log.i("test","GATT SUCCESS " + "DATA :");
                //res.setCallback(true);

            }
            if(status==BluetoothGatt.GATT_CONNECTION_CONGESTED)
            {
                Log.i("test","GATT WRITE connection congested");
            }
            if(status==BluetoothGatt.GATT_WRITE_NOT_PERMITTED)
            {
                Log.i("test","GATT WRITE not permitted");
            }
            if(status==BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            {
                Log.i("test","GATT invalid attribute lenght");
            }
            if(status==BluetoothGatt.GATT_FAILURE)
            {
                Log.i("test","GATT WRITE other errors");
            }
            if(status==BluetoothGatt.GATT_CONNECTION_CONGESTED)
            {
                Log.i("test","GATT WRITE congested");
            }
            if(status==BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION)
            {
                Log.i("test","GATT WRITE authentication");
            }
            else
            {
                Log.i("test","GATT WRITE :"+status);
            }

            broadcastUpdate(ACTION_DATA_WRITE);
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.w(TAG, "RSSI " + rssi);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }



        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

            Log.w("WSD_MTU", String.format("mtu = %d, status = %d", mtu, status));
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                res = new Resource(true, mtu);
//                Log.w("WSD_MTU", "status = GATT_SUCCESS");
//                if (SampleGattAttributes.WRITE_BYTES.equals(characteristic.getUuid().toString())) {
//                    Log.w("WSD_MTU", "attribute WRITE_BYTES");
//                    WriterTransmitter wrt = new WriterTransmitter("Write", mBluetoothGatt,
//                            characteristic, 1, STData, Res);
//                    Log.w("WSD_MTU", "object WriterTransmitter is done");
//                    wrt.addWriteListener(new WriterTransmitterCallbackListener() {
//                        @Override
//                        public void doWriteCharacteristic(String str) {
//                            System.out.println(str);
//                        }
//                    });
//                    Log.w("WSD_MTU", "add WriteListener");
//                    wrt.start();
//                    Log.w("WSD_MTU", "Write Thread START");
//                }
//            }
        }






    };
    // строку загружаем в Intent и передаём в BroadcastReceiver-у
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_WDATA, wrData);
        sendBroadcast(intent);
    }
   // перегруженный метод broadcastUpdate в который помимо сообщения передаём и характеристику
   // и получаем данные
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            //if (loopback)
            storageBtToNet.putData(data);
            res.setCallback(true);

            if (data != null && data.length > 0) {
                if (Settings.debug) Log.w("WSD_BLE_DATA","storageBtToNet.putData " + Decode.bytesToHexMark1(data));
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.i(TAG, "Get remote Device " + device);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
       // Log.i(TAG, "mConnectionState = " + STATE_CONNECTING);
        return true;
    }

     public String getConnectDeviceAddress(){
         return mBluetoothDeviceAddress;
     }

    /**
     * Disconnects an existing connection or closeConnectionForce a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (getWriteThread() != null)
            wrt.cancel();
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt,
     * android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic write to.
     */

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (SampleGattAttributes.WRITE_BYTES.equals(characteristic.getUuid().toString())) {
// потоковая запись данных в периферийное устройство
            wrt = new WriterTransmitter("Write_one", res, storageNetToBt, mBluetoothGatt, characteristic);
            wrt.setPriority(Thread.MAX_PRIORITY);
            wrt.addWriteListener(new WriterTransmitterCallbackListener() {
                @Override
                public void doWriteCharacteristic(String str) {
                    System.out.println(str);
                }
            });
            wrt.start();
// одноразовая запись данных в периферийное устройство

//            StringBuilder data = new StringBuilder();
//            // посылаем на запись 16 байт данных
//            data.append("FFFF0000FFFF0000");
//            byte[] dataByte = data.toString().getBytes();
//            //byte[] dataByte = new byte[16];
//            characteristic.setValue(dataByte);
//            Log.w(TAG, "characteristic = " + characteristic);
//            mBluetoothGatt.writeCharacteristic(characteristic);


        }
    }

    /**
     * Stop the Write Thread
     */
    public void stopWriteThread() {
        wrt.cancel();
    }
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        if (READ_BYTES.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
            else {
                Log.w(TAG, "characteristic is null");
            }
        }

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
