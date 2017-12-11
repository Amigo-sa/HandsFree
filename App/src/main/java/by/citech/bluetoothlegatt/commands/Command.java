package by.citech.bluetoothlegatt.commands;

/**
 * Created by tretyak on 06.12.2017.
 */

public interface Command {
    void execute();
    void undo();
}
