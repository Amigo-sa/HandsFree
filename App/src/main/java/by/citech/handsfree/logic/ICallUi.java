package by.citech.handsfree.logic;

public interface ICallUi {

    default void onClickBtnRed() {
        CallUi.getInstance().onClickBtnRed();
    }

    default void onClickBtnGreen() {
        CallUi.getInstance().onClickBtnGreen();
    }

}
