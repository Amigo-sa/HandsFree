package by.citech.websocketduplex.util;

public final class StatusMessages {
    public static final String WEBSOCKET_OPENED = "WEBSOCKET_OPENED";
    public static final String WEBSOCKET_NOTAVAILABLE = "WEBSOCKET_NOTAVAILABLE";
    public static final String WEBSOCKET_FAILURE = "WEBSOCKET_FAILURE";
    public static final String WEBSOCKET_TIMEOUT = "WEBSOCKET_TIMEOUT";
    public static final String WEBSOCKET_CLOSED = "WEBSOCKET_CLOSED";
    public static final String WEBSOCKET_CLOSING = "WEBSOCKET_CLOSING";
    public static final String WEBSOCKET_CANCEL = "WEBSOCKET_CANCEL";

    public static final String SRV_CANTSTART = "SRV_CANTSTART";
    public static final String SRV_STARTED = "SRV_STARTED";
    public static final int SRV_ONMESSAGE = 0;
    public static final int SRV_ONCLOSE = 1;
    public static final int SRV_ONOPEN = 2;
    public static final int SRV_ONPONG = 3;
    public static final int SRV_ONEXCEPTION = 4;
    public static final int SRV_ONDEBUGFRAMERX = 5;
    public static final int SRV_ONDEBUGFRAMETX = 6;
}
