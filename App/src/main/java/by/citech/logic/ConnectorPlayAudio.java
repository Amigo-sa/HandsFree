package by.citech.logic;

import android.util.Log;

import by.citech.data.StorageData;
import by.citech.exchange.IReceiver;
import by.citech.exchange.IReceiverCtrl;
import by.citech.exchange.IReceiverReg;
import by.citech.exchange.ToAudio;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class ConnectorPlayAudio implements IReceiverReg {

    private static final String TAG = Tags.AUDPLA_CONNECTOR;
    private static final boolean debug = Settings.debug;

    private IReceiver iReceiver;
    private IReceiverCtrl iReceiverCtrl;
    private StorageData<byte[]> source;
    private boolean isRunning;

    //--------------------- singleton

    private static volatile ConnectorPlayAudio instance = null;

    private ConnectorPlayAudio() {
    }

    public static ConnectorPlayAudio getInstance() {
        if (instance == null) {
            synchronized (ConnectorPlayAudio.class) {
                if (instance == null) {
                    instance = new ConnectorPlayAudio();
                }
            }
        }
        return instance;
    }

    //--------------------- getters and setters

    public void setSource(StorageData<byte[]> source) {
        this.source = source;
    }

    public void start() {
        if (source == null) {
            Log.e(TAG, "prepare illegal parameters");
            return;
        }
        ToAudio toAudio = new ToAudio(this);
        toAudio.prepare();
        iReceiverCtrl = toAudio;
        new Thread(toAudio::run).start();
        run();
    }

    private void run() {
        isRunning = true;
        while (isRunning) {
            if (!source.isEmpty()) {
                iReceiver.onReceiveData(source.getData());
            } else {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        if (debug) Log.i(TAG, "stop");
        isRunning = false;
        if (iReceiver != null){
            iReceiverCtrl.redirectOff();
        }
        if (source != null) {
            source.clear();
        }
    }

    @Override
    public void registerReceiver(IReceiver iReceiver) {
        this.iReceiver = iReceiver;
    }
}
