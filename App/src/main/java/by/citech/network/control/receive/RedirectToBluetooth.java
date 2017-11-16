package by.citech.network.control.receive;

import android.util.Log;

import java.util.Arrays;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToBluetooth implements IRedirectCtrl, IReceiveListener {
    private static final String TAG = Tags.NET_REDIR_BLUETOOTH;
    private static final boolean debug = Settings.debug;
    private IReceiveListenerReg iReceiveListenerReg;
    private boolean isRedirecting = false;
    private StorageData<byte[][]> storageNetToBt;
    private byte[][] byteArrayX2;
    private byte[] tempArray;

    RedirectToBluetooth(IReceiveListenerReg iReceiveListenerReg, StorageData<byte[][]> storageNetToBt) {
        this.iReceiveListenerReg = iReceiveListenerReg;
        this.storageNetToBt = storageNetToBt;
        byteArrayX2 = new byte[Settings.btToNetFactor][Settings.btToBtSendSize];
        tempArray = new byte[Settings.btSignificantBytes];
    }

    public IRedirectCtrl start() {
        if (debug) Log.i(TAG, "start");
        redirectOff();
        return this;
    }

    public void run() {
        if (debug) Log.i(TAG, "run");
        isRedirecting = true;
        iReceiveListenerReg.registerReceiverListener(this);
        if (Settings.debug) Log.i(TAG, "run done");
    }

    @Override
    public void redirectOff() {
        if (debug) Log.i(TAG, "redirectOff");
        isRedirecting = false;
        storageNetToBt.clear();
        iReceiveListenerReg.registerReceiverListener(null);
    }

    @Override
    public void onReceiveData(byte[] data) {
        if (debug) Log.i(TAG, "onReceiveData");
        if (data.length != Settings.btnNetToNetSendSize) {
            if (debug) Log.i(TAG, "onReceiveData received bytes: " + data.length);
            return;
        }
        if (isRedirecting) {
            for (int i = 0; i < Settings.btToNetFactor; i++) {
                tempArray = Arrays.copyOfRange(data, i * Settings.btSignificantBytes, (i + 1) * Settings.btSignificantBytes);
                byteArrayX2[i] = Arrays.copyOf(tempArray, Settings.btToBtSendSize);
            }
            storageNetToBt.putData(byteArrayX2);
        }
    }

}
