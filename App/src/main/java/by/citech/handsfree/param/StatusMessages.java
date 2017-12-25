package by.citech.handsfree.param;

public final class StatusMessages {

    public static final String ERR_NOT_OVERRIDED = " usage of not overrided default method";
    public static final String WRN_NOT_OVERRIDED = " usage of not overrided default method";
    public static final String ERR_ID_ALREADY_IN_USE = " this id already used";
    public static final String ERR_PARAMETERS    = " illegal parameters";

    public static final String SRV_CANTSTART = "SRV_CANTSTART";
    public static final String SRV_STARTED = "SRV_STARTED";

    public static final int SRV_ONMESSAGE = 0;
    public static final int SRV_ONCLOSE = 1;
    public static final int SRV_ONOPEN = 2;
    public static final int SRV_ONPONG = 3;
    public static final int SRV_ONFAILURE = 4;
    public static final int SRV_ONDEBUGFRAMERX = 5;
    public static final int SRV_ONDEBUGFRAMETX = 6;

    public static final int CLT_ONOPEN = 10;
    public static final int CLT_ONMESSAGE_BYTES = 11;
    public static final int CLT_ONMESSAGE_TEXT = 12;
    public static final int CLT_ONCLOSING = 13;
    public static final int CLT_ONCLOSED = 14;
    public static final int CLT_ONFAILURE = 15;
    public static final int CLT_CANCEL = 16;

    public static final String CLT_MESSAGE_CANT = "CLT_MESSAGE_CANT";
    public static final String CLT_MESSAGE_SENDED = "CLT_MESSAGE_SENDED";
}
