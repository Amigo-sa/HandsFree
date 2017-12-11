package by.citech.bluetoothlegatt.commands.service;

import android.content.Intent;
import android.content.ServiceConnection;

import by.citech.IService;
import by.citech.bluetoothlegatt.commands.Command;

/**
 * Created by tretyak on 07.12.2017.
 */

public class StartServiceCommand implements Command {
    private IService iService;
    private Intent intent;

    public StartServiceCommand(IService iService, Intent intent) {
        this.iService = iService;
        this.intent = intent;
    }

    @Override
    public void execute() {

    }

    @Override
    public void undo() {

    }
}
