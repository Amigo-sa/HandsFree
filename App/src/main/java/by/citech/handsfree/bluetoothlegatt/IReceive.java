package by.citech.handsfree.bluetoothlegatt;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by tretyak on 06.12.2017.
 */

public interface IReceive {
    void unregisterReceiver(BroadcastReceiver receiver);
    Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);
}
