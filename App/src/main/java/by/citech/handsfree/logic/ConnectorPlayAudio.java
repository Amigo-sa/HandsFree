package by.citech.handsfree.logic;

import android.util.Log;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.IReceiver;
import by.citech.handsfree.exchange.IReceiverCtrl;
import by.citech.handsfree.exchange.IReceiverReg;
import by.citech.handsfree.exchange.ToAudioOut;
import by.citech.handsfree.param.Settings;
import by.citech.handsfree.param.Tags;

public class ConnectorPlayAudio implements IReceiverReg {

    private static final String TAG = Tags.AUDPLA_CONNECTOR;
    private static final boolean debug = Settings.debug;

    private IReceiver iReceiver;
    private IReceiverCtrl iReceiverCtrl;
    private StorageData<short[]> source;
    private boolean isRunning;

    public ConnectorPlayAudio(StorageData<short[]> source) {
        this.source = source;
    }

    //--------------------- getters and setters

    public void start() {
        if (source == null) {
            Log.e(TAG, "prepareStream illegal parameters");
            return;
        }
        iReceiverCtrl = new ToAudioOut(this);
        iReceiverCtrl.prepareRedirect();
        new Thread(iReceiverCtrl::redirectOn).start();
        run();
    }

    private void run() {
        if (debug) Log.i(TAG, "run");
        isRunning = true;
        while (isRunning) {
            if (!source.isEmpty() && iReceiver != null) {
                iReceiver.onReceiveData(source.getData());
            } else {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (debug) Log.w(TAG, "run done");
    }

    public void stop() {
        if (debug) Log.i(TAG, "stop");
        isRunning = false;
        if (iReceiverCtrl != null){
            iReceiverCtrl.redirectOff();
            iReceiverCtrl = null;
        }
        if (source != null) {
            source.clear();
            source = null;
        }
        iReceiver = null;
    }

    @Override
    public void registerReceiver(IReceiver iReceiver) {
        if (debug) Log.w(TAG, "registerReceiver");
        this.iReceiver = iReceiver;
    }

}
