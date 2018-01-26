package by.citech.handsfree.bluetoothlegatt.commands;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tretyak on 26.01.2018.
 */

public class Controller implements Command{
    private final List<Command> commands = new LinkedList<>();

    public Controller setCommand(Command command){
        commands.add(command);
        return this;
    }

    @Override
    public void execute() {
        for( Command command : commands) {
            command.execute();
        }
        commands.clear();
    }

    @Override
    public void undo(){
        for( Command command : commands) {
            command.undo();
        }
        commands.clear();
    }


}
