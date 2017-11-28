package by.citech.debug;

import android.util.Log;

import by.citech.codec.audio.AudioCodec;
import by.citech.codec.audio.AudioCodecType;
import by.citech.data.StorageData;
import by.citech.logic.ConnectorPlayAudio;
import by.citech.logic.ConnectorRecordAudio;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class DebugMicToAudLooper
        extends Thread
        implements IDebugListener {

    private static final String TAG = Tags.MIC2AUD_LOOPER;
    private static final boolean debug = Settings.debug;
    private static final AudioCodecType codecType = Settings.codecType;

    private StorageData<short[]> storageMic;
    private StorageData<short[]> sourceAud;
    private boolean isRunning;
    private boolean isActive;
    private AudioCodec audioCodec;
    private ConnectorPlayAudio playAudio;
    private ConnectorRecordAudio recordAudio;

    public DebugMicToAudLooper(StorageData<short[]> storageMic, StorageData<short[]> sourceAud) {
        this.storageMic = storageMic;
        this.sourceAud = sourceAud;
        audioCodec = new AudioCodec(codecType);
        playAudio = new ConnectorPlayAudio(sourceAud);
        recordAudio = new ConnectorRecordAudio(storageMic);
    }

    @Override
    public void run() {
        if (debug) Log.i(TAG, "run");
        isActive = true;
        new Thread(() -> playAudio.start()).start();
        recordAudio.start();
        audioCodec.initiateDecoder();
        audioCodec.initiateEncoder();
        while (isActive) {
            while (!isRunning) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (isRunning) {
                if (!storageMic.isEmpty()) {
                    if (debug) Log.i(TAG, "run encode -> decode");
                    //sourceAud.putData(audioCodec.getDecodedData(audioCodec.getEncodedData(storageMic.getData())));
                    sourceAud.putData((storageMic.getData()));
                }
            }
            audioCodec.initiateDecoder();
            audioCodec.initiateEncoder();
        }
        recordAudio.stop();
        playAudio.stop();
        if (debug) Log.w(TAG, "run done");
    }

    public void deactivate() {
        if (debug) Log.i(TAG, "deactivate");
        isActive = false;
        isRunning = false;
    }

    @Override
    public void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        isRunning = true;
    }

    @Override
    public void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        isRunning = false;
    }
}
