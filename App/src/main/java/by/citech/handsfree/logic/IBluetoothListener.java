package by.citech.handsfree.logic;

public interface IBluetoothListener {
    void withoutDeviceView();
    void withDeviceView();
    String getUnknownServiceString();
    String unknownCharaString();
}
