package by.citech.handsfree.bluetoothlegatt.commands.service;

import android.content.ServiceConnection;
import android.util.Log;

import by.citech.handsfree.common.IService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by tretyak on 07.12.2017.
 */

public class BindServiceCommand implements Command {
    private IService iService;
    private ServiceConnection serviceConnection;

    public BindServiceCommand(IService iService, ServiceConnection serviceConnection) {
        this.iService = iService;
        this.serviceConnection = serviceConnection;
    }

    @Override
    public void execute() {
        Log.i("command", "iService = " + iService);
        Log.i("command", "serviceConnection = " + serviceConnection);
        Log.i("command", "BIND_AUTO_CREATE = " + BIND_AUTO_CREATE);
        Log.i("command", "getServiceIntent = " + iService.getServiceIntent());
        iService.bindService(iService.getServiceIntent(), serviceConnection,  BIND_AUTO_CREATE);
    }

    @Override
    public void undo() {

    }
}
