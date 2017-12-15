package by.citech.handsfree.exchange;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.debug.ITrafficUpdate;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public class ToBluetooth
        implements IReceiverCtrl, IReceiver, ITrafficUpdate {

    private static final String TAG = Tags.TO_BLUETOOTH;
    private static final boolean debug = Settings.debug;

    //--------------------- settings

    private int btFactor;
    private int btToBtSendSize;
    private int btSignificantBytes;
    private int btSendSize;
    private boolean btSinglePacket;
    private boolean btSignificantAll;
    private byte[][] dataAssembled;

    {
        initiate();
    }

    private void initiate() {
        takeSettings();
        applySettings();
    }

    private void takeSettings() {
        btSignificantAll = Settings.btSignificantAll;
        btSinglePacket = Settings.btSinglePacket;
        btFactor = Settings.btFactor;
        btToBtSendSize = Settings.bt2btPacketSize;
        btSignificantBytes = btSignificantAll ? btToBtSendSize : Settings.btSignificantBytes;
        btSendSize = Settings.btSendSize;
    }

    private void applySettings() {
        dataAssembled = new byte[btFactor][btToBtSendSize];
    }

    //--------------------- non-settings

    private IReceiverReg iReceiverReg;
//  private TrafficInfo trafficInfo;
    private boolean isRedirecting = false;
    private StorageData<byte[][]> storage;

    public ToBluetooth(IReceiverReg iReceiverReg, StorageData<byte[][]> storage) {
        if (iReceiverReg == null || storage == null) {
            Log.e(TAG, "ToBluetooth" + StatusMessages.ERR_PARAMETERS);
            return;
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
        if (debug) Log.i(TAG, "onReceiveData byte[]");
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
                    if (debug) Log.w(TAG, String.format("onReceiveData dataAssembled[%d] assign to range of data[] from positions %d to %d, btFactor is %d",
                            i,
                            i * btSignificantBytes,
                            (i + 1) * btSignificantBytes,
                            btFactor
                    ));
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
