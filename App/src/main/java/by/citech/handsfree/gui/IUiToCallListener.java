package by.citech.handsfree.gui;

import by.citech.handsfree.logic.CallUi;

public interface IUiToCallListener {

    default void onClickBtnRed() {
        CallUi.getInstance().onClickBtnRed();
    }

    default void onClickBtnGreen() {
        CallUi.getInstance().onClickBtnGreen();
    }

}
