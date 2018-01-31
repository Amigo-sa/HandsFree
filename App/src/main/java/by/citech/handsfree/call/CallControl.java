package by.citech.handsfree.call;

import android.os.Handler;

import by.citech.handsfree.activity.fsm.ActivityFsm;
import by.citech.handsfree.activity.fsm.EActivityReport;
import by.citech.handsfree.activity.fsm.EActivityState;
import by.citech.handsfree.bluetoothlegatt.fsm.BtFsm;
import by.citech.handsfree.bluetoothlegatt.fsm.EBtReport;
import by.citech.handsfree.bluetoothlegatt.fsm.EBtState;

import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.threading.IThreading;
import timber.log.Timber;

public class CallControl
        implements IThreading,
        ActivityFsm.IActivityFsmListener,
        BtFsm.IBtFsmListener,
        CallFsm.ICallFsmListener {

    private static final String STAG = Tags.ConnectionControl;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private Runnable checkIfFound = () -> {
        if (debug) Timber.tag(TAG).i("checkIfFound");
        EConnectionState state = getConnectionFsmState();
        if (state != Found) reportToConnectionFsm(state, SearchStopped, TAG);
    };
    private Runnable checkIfConnected = () -> {
        if (debug) Timber.tag(TAG).i("checkIfConnected");
        EConnectionState state = getConnectionFsmState();
        if (state != Connected) reportToConnectionFsm(state, ConnectStopped, TAG);
    };
    private Runnable checkIfGotStatus = () -> {
        if (debug) Timber.tag(TAG).i("checkIfGotStatus");
        EConnectionState state = getConnectionFsmState();
        if (state != EConnectionState.GotStatus) reportToConnectionFsm(state, GettingStatusStopped, TAG);
    };
    private Runnable checkIfGotInitData = () -> {
        if (debug) Timber.tag(TAG).i("checkIfGotInitData");
        EConnectionState state = getConnectionFsmState();
        if (state != GotInitData) reportToConnectionFsm(state, GettingInitDataStopped, TAG);
    };

    private Handler handler;

    //--------------------- singleton

    private static volatile CallControl instance = null;

    private CallControl() {
    }

    public static CallControl getInstance() {
        if (instance == null) {
            synchronized (CallControl.class) {
                if (instance == null) {
                    instance = new CallControl();
                }
            }
        }
        return instance;
    }

    //--------------------- getters and setters

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    //--------------------- IActivityFsmListener

    @Override
    public void onBtFsmStateChange(EBtState from, EBtState to, EBtReport why) {
        switch (to) {
            case DeviceChosen:
                reportToConnectionFsm(to, SearchStarted, TAG); break;
            case Searching:
                add(checkIfFound, 5000); break;
            case NotFound:
                reportToConnectionFsm(to, SearchStarted, TAG); break;
            case Found:
                remove(checkIfFound);
                reportToConnectionFsm(to, ConnectStarted, TAG); break;
            case Connecting:
                add(checkIfConnected, 20000); break;
            case Connected:
                remove(checkIfConnected);
                reportToConnectionFsm(to, GettingStatusStarted, TAG); break;
            case GotStatus:
                remove(checkIfGotStatus);
                reportToConnectionFsm(to, GettingInitDataStarted, TAG); break;
            case Disconnected:
                reportToConnectionFsm(to, SearchStarted, TAG); break;
            case GettingStatus:
                add(checkIfGotStatus, 5000); break;
            case GettingInitData:
                add(checkIfGotInitData, 5000); break;
            case GotInitData:
                remove(checkIfGotInitData); break;
            case BtNotSupported:
            case BtPrepared:
            case DeviceNotChosen:
            case Incompatible:
            case TurnedOn:
            case Failure:
            default: break;
        }
    }

    //--------------------- add and remove delayed

    private void add(Runnable r, long delay) {
        if (handler != null) handler.postDelayed(r, delay);
    }

    private void remove(Runnable r) {
        if (handler != null) handler.removeCallbacks(r);
    }

    private void removeAll() {
        if (handler != null) {
            handler.removeCallbacks(checkIfFound);
            handler.removeCallbacks(checkIfConnected);
            handler.removeCallbacks(checkIfGotStatus);
            handler.removeCallbacks(checkIfGotInitData);
        }
    }

    //--------------------- IActivityFsmListener

    @Override
    public void onActivityFsmStateChange(EActivityState from, EActivityState to, EActivityReport why) {
//        if (to == LightA) {
//            registerConnectionFsmListener(this, TAG);
//            reportToBtFsm(getBtFsmState(), GettingInitDataStart, TAG);
//        } else if (why == LightA2ScanAPressed) {
//            removeAll();
//            unregisterConnectionFsmListener(this, TAG);
//        }
    }

}
