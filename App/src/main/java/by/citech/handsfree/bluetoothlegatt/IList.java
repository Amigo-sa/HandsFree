package by.citech.handsfree.bluetoothlegatt;

import by.citech.handsfree.dialog.DialogProcessor;

public interface IList {
    void initList();
    void clickItemList(int position, DialogProcessor dialogProcessor);
}
