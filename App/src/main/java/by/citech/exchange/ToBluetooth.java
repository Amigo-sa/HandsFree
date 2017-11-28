package by.citech.exchange;

import android.util.Log;

import java.util.Arrays;

import by.citech.data.StorageData;
import by.citech.debug.TrafficAnalyzer;
import by.citech.debug.ITrafficUpdate;
import by.citech.debug.TrafficInfo;
import by.citech.debug.TrafficNodes;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class ToBluetooth
        implements IReceiverCtrl, IReceiver, ITrafficUpdate {

    private static final String TAG = Tags.TO_BLUETOOTH;
    private static final boolean debug = Settings.debug;

    private IReceiverReg iReceiverReg;
    private TrafficInfo trafficInfo;
    private boolean isRedirecting = false;
    private StorageData<byte[][]> source;
    private byte[][] dataAssembled;
    private byte[] dataChunk;

    public ToBluetooth(IReceiverReg iReceiverReg, StorageData<byte[][]> source) {
        if (iReceiverReg == null
                || source == null) {
            Log.e(TAG, "ToBluetooth one of key parameters are null");
            return;
        }
        this.iReceiverReg = iReceiverReg;
        this.source = source;
        dataAssembled = new byte[Settings.btToNetFactor][Settings.btToBtSendSize];
        dataChunk = new byte[Settings.btSignificantBytes];
        trafficInfo = new TrafficInfo(TrafficNodes.NetIn, this);
        TrafficAnalyzer.getInstance().addTrafficInfo(trafficInfo);
    }

    public void prepare() {
        if (debug) Log.i(TAG, "prepare");
        redirectOff();
    }

    public void run() {
        if (debug) Log.i(TAG, "run");
        isRedirecting = true;
        iReceiverReg.registerReceiver(this);
        if (Settings.debug) Log.i(TAG, "run done");
    }

    @Override
    public void redirectOff() {
        if (debug) Log.i(TAG, "redirectOff");
        isRedirecting = false;
        source.clear();
        iReceiverReg.registerReceiver(null);
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
            source.putData(dataAssembled);
        }
    }

    @Override
    public void onReceiveData(short[] data) {
        Log.e(TAG, "onReceiveData short[]");
    }

    @Override
    public long getBytesDelta() {
        return 0;
    }

}
