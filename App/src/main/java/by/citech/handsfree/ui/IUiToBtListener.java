package by.citech.handsfree.ui;

/**
 * Created by tretyak on 15.12.2017.
 */

public interface IUiToBtListener {
    void scanItemSelectedListener();
    void stopItemSelectedListener();
    void clickItemListListener(int position);
    void clickBtnChangeDeviceListenerOne();
    void clickBtnChangeDeviceListenerTwo();
    void swipeScanStartListener();
    void swipeScanStopListener();
}
