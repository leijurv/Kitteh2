/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.Command;
import compiler.command.CommandDefineFunction;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.preprocess.StripLocation;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import javax.xml.crypto.NoSuchMechanismException;

/**
 *
 * @author leijurv
 */
public class FunctionsContext {
    private final HashMap<String, FunctionHeader> functionMap = new HashMap<>();
    private final ArrayList<CommandDefineFunction> functionDefinitions;
    public FunctionsContext(ArrayList<Command> definitions) {
        functionDefinitions = new ArrayList<>(definitions.size());
        for (Command com : definitions) {
            CommandDefineFunction cdf = (CommandDefineFunction) com;
            functionDefinitions.add(cdf);
            FunctionHeader header = cdf.getHeader();
            String name = header.name;
            if (functionMap.containsKey(name)) {
                throw new EnumConstantNotPresentException(StripLocation.class, "   error: Two functions with same name: " + name);
            }
            functionMap.put(name, header);
        }
        if (!functionMap.containsKey("main")) {
            throw new NoSuchMechanismException("You need a main function");
        }
    }
    public void parseRekursively() {
        for (CommandDefineFunction cdf : functionDefinitions) {
            cdf.parse(this);
        }
    }
    public FunctionHeader getHeader(String name) {
        if (name.equals("KEYWORD" + Keyword.PRINT.toString())) {
            return CommandDefineFunction.PRINTINT;
        }
        if (name.equals("malloc")) {
            return CommandDefineFunction.MALLOC;
        }
        FunctionHeader tr = functionMap.get(name);
        if (tr == null) {
            throw new ConcurrentModificationException("you tryna call a nonexistent function " + name);
        }
        return tr;
    }
}
