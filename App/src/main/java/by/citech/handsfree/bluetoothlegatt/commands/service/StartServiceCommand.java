package by.citech.handsfree.bluetoothlegatt.commands.service;

import android.content.Intent;

import by.citech.handsfree.common.IService;
import by.citech.handsfree.bluetoothlegatt.commands.Command;

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
