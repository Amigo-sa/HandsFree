package by.citech.handsfree.debug;

import android.util.Log;

import by.citech.handsfree.codec.audio.AudioCodec;
import by.citech.handsfree.settings.enumeration.AudioCodecType;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.FromAudioIn;
import by.citech.handsfree.exchange.IReceiver;
import by.citech.handsfree.exchange.IReceiverCtrl;
import by.citech.handsfree.exchange.IReceiverReg;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.exchange.ToBluetooth;
import by.citech.handsfree.logic.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

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
