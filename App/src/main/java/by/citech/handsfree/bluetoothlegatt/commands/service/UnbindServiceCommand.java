package by.citech.handsfree.bluetoothlegatt.commands.service;

import android.content.ServiceConnection;
import android.util.Log;

import by.citech.handsfree.common.IService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;
import by.citech.handsfree.settings.Settings;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by tretyak on 06.12.2017.
 */

public class UnbindServiceCommand implements Command {
    private ServiceConnection serviceConnection;
    private IService iService;

    public UnbindServiceCommand() {

    }

    public void setServiceConnection(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }

    public void setiService(IService iService) {
        this.iService = iService;
    }

    @Override
    public void execute() {

        try {
            if (iService!=null) {
                iService.unbindService(serviceConnection);
            }
        } catch (IllegalArgumentException e) {
            if (Settings.debug) Log.e("Command", "now Service are unbinded");
        }

    }

    @Override
    public void undo() {

    }

}
