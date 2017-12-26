package by.citech.handsfree.logic;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import by.citech.handsfree.network.INetListener;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

class HandlerExtended
        extends Handler {

    private static final String STAG = Tags.HandlerExtended;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private INetListener iNetListener;

    HandlerExtended (INetListener iNetListener) {
        super();
        this.iNetListener = iNetListener;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case StatusMessages.SRV_ONMESSAGE:
                if (debug) Log.i(TAG, "handleMessage SRV_ONMESSAGE");
                break;
            case StatusMessages.SRV_ONCLOSE:
                if (debug) Log.i(TAG, "handleMessage SRV_ONCLOSE");
                iNetListener.srvOnClose();
                break;
            case StatusMessages.SRV_ONOPEN:
                if (debug) Log.i(TAG, "handleMessage SRV_ONOPEN");
                iNetListener.srvOnOpen();
                break;
            case StatusMessages.SRV_ONPONG:
                if (debug) Log.i(TAG, "handleMessage SRV_ONPONG");
                break;
            case StatusMessages.SRV_ONFAILURE:
                if (debug) Log.e(TAG, "handleMessage SRV_ONFAILURE");
                iNetListener.srvOnFailure();
                break;
            case StatusMessages.SRV_ONDEBUGFRAMERX:
                if (debug) Log.i(TAG, "handleMessage SRV_ONDEBUGFRAMERX");
                break;
            case StatusMessages.SRV_ONDEBUGFRAMETX:
                if (debug) Log.i(TAG, "handleMessage SRV_ONDEBUGFRAMETX");
                break;
            case StatusMessages.CLT_ONOPEN:
                if (debug) Log.i(TAG, "handleMessage CLT_ONOPEN");
                iNetListener.cltOnOpen();
                break;
            case StatusMessages.CLT_ONMESSAGE_BYTES:
                if (debug) Log.i(TAG, "handleMessage CLT_ONMESSAGE_BYTES");
                break;
            case StatusMessages.CLT_ONMESSAGE_TEXT:
                if (debug) Log.i(TAG, "handleMessage CLT_ONMESSAGE_TEXT");
                iNetListener.cltOnMessageText((String) msg.obj);
                break;
            case StatusMessages.CLT_ONCLOSING:
                if (debug) Log.i(TAG, "handleMessage CLT_ONCLOSING");
                break;
            case StatusMessages.CLT_ONCLOSED:
                iNetListener.cltOnClose();
                if (debug) Log.i(TAG, "handleMessage CLT_ONCLOSED");
                break;
            case StatusMessages.CLT_ONFAILURE:
                iNetListener.cltOnFailure();
                if (debug) Log.e(TAG, "handleMessage CLT_ONFAILURE");
                break;
            case StatusMessages.CLT_CANCEL:
                if (debug) Log.i(TAG, "handleMessage CLT_CANCEL");
                break;
            default:
                if (debug) Log.e(TAG, "handleMessage DEFAULT");
                break;
        }
    }

}
