/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Context;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public abstract class CommandBlock extends Command {
    protected final ArrayList<Command> contents;
    public CommandBlock(Context context, ArrayList<Command> contents) {
        super(context);
        this.contents = contents;
    }
    @Override
    public Stream<String> getAllVarsModified() {
        return contents.stream().flatMap(Command::getAllVarsModified);
    }
}
