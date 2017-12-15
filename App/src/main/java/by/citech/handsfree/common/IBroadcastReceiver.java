package by.citech.handsfree.common;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by tretyak on 06.12.2017.
 */

public interface IBroadcastReceiver {
    void unregisterReceiver(BroadcastReceiver receiver);
    Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);
}
