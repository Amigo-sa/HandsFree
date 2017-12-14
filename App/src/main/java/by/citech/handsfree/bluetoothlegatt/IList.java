package by.citech.handsfree.bluetoothlegatt;

import by.citech.handsfree.dialog.DialogProcessor;

/**
 * Created by tretyak on 11.12.2017.
 */

public interface IList {
    void initList();
    void clickItemList(int position, DialogProcessor dialogProcessor);
}
