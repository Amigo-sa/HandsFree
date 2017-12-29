package by.citech.handsfree.exchange;

import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.debug.ITrafficUpdate;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.SeverityLevel;

public class ToBluetooth
        implements IReceiverCtrl, IReceiver, ITrafficUpdate, IPrepareObject, ISettingsCtrl {

    private static final String TAG = Tags.TO_BLUETOOTH;
    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private int btFactor;
    private int btToBtSendSize;
    private int btSignificantBytes;
    private int btSendSize;
    private boolean btSinglePacket;
    private boolean btSignificantAll;
    private byte[][] dataAssembled;
    private IReceiverReg iReceiverReg;
    //  private TrafficInfo trafficInfo;
    private boolean isRedirecting = false;
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
        btToBtSendSize = Settings.bt2btPacketSize;
        btSignificantBytes = btSignificantAll ? btToBtSendSize : Settings.btSignificantBytes;
        btSendSize = Settings.btSendSize;
        return true;
    }

    @Override
    public boolean applySettings(SeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        dataAssembled = new byte[btFactor][btToBtSendSize];
        return true;
    }

    //--------------------- constructor

    public ToBluetooth(IReceiverReg iReceiverReg, StorageData<byte[][]> storage) throws Exception {
        if (iReceiverReg == null || storage == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        this.iReceiverReg = iReceiverReg;
        this.storage = storage;
        //TODO: доработать анализатор траффика
//      trafficInfo = new TrafficInfo(TrafficNodes.NetIn, this);
//      TrafficAnalyzer.getInstance().addTrafficInfo(trafficInfo);
    }

    @Override
    public void prepareRedirect() {
        if (debug) Log.i(TAG, "prepareRedirect");
        Log.w(TAG, String.format(Locale.US, "redirectOn parameters is:" +
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
    }

    @Override
    public void redirectOn() {
        if (debug) Log.i(TAG, "redirectOn");
        isRedirecting = true;
        iReceiverReg.registerReceiver(this);
        if (Settings.debug) Log.i(TAG, "redirectOn done");
    }

    @Override
    public void redirectOff() {
        if (debug) Log.i(TAG, "redirectOff");
        isRedirecting = false;
        storage.clear();
        storage = null;
        iReceiverReg.registerReceiver(null);
        iReceiverReg = null;
    }

    @Override
    public void onReceiveData(byte[] data) {
        if (data == null) {
            if (debug) Log.i(TAG, "onReceiveData byte[]" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (isRedirecting) {
            if (btSinglePacket) {
                dataAssembled[0] = Arrays.copyOf(data, btToBtSendSize);
            } else {
                int receivedDataSize = data.length;
                if (receivedDataSize != btSendSize) {
                    Log.e(TAG, String.format("onReceiveData received wrong amount of data: %d bytes", receivedDataSize));
                    return;
                } else {
                    if (debug) Log.w(TAG, String.format("onReceiveData received correct amount of data: %d bytes", receivedDataSize));
                }
                for (int i = 0; i < btFactor; i++) {
//                  if (debug) Log.w(TAG, String.format("onReceiveData dataAssembled[%d] assign to range of data[] from positions %d to %d, btFactor is %d",
//                          i, i * btSignificantBytes, (i + 1) * btSignificantBytes, btFactor
//                  ));
                    if (btSignificantAll) {
                        dataAssembled[i] = Arrays.copyOfRange(data, i * btSignificantBytes, (i + 1) * btSignificantBytes);
                    } else {
                        dataAssembled[i] = Arrays.copyOf(Arrays.copyOfRange(data, i * btSignificantBytes, (i + 1) * btSignificantBytes), btToBtSendSize);
                    }
                }
                if (debug) Log.w(TAG, "onReceiveData data assembled, put");
            }
            storage.putData(dataAssembled);
        }
    }

}
