package by.citech.network.control.receive;

import android.util.Log;

import java.util.Arrays;

import by.citech.data.StorageData;
import by.citech.debug.TrafficAnalyzer;
import by.citech.debug.ITrafficUpdate;
import by.citech.debug.TrafficInfo;
import by.citech.debug.TrafficNodes;
import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToBluetooth implements IRedirectCtrl, IReceiveListener, ITrafficUpdate {

    private static final String TAG = Tags.NET_REDIR_BT;
    private static final boolean debug = Settings.debug;

    private IReceiveListenerReg iReceiveListenerReg;
    private TrafficInfo trafficInfo;
    private boolean isRedirecting = false;
    private StorageData<byte[][]> storageNetToBt;
    private byte[][] dataAssembled;
    private byte[] dataChunk;

    RedirectToBluetooth(IReceiveListenerReg iReceiveListenerReg, StorageData<byte[][]> storageNetToBt) {
        if (iReceiveListenerReg == null
                || storageNetToBt == null) {
            Log.e(TAG, "RedirectToBluetooth one of key parameters are null");
            return;
        }
        this.iReceiveListenerReg = iReceiveListenerReg;
        this.storageNetToBt = storageNetToBt;
        dataAssembled = new byte[Settings.btToNetFactor][Settings.btToBtSendSize];
        dataChunk = new byte[Settings.btSignificantBytes];
        trafficInfo = new TrafficInfo(TrafficNodes.NetIn, this);
        TrafficAnalyzer.getInstance().addTrafficInfo(trafficInfo);
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
//        if (debug) Log.i(TAG, "onReceiveData received bytes: " + data.length);
        if (data.length != Settings.btToNetSendSize)
            return;
        if (debug) Log.i(TAG, "onReceiveData received correct amount of bytes");
        if (isRedirecting) {
            for (int i = 0; i < Settings.btToNetFactor; i++) {
//                if (debug) Log.i(TAG, String.format("chunk %d from %d", (i - 1), data.length));
                dataChunk = Arrays.copyOfRange(data, i * Settings.btSignificantBytes, (i + 1) * Settings.btSignificantBytes);
//                if (debug) Log.i(TAG, String.format("onReceiveData dataChunk[btSignificantBytes] is %s", Decode.bytesToHexMark1(dataChunk)));
                dataAssembled[i] = Arrays.copyOf(dataChunk, Settings.btToBtSendSize);
//                if (debug) Log.i(TAG, String.format("onReceiveData dataAssembled[%d][btToBtSendSize] is %s", i, Decode.bytesToHexMark1(dataAssembled[i])));
            }
            storageNetToBt.putData(dataAssembled);
        }
    }

    @Override
    public long getBytesDelta() {
        return 0;
    }
}
