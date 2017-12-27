package by.citech.handsfree.common;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface IPrepareObject {

    String TAG = Tags.I_CHECK;
    String MSG_ERR = StatusMessages.ERR_NOT_OVERRIDED;

    default boolean prepareObject() {
        return false;
    }

    default boolean isObjectPrepared() {
        return false;
    }

}
