package by.citech.handsfree.call;

public interface ICallUi {

    default void onClickBtnRed() {
        CallUi.getInstance().onClickBtnRed();
    }

    default void onClickBtnGreen() {
        CallUi.getInstance().onClickBtnGreen();
    }

}
