package by.citech.handsfree.bluetoothlegatt.commands.service;

import android.content.ServiceConnection;
import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.LeBroadcastReceiver;
import by.citech.handsfree.common.IService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.settings.Settings;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by tretyak on 07.12.2017.
 */

public class BindServiceCommand implements Command {
    private IService iService;
    private ServiceConnection serviceConnection;

    public BindServiceCommand() {
    }

    public void setiService(IService iService) {
        this.iService = iService;
    }

    public void setServiceConnection(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }

    @Override
    public void execute() {

        try {
            if (iService != null) {
                iService.bindService(iService.getServiceIntent(), serviceConnection,  BIND_AUTO_CREATE);
            }
        } catch (IllegalArgumentException e) {
            if (Settings.debug) Log.e("BindServiceCommand", "now Service are binded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void undo() {

    }
}
