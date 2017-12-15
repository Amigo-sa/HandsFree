package by.citech.handsfree.common;

import android.content.Intent;
import android.content.ServiceConnection;

/**
 * Created by tretyak on 06.12.2017.
 */

public interface IService {
    void unbindService(ServiceConnection conn);
    boolean bindService(Intent service, ServiceConnection conn, int flags);
    Intent getServiceIntent();
}
