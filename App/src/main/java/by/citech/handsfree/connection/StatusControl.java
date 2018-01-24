package by.citech.handsfree.connection;

import by.citech.vincentwillemvangogh.connection.fsm.EConnectionReport;
import by.citech.vincentwillemvangogh.connection.fsm.EConnectionState;
import by.citech.vincentwillemvangogh.connection.fsm.IConnectionFsmListener;
import by.citech.vincentwillemvangogh.connection.fsm.IConnectionFsmReporter;
import by.citech.vincentwillemvangogh.exchange.IRx;
import by.citech.vincentwillemvangogh.messaging.AutBProtocolHandler.IMsgConsumerRegister;
import by.citech.vincentwillemvangogh.messaging.IMsgEncodingTransmitter;
import by.citech.vincentwillemvangogh.messaging.MsgBase;
import by.citech.vincentwillemvangogh.parameters.Tags;
import by.citech.vincentwillemvangogh.settings.Settings;
import timber.log.Timber;

import static by.citech.vincentwillemvangogh.connection.fsm.EConnectionReport.GotStatus;
import static by.citech.vincentwillemvangogh.connection.fsm.EConnectionState.GettingStatus;
import static by.citech.vincentwillemvangogh.messaging.AutBEMsgType.STATUS_OK;
import static by.citech.vincentwillemvangogh.messaging.AutBMsgAdaptor.getMsgGetStatus;

public class StatusControl
        implements IMsgEncodingTransmitter, IMsgConsumerRegister,
        IRx<MsgBase>, IConnectionFsmListener, IConnectionFsmReporter {

    private static final String STAG = Tags.StatusControl;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    //--------------------- singleton

    private static volatile StatusControl instance = null;

    private StatusControl() {
    }

    public static StatusControl getInstance() {
        if (instance == null) {
            synchronized (StatusControl.class) {
                if (instance == null) {
                    instance = new StatusControl();
                }
            }
        }
        return instance;
    }

    //--------------------- AutBMsgStatusOkData receiving

    @Override
    public void onRx(MsgBase received) {
        if (getConnectionFsmState() == GettingStatus) {
            if (debug) Timber.tag(TAG).i("onRx received AutBMsgStatusOk");
            reportToConnectionFsm(GettingStatus, GotStatus, TAG);
        }
    }

    @Override
    public void onRxFinished() {
        if (debug) Timber.tag(TAG).i("onRxFinished");
        unregisterMsgConsumer(this, STATUS_OK);
    }

    //--------------------- IActivityFsmListener

    @Override
    public void onConnectionFsmStateChange(EConnectionState from, EConnectionState to, EConnectionReport why) {
        if (to == GettingStatus) {
            if (debug) Timber.tag(TAG).i("onConnectionFsmStateChange transmit AutBMsgGetStatus");
            transmit(getMsgGetStatus());
        }
    }

}
