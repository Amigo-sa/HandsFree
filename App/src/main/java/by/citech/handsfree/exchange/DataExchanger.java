package by.citech.handsfree.exchange;

import by.citech.handsfree.data.StorageData;

public class DataExchanger {

    private StorageData<byte[]> storageDataBytesX1;
    private StorageData<byte[][]> storageDataBytesX2;
    private ITransmitter iTransmitter;

    public static ITransmitter getStorageExchanger() {
        return null;
    }
}
