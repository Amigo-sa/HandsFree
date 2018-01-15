package by.citech.handsfree.common;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;

public interface IPrepareObject {

    String TAG = Tags.IPrepareObject;
    String MSG_ERR = StatusMessages.ERR_NOT_OVERRIDED;

    default boolean prepareObject() {
        return false;
    }

    default boolean isObjectPrepared() {
        return false;
    }

}
