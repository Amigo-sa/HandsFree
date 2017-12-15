package by.citech.handsfree.logic;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import by.citech.handsfree.network.INetListener;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public class HandlerExtended
        extends Handler {

    private INetListener iNetListener;

    public HandlerExtended (INetListener iNetListener) {
        super();
        this.iNetListener = iNetListener;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case StatusMessages.SRV_ONMESSAGE:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage SRV_ONMESSAGE");
                //if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, String.format("handleMessage SRV_ONMESSAGE %s", ((WebSocketFrame) msg.obj).getTextPayload()));
                break;
            case StatusMessages.SRV_ONCLOSE:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage SRV_ONCLOSE");
                iNetListener.srvOnClose();
                break;
            case StatusMessages.SRV_ONOPEN:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage SRV_ONOPEN");
                iNetListener.srvOnOpen();
                break;
            case StatusMessages.SRV_ONPONG:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage SRV_ONPONG");
                break;
            case StatusMessages.SRV_ONFAILURE:
                if (Settings.debug) Log.e(Tags.ACT_DEVICECTRL, "handleMessage SRV_ONFAILURE");
                iNetListener.srvOnFailure();
                break;
            case StatusMessages.SRV_ONDEBUGFRAMERX:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage SRV_ONDEBUGFRAMERX");
                break;
            case StatusMessages.SRV_ONDEBUGFRAMETX:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage SRV_ONDEBUGFRAMETX");
                break;
            case StatusMessages.CLT_ONOPEN:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage CLT_ONOPEN");
                iNetListener.cltOnOpen();
                break;
            case StatusMessages.CLT_ONMESSAGE_BYTES:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage CLT_ONMESSAGE_BYTES");
                break;
            case StatusMessages.CLT_ONMESSAGE_TEXT:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage CLT_ONMESSAGE_TEXT");
                iNetListener.cltOnMessageText((String) msg.obj);
                break;
            case StatusMessages.CLT_ONCLOSING:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage CLT_ONCLOSING");
                break;
            case StatusMessages.CLT_ONCLOSED:
                iNetListener.cltOnClose();
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage CLT_ONCLOSED");
                break;
            case StatusMessages.CLT_ONFAILURE:
                iNetListener.cltOnFailure();
                if (Settings.debug) Log.e(Tags.ACT_DEVICECTRL, "handleMessage CLT_ONFAILURE");
                break;
            case StatusMessages.CLT_CANCEL:
                if (Settings.debug) Log.i(Tags.ACT_DEVICECTRL, "handleMessage CLT_CANCEL");
                break;
            default:
                if (Settings.debug) Log.e(Tags.ACT_DEVICECTRL, "handleMessage DEFAULT");
                break;
        }
    }

}
