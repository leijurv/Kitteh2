/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Keyword;
import compiler.Operator;
import compiler.command.CommandDefineFunction.FunctionHeader;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.stream.Stream;
import javax.xml.crypto.NoSuchMechanismException;

/**
 *
 * @author leijurv
 */
public class FunctionsContext {
    public static final boolean PARALLEL_FUNCTION_PARSING = true;
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
                throw new EnumConstantNotPresentException(Operator.class, "   error: Two functions with same name: " + name);
            }
            functionMap.put(name, header);
        }
        if (!functionMap.containsKey("main")) {
            throw new NoSuchMechanismException("You need a main function");
        }
    }
    public void parseRekursively() {
        Stream<CommandDefineFunction> stream = functionDefinitions.stream();
        if (PARALLEL_FUNCTION_PARSING) {
            stream = stream.parallel();
        }
        stream.forEach(cdf -> {
            long start = System.currentTimeMillis();
            System.out.println("> Starting parsing function " + cdf.getHeader().name);
            cdf.parse(this);
            System.out.println("> Finished parsing function " + cdf.getHeader().name + " -- " + (System.currentTimeMillis() - start) + "ms");
        });
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
