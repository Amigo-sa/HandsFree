package by.citech.handsfree.exchange.consumers;

import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.ESeverityLevel;
import by.citech.handsfree.statistic.TrafficAnalyzer;

public class ToBluetooth
        implements ITransmitterCtrl, ITransmitter,
        TrafficAnalyzer.ITrafficUpdate, IPrepareObject, ISettingsCtrl {

    private static final String TAG = Tags.ToBluetooth;
    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private int btFactor;
    private int btToBtSendSize;
    private int btSignificantBytes;
    private int btSendSize;
    private boolean btSinglePacket;
    private boolean btSignificantAll;
    private byte[][] dataAssembled;
    //  private TrafficInfo trafficInfo;
    private boolean isStreaming;
    private boolean isPrepared;
    private boolean isFinished;
    private StorageData<byte[][]> storage;

    {
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        takeSettings();
        applySettings(null);
        return true;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        btSignificantAll = Settings.btSignificantAll;
        btSinglePacket = Settings.btSinglePacket;
        btFactor = Settings.btFactor;
        btToBtSendSize = Settings.bt2BtPacketSize;
        btSignificantBytes = btSignificantAll ? btToBtSendSize : Settings.btSignificantBytes;
        btSendSize = Settings.btSendSize;
        return true;
    }

    @Override
    public boolean applySettings(ESeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        return true;
    }

    //--------------------- constructor

    public ToBluetooth(StorageData<byte[][]> storage) throws Exception {
        if (storage == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        this.storage = storage;
        //TODO: доработать анализатор траффика
//      trafficInfo = new TrafficInfo(ETrafficNodes.NetIn, this);
//      TrafficAnalyzer.getInstance().addTrafficInfo(trafficInfo);
    }

    //--------------------- IReceiverCtrl

    @Override
    public void prepareStream(ITransmitter receiver) throws Exception {
        if (isFinished) {
            if (debug) Log.w(TAG, "prepareStream stream is finished, return");
            return;
        } else {
            if (debug) Log.i(TAG, "prepareStream");
        }
        if (debug) Log.w(TAG, String.format(Locale.US, "prepareStream parameters is:" +
                        " btSignificantAll is %b," +
                        " btSinglePacket is %b," +
                        " btFactor is %d," +
                        " btToBtSendSize is %d," +
                        " btSignificantBytes is %d," +
                        " btSendSize is %d",
                btSignificantAll,
                btSinglePacket,
                btFactor,
                btToBtSendSize,
                btSignificantBytes,
                btSendSize
        ));
        isPrepared = true;
    }

    @Override
    public void finishStream() {
        if (debug) Log.i(TAG, "finishStream");
        isFinished = true;
        streamOff();
        storage = null;
    }

    @Override
    public void streamOn() {
        if (isFinished) {
            if (debug) Log.w(TAG, "streamOn stream is finished, return");
            return;
        } else {
            if (debug) Log.i(TAG, "streamOn");
        }
        isStreaming = true;
    }

    @Override
    public void streamOff() {
        if (debug) Log.i(TAG, "streamOff");
        isStreaming = false;
        storage.clear();
    }

    @Override
    public boolean isStreaming() {
        return isStreaming;
    }

    @Override
    public boolean isReadyToStream() {
        if (isFinished) {
            if (debug) Log.w(TAG, "isReadyToStream finished");
            return false;
        } else if (!isPrepared) {
            if (debug) Log.w(TAG, "isReadyToStream not prepared");
            return false;
        } else {
            return true;
        }
    }

    //--------------------- IReceiver

    @Override
    public void sendData(byte[] data) {
        if (data == null) {
            if (debug) Log.i(TAG, "sendData byte[]" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (!isStreaming() || !isReadyToStream()) return;
        if (dataAssembled == null) {
            dataAssembled = new byte[btFactor][btToBtSendSize];
        }
        if (btSinglePacket) {
            dataAssembled[0] = Arrays.copyOf(data, btToBtSendSize);
        } else {
            int receivedDataSize = data.length;
            if (receivedDataSize != btSendSize) {
                if (debug)
                    Log.e(TAG, String.format("sendData received wrong amount of data: %d bytes", receivedDataSize));
                return;
            } else {
                if (debug)
                    Log.i(TAG, String.format("sendData received correct amount of data: %d bytes", receivedDataSize));
            }
            for (int i = 0; i < btFactor; i++) {
                if (btSignificantAll) {
                    dataAssembled[i] = Arrays.copyOfRange(data, i * btSignificantBytes, (i + 1) * btSignificantBytes);
                } else {
                    dataAssembled[i] = Arrays.copyOf(Arrays.copyOfRange(data, i * btSignificantBytes, (i + 1) * btSignificantBytes), btToBtSendSize);
                }
            }
            if (debug) Log.i(TAG, "sendData data assembled");
        }
        if (isStreaming() && isReadyToStream()) {
            storage.putData(dataAssembled);
        }
        dataAssembled = null;
    }

}
