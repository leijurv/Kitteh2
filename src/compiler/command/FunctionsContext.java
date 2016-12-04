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
import java.nio.file.Path;
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
    public FunctionsContext(List<CommandDefineFunction> definitions, List<Pair<Path, List<CommandDefineFunction>>> otherFiles) {
        functionDefinitions = new ArrayList<>(definitions.size());
        for (CommandDefineFunction cdf : definitions) {
            functionDefinitions.add(cdf);
            FunctionHeader header = cdf.getLocalHeader();
            String name = header.name;
            if (functionMap.containsKey(name)) {
                throw new EnumConstantNotPresentException(Operator.class, "   error: Two functions with same name: " + name);
            }
            functionMap.put(name, cdf.getHeader());//put the pkg::funcName header under funcName in the map
        }
        for (Pair<Path, List<CommandDefineFunction>> file : otherFiles) {
            for (CommandDefineFunction cdf : file.getB()) {
                FunctionHeader header = cdf.getHeader();
                String name = header.name;
                if (functionMap.containsKey(name)) {
                    throw new EnumConstantNotPresentException(Operator.class, "   error: Two functions with same name: " + name + " " + functionMap);
                }
                functionMap.put(name, header);
            }
        }
        System.out.println(functionMap);
    }
    public void setEntryPoint() {
        for (CommandDefineFunction cdf : functionDefinitions) {
            if (cdf.getLocalHeader().name.equals("main")) {
                System.out.println("Setting entry point");
                cdf.setEntryPoint();
                return;
            }
        }
        throw new RuntimeException();
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
        if (name.equals("calloc")) {
            return CommandDefineFunction.CALLOC;
        }
        if (name.equals("free")) {
            return CommandDefineFunction.FREE;
        }
        String actual;
        if (pkg == null) {
            actual = name;
        } else {
            actual = pkg.replace(".", "DOT").replace("/", "_") + Math.abs(pkg.hashCode()) + "__" + name;
        }
        FunctionHeader tr = functionMap.get(actual);
        if (tr == null) {
            throw new ConcurrentModificationException("you tryna call a nonexistent function " + actual + " " + functionMap);
        }
        return tr;
    }
}
