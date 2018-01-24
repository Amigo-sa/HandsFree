package by.citech.handsfree.util;

import android.bluetooth.BluetoothAdapter;

public class BluetoothHelper {

    public static boolean isValidAddr(String addr) {
        return BluetoothAdapter.checkBluetoothAddress(addr);
    }

}
