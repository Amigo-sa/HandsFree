package by.citech.handsfree.util;

public class BluetoothHelper {
    public static boolean isValidAddr(String addr) {
        return addr != null && !addr.isEmpty() && addr.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }
}
