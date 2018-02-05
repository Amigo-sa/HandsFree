package by.citech.handsfree.common;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import by.citech.handsfree.network.INetListener;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class HandlerExtended
        extends Handler {

    private static final String STAG = Tags.HandlerExtended;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private INetListener iNetListener;

    public HandlerExtended (INetListener iNetListener) {
        super();
        this.iNetListener = iNetListener;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case StatusMessages.SRV_ONMESSAGE:
                Timber.i("handleMessage SRV_ONMESSAGE");
                break;
            case StatusMessages.SRV_ONCLOSE:
                Timber.i("handleMessage SRV_ONCLOSE");
                iNetListener.srvOnClose();
                break;
            case StatusMessages.SRV_ONOPEN:
                Timber.i("handleMessage SRV_ONOPEN");
                iNetListener.srvOnOpen();
                break;
            case StatusMessages.SRV_ONPONG:
                Timber.i("handleMessage SRV_ONPONG");
                break;
            case StatusMessages.SRV_ONFAILURE:
                Timber.e("handleMessage SRV_ONFAILURE");
                iNetListener.srvOnFailure();
                break;
            case StatusMessages.SRV_ONDEBUGFRAMERX:
                Timber.i("handleMessage SRV_ONDEBUGFRAMERX");
                break;
            case StatusMessages.SRV_ONDEBUGFRAMETX:
                Timber.i("handleMessage SRV_ONDEBUGFRAMETX");
                break;
            case StatusMessages.CLT_ONOPEN:
                Timber.i("handleMessage CLT_ONOPEN");
                iNetListener.cltOnOpen();
                break;
            case StatusMessages.CLT_ONMESSAGE_BYTES:
                Timber.i("handleMessage CLT_ONMESSAGE_BYTES");
                break;
            case StatusMessages.CLT_ONMESSAGE_TEXT:
                Timber.i("handleMessage CLT_ONMESSAGE_TEXT");
                iNetListener.cltOnMessageText((String) msg.obj);
                break;
            case StatusMessages.CLT_ONCLOSING:
                Timber.i("handleMessage CLT_ONCLOSING");
                break;
            case StatusMessages.CLT_ONCLOSED:
                iNetListener.cltOnClose();
                Timber.i("handleMessage CLT_ONCLOSED");
                break;
            case StatusMessages.CLT_ONFAILURE:
                iNetListener.cltOnFailure();
                Timber.e("handleMessage CLT_ONFAILURE");
                break;
            case StatusMessages.CLT_CANCEL:
                Timber.i("handleMessage CLT_CANCEL");
                break;
            default:
                Timber.e("handleMessage DEFAULT");
                break;
        }
    }

}
