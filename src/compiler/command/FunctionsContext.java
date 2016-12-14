/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.command;
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
    //public static final boolean PARALLEL_FUNCTION_PARSING = true;
    private final HashMap<String, FunctionHeader> functionMap = new HashMap<>();
    private final ArrayList<CommandDefineFunction> functionDefinitions;
    private final Path path;
    public FunctionsContext(Path thisPath, List<CommandDefineFunction> definitions, List<CommandDefineFunction> structMethods, List<Path> defineLocally, List<Pair<Path, List<CommandDefineFunction>>> otherFiles) {
        functionDefinitions = new ArrayList<>(definitions.size());
        this.path = thisPath;
        //System.out.println("Local imports for " + thisPath + ": " + defineLocally);
        for (CommandDefineFunction cdf : definitions) {
            functionDefinitions.add(cdf);
            FunctionHeader header = cdf.getLocalHeader();
            String name = header.name;
            if (functionMap.containsKey(name)) {
                throw new EnumConstantNotPresentException(Operator.class, "   error: Two functions with same name: " + name);
            }
            functionMap.put(name, cdf.getHeader());//put the pkg::funcName header under funcName in the map
        }
        for (CommandDefineFunction cdf : structMethods) {
            FunctionHeader header = cdf.getLocalHeader();
            String name = header.name;
            if (functionMap.containsKey(name)) {
                throw new EnumConstantNotPresentException(Operator.class, "   error: Two struct methods with same name: " + name);
            }
            functionMap.put(name, cdf.getHeader());//put the pkg::funcName header under funcName in the map
        }
        for (Pair<Path, List<CommandDefineFunction>> file : otherFiles) {
            for (CommandDefineFunction cdf : file.getB()) {
                FunctionHeader header = cdf.getHeader();
                String name = header.name;
                if (functionMap.containsKey(name)) {
                    throw new EnumConstantNotPresentException(Operator.class, "   error: Two functions with same name from aliased import " + name);
                }
                functionMap.put(name, header);
                if (defineLocally.contains(file.getA()) && !thisPath.equals(file.getA())) {
                    String name1 = cdf.getLocalHeader().name;
                    if (functionMap.containsKey(name1)) {
                        throw new EnumConstantNotPresentException(Operator.class, "   error: Two functions with same name from local import " + name + " " + defineLocally + " " + thisPath + " " + file.getA());
                    }
                    functionMap.put(name1, header);
                }
            }
        }
    }
    public void setEntryPoint() {
        for (CommandDefineFunction cdf : functionDefinitions) {
            if (cdf.getLocalHeader().name.equals("main")) {
                //System.out.println("Setting entry point in file " + path);
                cdf.setEntryPoint();
                return;
            }
        }
        throw new RuntimeException("You need a main function in " + path);
    }
    public Stream<Runnable> parseRekursivelie() {
        return functionDefinitions.stream().map(cdf -> () -> cdf.parse(this));
    }
    //public void parseRekursivelie() {
    //long start1 = System.currentTimeMillis();
    //System.out.println("> Starting parsing functions in " + path);
    //stream.forEach((CommandDefineFunction cdf) -> {
    //long start = System.currentTimeMillis();
    //System.out.println("> Starting parsing function " + cdf.getHeader().name);
    //cdf.parse(this);
    //System.out.println("> Finished parsing function " + cdf.getHeader().name + " -- " + (System.currentTimeMillis() - start) + "ms");
    //});
    //System.out.println("> Finished parsing functions in " + path + " -- " + (System.currentTimeMillis() - start1) + "ms");
    //}
    public FunctionHeader getHeader(String pkg, String name) {
        try {
            return (FunctionHeader) Stream.of(CommandDefineFunction.class).parallel().map(Class::getFields).flatMap(Stream::of).parallel().filter(x -> x.getName().equals(name.toUpperCase())).findAny().get().get(null);
        } catch (IllegalAccessException | RuntimeException ex) {
        }
        String actual;
        if (pkg == null) {
            actual = name;
        } else {
            actual = CommandDefineFunction.headerNameFromPkgAndName(pkg, name);
        }
        FunctionHeader tr = functionMap.get(actual);
        if (tr == null) {
            throw new ConcurrentModificationException(path + ": you tryna call a nonexistent function " + actual + " " + functionMap.keySet());
        }
        return tr;
    }
}
