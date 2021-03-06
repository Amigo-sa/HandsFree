package by.citech.handsfree.bluetoothlegatt;

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
import android.util.Log;

import java.util.List;
import java.util.UUID;

import by.citech.handsfree.application.ThisApp;
import by.citech.handsfree.bluetoothlegatt.rwdata.CallbackWriteListener;
import by.citech.handsfree.bluetoothlegatt.rwdata.Requestable;
import by.citech.handsfree.data.SampleGattAttributes;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.statistic.RssiReporter;
import by.citech.handsfree.statistic.TrafficAnalyzer;
import timber.log.Timber;

/**
 * Created by tretyak on 24.01.2018.
 */

public class BluetoothLeCore
        implements TrafficAnalyzer.ITrafficUpdate,
                   Requestable,
                   RssiReporter.IRssiProvider,
                   RssiReporter.IRssiProviderRegister{

    private final static String TAG = "WSD_BluetoothLeCore";
    private static BluetoothManager mBluetoothManager;
    private static BluetoothAdapter mBluetoothAdapter;

    private static volatile BluetoothLeCore instance = null;

    private BluetoothLeCore(){

    }

    public static BluetoothLeCore getInstance(){
        if (instance == null) {
            synchronized (BluetoothLeCore.class) {
                if (instance == null){
                    instance = new BluetoothLeCore();
                }
            }
        }
        return instance;
    }
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private CallbackWriteListener mCallbackWriteListener;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    //Создаём сообщения для BroadcastReceiverWrapper-а которые будут отправляться в качестве Callback-а
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_WRITE = "com.example.bluetooth.le.ACTION_DATA_WRITE";
    public final static String ACTION_DESCRIPTOR_WRITE = "com.example.bluetooth.le.ACTION_DESCRIPTOR_WRITE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_WDATA = "com.example.bluetooth.le.EXTRA_WDATA";

    public final static UUID READ_BYTES = UUID.fromString(SampleGattAttributes.READ_BYTES);

    public void setCallbackWriteListener(CallbackWriteListener callbackWriteListener) {
        this.mCallbackWriteListener = callbackWriteListener;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.i(TAG, "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                //------------- TEST START
                registerRssiProvider(BluetoothLeCore.this);
                //------------- TEST END

                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                if (Settings.debug) Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                if (Settings.debug) Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                //------------- TEST START
                unregisterRssiProvider(BluetoothLeCore.this);
                //------------- TEST END

                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                if (Settings.debug) Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                if (Settings.debug) Log.w(TAG, "onServicesDiscovered received: " + status);
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
            //if (Settings.debug) Log.i(TAG,"onCharacteristicWrite");
            if (mCallbackWriteListener != null) mCallbackWriteListener.callbackIsDone();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //if (Settings.debug) Log.i(TAG,"GATT SUCCESS " + "DATA WRITE:");
            }
            if (status == BluetoothGatt.GATT_CONNECTION_CONGESTED) {
                if (Settings.debug) Log.i(TAG, "GATT WRITE connection congested");
            }
            if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
                if (Settings.debug) Log.i(TAG, "GATT WRITE not permitted");
            }
            if (status == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH) {
                if (Settings.debug) Log.i(TAG, "GATT invalid attribute lenght");
            }
            if (status == BluetoothGatt.GATT_FAILURE) {
                if (Settings.debug) Log.i(TAG, "GATT WRITE other errors");
            }
            if (status == BluetoothGatt.GATT_CONNECTION_CONGESTED) {
                if (Settings.debug) Log.i(TAG, "GATT WRITE congested");
            }
            if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (Settings.debug) Log.i(TAG, "GATT WRITE authentication");
            } else {
                //if (Settings.debug) Log.i(TAG,"GATT WRITE :"+status);
            }
            broadcastUpdate(ACTION_DATA_WRITE);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                RssiReporter.getInstance().onRssiResponse(rssi);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            if (mCallbackWriteListener != null) {
                final byte[] data = characteristic.getValue();
                mCallbackWriteListener.rcvBtPktIsDone(data);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            //if (Settings.debug) Log.w("WSD_MTU", String.format("mtu = %d, status = %d", mtu, status));
            if (status==BluetoothGatt.GATT_SUCCESS) mCallbackWriteListener.onMtuChangeIsDone(mtu);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (Settings.debug) Log.i(TAG, String.format("onDescriptorWrite() success, write status = %d", status));
            if (status==BluetoothGatt.GATT_SUCCESS){
                mCallbackWriteListener.callbackDescriptorIsDone();
                broadcastUpdate(ACTION_DESCRIPTOR_WRITE);
            }
        }

    };

    // строку загружаем в Intent и передаём в BroadcastReceiverWrapper-у
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        //intent.putExtra(EXTRA_WDATA, wrData);
        ThisApp.getAppContext().sendBroadcast(intent);
    }

    // перегруженный метод broadcastUpdate в который помимо сообщения передаём и характеристику
    // и получаем данные
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        ThisApp.getAppContext().sendBroadcast(intent);
    }

    @Override
    public void requestMtu() {
        if (mBluetoothGatt != null) mBluetoothGatt.requestMtu(Settings.Bluetooth.btMtuSize);
        if (Settings.debug) Log.i(TAG, "requestMtu was sendet, await callback ...");
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (getBluetoothManager() == null) {
            if (Settings.debug) Log.e(TAG, "Unable to initialize BluetoothManager.");
            return false;
        }

        if (getBluetoothAdapter() == null) {
            if (Settings.debug) Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public static BluetoothManager getBluetoothManager() {
        if (mBluetoothManager == null)
            mBluetoothManager = (BluetoothManager) ThisApp.getAppContext().getSystemService(Context.BLUETOOTH_SERVICE);
        return mBluetoothManager;
    }

    public static BluetoothAdapter getBluetoothAdapter() {
        if (mBluetoothAdapter == null) {
            mBluetoothManager = getBluetoothManager();
            if (mBluetoothManager != null) {
                mBluetoothAdapter = mBluetoothManager.getAdapter();
            }
        }
        return mBluetoothAdapter;
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            if (Settings.debug) Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            if (Settings.debug) Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            try {
                if (mBluetoothGatt.connect()) {
                    if (Settings.debug) Log.d(TAG, "mBluetoothGatt.connect()");
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e){
                mBluetoothGatt.disconnect();
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (Settings.debug) Log.i(TAG, "Get remote Device " + device);
        if (device == null) {
            if (Settings.debug) Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.    , BluetoothDevice.TRANSPORT_LE

        //--------------- TEST START
        // mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothGatt = device.connectGatt(ThisApp.getAppContext(), false, mGattCallback);
        //--------------- TEST END

        mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);

        if (Settings.debug) Log.i(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        return true;
    }

    /**
     * Disconnects an existing connection or closeConnectionForce a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            if (Settings.debug) Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
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
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            if (Settings.debug) Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt,
     * android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     * @param characteristic The characteristic write to.
     */
    public boolean oneCharacteristicWrite(BluetoothGattCharacteristic characteristic) {
        //if (Settings.debug) Log.w(TAG, "oneCharacteristicWrite()");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            if (Settings.debug) Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }
    /**
     * Enables or disables notification on a give characteristic.
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || characteristic == null) {
            if (Settings.debug) Log.i(TAG, "BluetoothAdapter not initialized");
            return;
        }
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (Settings.debug) Log.i(TAG, "setCharacteristicNotification " + enabled);
        if (READ_BYTES.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            if (descriptor != null) {
                if (enabled) descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                else         descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                //if (Settings.debug) Log.i(TAG, "descriptorValue = " + descriptor.getValue());
                if (Settings.debug) Log.i(TAG, "descriptorCharacteristic = " + descriptor.getCharacteristic());
                //if (Settings.debug) Log.i(TAG, "descriptorUUID = " + descriptor.getUuid());
                //if (Settings.debug) Log.i(TAG, "descriptorPermissions = " + descriptor.getPermissions());
                mBluetoothGatt.writeDescriptor(descriptor);
                if (Settings.debug) Log.i(TAG, "Notify descriptor was written, await callback ...");
            }
            else {
                if (Settings.debug) Log.e(TAG, "characteristic is null");
            }
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    //--------------------- IRssiProvider

    @Override
    public void requestRssi() {
        if (mBluetoothGatt != null) mBluetoothGatt.readRemoteRssi();
    }
}
