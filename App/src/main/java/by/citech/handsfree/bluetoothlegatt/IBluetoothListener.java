package by.citech.handsfree.bluetoothlegatt;

public interface IBluetoothListener {
    void withoutDeviceView();
    void withDeviceView();
    String getUnknownServiceString();
    String unknownCharaString();
}
