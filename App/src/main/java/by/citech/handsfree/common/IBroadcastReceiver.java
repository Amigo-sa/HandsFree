package by.citech.handsfree.common;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

public interface IBroadcastReceiver {
    void unregisterReceiver(BroadcastReceiver receiver);
    Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);
}
