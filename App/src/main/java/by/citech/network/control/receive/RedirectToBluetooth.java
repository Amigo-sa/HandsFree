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
    private byte[][] dataAssembled;
    private byte[] dataChunk;

    RedirectToBluetooth(IReceiveListenerReg iReceiveListenerReg, StorageData<byte[][]> storageNetToBt) {
        this.iReceiveListenerReg = iReceiveListenerReg;
        this.storageNetToBt = storageNetToBt;
        dataAssembled = new byte[Settings.btToNetFactor][Settings.btToBtSendSize];
        dataChunk = new byte[Settings.btSignificantBytes];
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
    public void onReceiveData(final byte[] data) {
        if (debug) Log.i(TAG, "onReceiveData");
        if (debug) Log.i(TAG, "onReceiveData received bytes: " + data.length);
        if (data.length != Settings.btnNetToNetSendSize) {
            return;
        }
        if (isRedirecting) {
            for (int i = 0; i < Settings.btToNetFactor; i++) {
                if (debug) Log.i(TAG, String.format("chunk %d from %d", (i - 1), data.length));
                dataChunk = Arrays.copyOfRange(data, i * Settings.btSignificantBytes, ((i + 1) * Settings.btSignificantBytes) - 1);
                //dataChunk = Arrays.copyOfRange(data, i * Settings.btSignificantBytes, (i + 1) * Settings.btSignificantBytes);
                if (debug) Log.i(TAG, String.format("onReceiveData dataChunk[btSignificantBytes] is %s", Arrays.toString(dataChunk)));
                dataAssembled[i] = Arrays.copyOf(dataChunk, Settings.btToBtSendSize);
                if (debug) Log.i(TAG, String.format("onReceiveData dataAssembled[%d][btToBtSendSize] is %s", i, Arrays.toString(dataAssembled[i])));
            }
            storageNetToBt.putData(dataAssembled);
        }
    }

}
