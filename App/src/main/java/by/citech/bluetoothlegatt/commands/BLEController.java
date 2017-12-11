package by.citech.bluetoothlegatt.commands;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tretyak on 06.12.2017.
 */

public class BLEController {
    private final List<Command> commands = new LinkedList<>();

    public BLEController setCommand(Command command){
        commands.add(command);
        return this;
    }

    public void execute() {
        for( Command command : commands) {
            command.execute();
        }
        commands.clear();
    }

    public void undo(){
        for( Command command : commands) {
            command.undo();
        }
        commands.clear();
    }

}
