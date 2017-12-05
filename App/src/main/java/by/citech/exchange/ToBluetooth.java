package by.citech.exchange;

import android.util.Log;

import java.util.Arrays;

import by.citech.codec.audio.AudioCodec;
import by.citech.codec.audio.AudioCodecType;
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

    //--------------------- settings

    private int toBtFactor;
    private int btToBtSendSize;
    private int btSignificantBytes;
    private int toBtSendSize;
    private boolean singlePacket;
    private byte[][] dataAssembled;

    {
        takeSettings();
        applySettings();
    }

    private void takeSettings() {
        toBtFactor = Settings.btFactor;
        btToBtSendSize = Settings.bt2btPacketSize;
        btSignificantBytes = Settings.btSignificantBytes;
        toBtSendSize = Settings.btSendSize;
        singlePacket = Settings.btSinglePacket;
    }

    private void applySettings() {
        dataAssembled = new byte[toBtFactor][btToBtSendSize];
    }

    //--------------------- non-settings

    private IReceiverReg iReceiverReg;
    private TrafficInfo trafficInfo;
    private boolean isRedirecting = false;
    private StorageData<byte[][]> source;

    public ToBluetooth(IReceiverReg iReceiverReg, StorageData<byte[][]> source) {
        if (iReceiverReg == null
                || source == null) {
            Log.e(TAG, "ToBluetooth illegal parameters");
            return;
        }
        this.iReceiverReg = iReceiverReg;
        this.source = source;
        trafficInfo = new TrafficInfo(TrafficNodes.NetIn, this);
        TrafficAnalyzer.getInstance().addTrafficInfo(trafficInfo);
    }

    @Override
    public void prepareRedirect() {
        if (debug) Log.i(TAG, "prepareStream");
        redirectOff();
    }

    @Override
    public void redirectOn() {
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
    public void onReceiveData(byte[] data) {
        if (debug) Log.i(TAG, "onReceiveData");
        if (isRedirecting) {
            if (singlePacket) {
                dataAssembled[0] = Arrays.copyOf(data, btToBtSendSize);
            } else {
                if (data.length != toBtSendSize) return;
                if (debug) Log.i(TAG, "onReceiveData received correct amount of bytes");
                for (int i = 0; i < toBtFactor; i++) {
                  dataAssembled[i] = Arrays.copyOf(Arrays.copyOfRange(data, i * btSignificantBytes, (i + 1) * btSignificantBytes), btToBtSendSize);
                }
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
