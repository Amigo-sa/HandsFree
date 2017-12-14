package by.citech.handsfree.bluetoothlegatt.commands.service;

import android.content.ServiceConnection;

import by.citech.handsfree.IService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 06.12.2017.
 */

public class UnbindServiceCommand implements Command {
    private ServiceConnection serviceConnection;
    private IService iService;

    public UnbindServiceCommand(ServiceConnection serviceConnection, IService iService) {
        this.serviceConnection = serviceConnection;
        this.iService = iService;
    }

    @Override
    public void execute() {
        iService.unbindService(serviceConnection);
    }

    @Override
    public void undo() {

    }

}
