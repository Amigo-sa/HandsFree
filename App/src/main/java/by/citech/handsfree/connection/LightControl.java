package by.citech.handsfree.connection;

import android.os.Handler;

import by.citech.vincentwillemvangogh.connection.fsm.EConnectionReport;
import by.citech.vincentwillemvangogh.connection.fsm.EConnectionState;
import by.citech.vincentwillemvangogh.connection.fsm.IConnectionFsmListener;
import by.citech.vincentwillemvangogh.connection.fsm.IConnectionFsmReporter;
import by.citech.vincentwillemvangogh.exchange.IRx;
import by.citech.vincentwillemvangogh.messaging.AutBMsgSendData;
import by.citech.vincentwillemvangogh.messaging.AutBProtocolHandler.IMsgConsumerRegister;
import by.citech.vincentwillemvangogh.messaging.IMsgEncodingTransmitter;
import by.citech.vincentwillemvangogh.messaging.MsgBase;
import by.citech.vincentwillemvangogh.parameters.Tags;
import by.citech.vincentwillemvangogh.settings.Settings;
import by.citech.vincentwillemvangogh.ui.IMsgToUi;
import timber.log.Timber;

import static by.citech.vincentwillemvangogh.connection.LightConfig.EArea;
import static by.citech.vincentwillemvangogh.connection.LightConfig.ELight;
import static by.citech.vincentwillemvangogh.connection.LightHelper.EConvertion.ToConfig;
import static by.citech.vincentwillemvangogh.connection.LightHelper.EConvertion.ToData;
import static by.citech.vincentwillemvangogh.connection.LightHelper.convert;
import static by.citech.vincentwillemvangogh.connection.LightHelper.getInitConfig;
import static by.citech.vincentwillemvangogh.connection.LightHelper.getInitData;
import static by.citech.vincentwillemvangogh.connection.LightHelper.update;
import static by.citech.vincentwillemvangogh.connection.fsm.EConnectionReport.GotInitData;
import static by.citech.vincentwillemvangogh.connection.fsm.EConnectionState.GettingInitData;
import static by.citech.vincentwillemvangogh.messaging.AutBEMsgType.SEND_DATA;
import static by.citech.vincentwillemvangogh.messaging.AutBMsgAdaptor.getMsgGetData;
import static by.citech.vincentwillemvangogh.messaging.AutBMsgAdaptor.getMsgSendData;

public class LightControl
        implements IMsgEncodingTransmitter, IMsgConsumerRegister,
        IRx<MsgBase>, IConnectionFsmListener, IConnectionFsmReporter {

    private static final String STAG = Tags.LightControl;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private LightConfig lightState;
    private IPostLightControl postLightControl;
    private IMsgToUi msgToUi;
    private Handler handler;

    //--------------------- singleton

    private static volatile LightControl instance = null;

    private LightControl() {
    }

    public static LightControl getInstance() {
        if (instance == null) {
            synchronized (LightControl.class) {
                if (instance == null) {instance = new LightControl();}}}
        return instance;
    }

    //--------------------- getters and setters

    public LightControl setPostLightControl(IPostLightControl postLightControl) {
        this.postLightControl = postLightControl;
        return this;
    }

    public LightControl setMsgToUi(IMsgToUi msgToUi) {
        this.msgToUi = msgToUi;
        return this;
    }

    public LightControl setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    //--------------------- IPreLightControl & AutBMsgSendData transmitting

    void tryToToggleLight(boolean isOn, EArea area, ELight light) {
        if (debug) Timber.i("tryToToggleLight");
        if (lightState == null) lightState = getInitConfig();
        byte[] toTransmit = getInitData();
        convert(ToData, toTransmit, update(lightState, isOn, area, light));
        transmit(getMsgSendData(toTransmit));
        if (handler != null) handler.postDelayed(() -> transmit(getMsgGetData()), Settings.btSendGetLatencyMs);
    }

    //--------------------- AutBMsgSendData receiving

    @Override
    public void onRx(MsgBase received) {
        if (debug) Timber.i("onRx received AutBMsgSendData");
        if (lightState == null) lightState = getInitConfig();
        convert(ToConfig, ((AutBMsgSendData) received).getDataField(), lightState);
        if (postLightControl != null) toUi(() -> postLightControl.setLight(lightState));
        if (getConnectionFsmState() == GettingInitData) reportToConnectionFsm(GettingInitData, GotInitData, TAG);
    }

    @Override
    public void onRxFinished() {
        if (debug) Timber.i("onRxFinished");
        unregisterMsgConsumer(this, SEND_DATA);
    }

    //--------------------- IActivityFsmListener

    @Override
    public void onConnectionFsmStateChange(EConnectionState from, EConnectionState to, EConnectionReport why) {
        if (to == GettingInitData) {
            if (debug) Timber.i("onConnectionFsmStateChange transmit AutBMsgGetData");
            transmit(getMsgGetData());
        }
    }

    //--------------------- additional

    private void toUi(Runnable r) {
        if (msgToUi != null) msgToUi.sendToUiRunnable(false, r);
    }

}
