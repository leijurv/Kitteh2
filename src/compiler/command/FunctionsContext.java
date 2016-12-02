/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
import compiler.Keyword;
import compiler.Operator;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.util.Pair;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public class FunctionsContext {
    public static final boolean PARALLEL_FUNCTION_PARSING = true;
    private final HashMap<String, FunctionHeader> functionMap = new HashMap<>();
    private final ArrayList<CommandDefineFunction> functionDefinitions;
    public FunctionsContext(List<Command> definitions, List<Pair<String, List<Command>>> otherFiles) {
        functionDefinitions = new ArrayList<>(definitions.size());
        for (Command com : definitions) {
            CommandDefineFunction cdf = (CommandDefineFunction) com;
            functionDefinitions.add(cdf);
            FunctionHeader header = cdf.getLocalHeader();
            String name = header.name;
            if (functionMap.containsKey(name)) {
                throw new EnumConstantNotPresentException(Operator.class, "   error: Two functions with same name: " + name);
            }
            functionMap.put(name, cdf.getHeader());
        }
        for (Pair<String, List<Command>> file : otherFiles) {
            String packageName = file.getKey();
            for (Command com : file.getValue()) {
                CommandDefineFunction cdf = (CommandDefineFunction) com;
                FunctionHeader header = cdf.getHeader();
                String name = header.name;
                if (functionMap.containsKey(name)) {
                    throw new EnumConstantNotPresentException(Operator.class, "   error: Two functions with same name: " + name);
                }
                functionMap.put(name, header);
            }
        }
        System.out.println(functionMap);
    }
    public void setEntryPoint() {
        for (CommandDefineFunction cdf : functionDefinitions) {
            if (cdf.getLocalHeader().name.equals("main")) {
                cdf.setEntryPoint();
            }
        }
    }
    public boolean hasMain() {
        return functionMap.containsKey("main");
    }
    public void parseRekursivelie() {
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
    public FunctionHeader getHeader(String pkg, String name) {
        if (name.equals(Keyword.PRINT.toString())) {
            return CommandDefineFunction.PRINTINT;
        }
        if (name.equals("malloc")) {
            return CommandDefineFunction.MALLOC;
        }
        String actual;
        if (pkg == null) {
            actual = name;
        } else {
            actual = pkg + "__" + name;
        }
        FunctionHeader tr = functionMap.get(actual);
        if (tr == null) {
            throw new ConcurrentModificationException("you tryna call a nonexistent function " + actual);
        }
        return tr;
    }
}
