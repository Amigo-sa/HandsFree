package by.citech.debug;

import android.util.Log;

import by.citech.codec.audio.AudioCodec;
import by.citech.codec.audio.AudioCodecType;
import by.citech.data.StorageData;
import by.citech.exchange.FromAudioIn;
import by.citech.exchange.IReceiver;
import by.citech.exchange.IReceiverCtrl;
import by.citech.exchange.IReceiverReg;
import by.citech.exchange.ITransmitter;
import by.citech.exchange.ITransmitterCtrl;
import by.citech.exchange.ToBluetooth;
import by.citech.logic.IBase;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class AudIn2BtLooper
        implements IDebugListener, IBase, ITransmitter, IReceiverReg {

    private static final String TAG = Tags.AUDIN2BT_LOOPER;
    private static final boolean debug = Settings.debug;

    //--------------------- settings

    private AudioCodecType codecType;
    private AudioCodec audioCodec;

    {
        initiate();
    }

    private void initiate() {
        takeSettings();
        applySettings();
    }

    private void takeSettings() {
        codecType = Settings.audioCodecType;
    }

    private void applySettings() {
        audioCodec = new AudioCodec(codecType);
    }

    //--------------------- non-settings

    private IReceiverCtrl iReceiverCtrl;
    private ITransmitterCtrl iTransmitterCtrl;
    private IReceiver iReceiver;

    public AudIn2BtLooper(StorageData<byte[][]> micToBtStorage) {
        iTransmitterCtrl = new FromAudioIn(this);
        iReceiverCtrl = new ToBluetooth(this, micToBtStorage);
    }

    @Override
    public void baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        stopDebug();
        iReceiverCtrl = null;
        iTransmitterCtrl = null;
    }

    @Override
    public void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        if (iReceiver == null) {
            audioCodec.initiateEncoder();
            audioCodec.initiateDecoder();
            iReceiverCtrl.prepareRedirect();
            iReceiverCtrl.redirectOn();
            iTransmitterCtrl.prepareStream();
            new Thread(() -> iTransmitterCtrl.streamOn()).start();
        }
    }

    @Override
    public void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        iReceiver = null;
        iReceiverCtrl.redirectOff();
        iTransmitterCtrl.streamOff();
    }

    @Override
    public void sendData(short[] data) {
        if (debug) Log.i(TAG, "sendData short[]");
        if (iReceiver != null) {
            iReceiver.onReceiveData(audioCodec.getEncodedData(data));
        }
    }

    @Override
    public void registerReceiver(IReceiver iReceiver) {
        this.iReceiver = iReceiver;
    }

}
