package by.citech.handsfree.exchange.consumers;

import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.ESeverityLevel;
import by.citech.handsfree.statistic.TrafficAnalyzer;
import timber.log.Timber;

public class ToBluetooth
        implements IStreamer, IRxComplex,
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
        btSignificantAll = Settings.Bluetooth.btSignificantAll;
        btSinglePacket = Settings.Bluetooth.btSinglePacket;
        btFactor = Settings.Bluetooth.btFactor;
        btToBtSendSize = Settings.Bluetooth.bt2BtPacketSize;
        btSignificantBytes = btSignificantAll ? btToBtSendSize : Settings.Bluetooth.btSignificantBytes;
        btSendSize = Settings.Bluetooth.btSendSize;
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
    public void prepareStream(IRxComplex receiver) throws Exception {
        if (isFinished) {
            Timber.w("prepareStream stream is finished, return");
            return;
        } else {
            Timber.i("prepareStream");
        }
        Timber.w(String.format(Locale.US, "prepareStream parameters is:" +
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
        Timber.i("finishStream");
        isFinished = true;
        streamOff();
        storage = null;
    }

    @Override
    public void streamOn() {
        if (isFinished) {
            Timber.w("streamOn stream is finished, return");
            return;
        } else {
            Timber.i("streamOn");
        }
        isStreaming = true;
    }

    @Override
    public void streamOff() {
        Timber.i("streamOff");
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
            Timber.w("isReadyToStream finished");
            return false;
        } else if (!isPrepared) {
            Timber.w("isReadyToStream not prepared");
            return false;
        } else {
            return true;
        }
    }

    //--------------------- IReceiver

    @Override
    public void sendData(byte[] data) {
        if (data == null) {
            Timber.i("sendData byte[]" + StatusMessages.ERR_PARAMETERS);
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
            Timber.i("sendData data assembled");
        }
        if (isStreaming() && isReadyToStream()) {
            storage.putData(dataAssembled);
        }
        dataAssembled = null;
    }

}
