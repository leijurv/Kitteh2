/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.CommandDefineFunction;
import compiler.command.FunctionsContext;
import compiler.type.TypeStruct;
import compiler.util.Kitterature;
import compiler.util.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public class CompilationState {
    private final LinkedList<Path> toLoad = new LinkedList<>();
    private final HashSet<Path> alrImp = new HashSet<>();
    final List<Pair<Path, List<CommandDefineFunction>>> loaded = new ArrayList<>();
    final HashMap<Path, Context> ctxts = new HashMap<>();
    private final ArrayList<Context> allContexts = new ArrayList<>();
    final HashMap<Path, HashMap<String, TypeStruct>> importz = new HashMap<>();
    final List<Path> autoImportedStd;
    private List<TypeStruct> structs = null;
    private List<FunctionsContext> contexts;
    public CompilationState(Path main) throws IOException {
        autoImportedStd = Kitterature.listFiles();
        toLoad.add(main);
        alrImp.add(main);
        for (Path path : autoImportedStd) {
            toLoad.add(path);
            alrImp.add(path);
            if (path.toFile().exists()) {
                throw new RuntimeException("Standard library " + path + " is ambiguous: " + path.toFile().getCanonicalPath() + " also exists");
            }
        }
    }
    public void newImport(Path impPath) {
        if (!alrImp.contains(impPath)) {
            toLoad.push(impPath);
            alrImp.add(impPath);
        }
    }
    public void doneImporting(Path path, Context context, List<CommandDefineFunction> functions) {
        ctxts.put(path, context);
        loaded.add(new Pair<>(path, functions));
        allContexts.add(context);
        importz.put(path, context.structsCopy());
    }
    public List<TypeStruct> getStructs() {
        if (structs == null) {
            structs = loaded.stream().map(Pair::getA).map(importz::get).map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
        }
        return structs;
    }
    public Stream<Pair<Context, CommandDefineFunction>> structMethod() {
        return getStructs().stream().map(TypeStruct::getStructMethods).flatMap(Collection::stream);
    }
    public Stream<CommandDefineFunction> allStructMethods() {
        return structMethod().map(Pair::getB);
    }
    public Stream<CommandDefineFunction> functions() {
        return loaded.stream().map(Pair::getB).flatMap(List::stream);
    }
    public boolean has() {
        return !toLoad.isEmpty();
    }
    public Path pop() {
        return toLoad.pop();
    }
    public List<CommandDefineFunction> allFunctions() {
        return Stream.of(allStructMethods(), functions()).flatMap(x -> x).collect(Collectors.toList());
    }
    public void parseStructMethods() {
        for (Pair<Context, CommandDefineFunction> cdf : structMethod().collect(Collectors.toList())) {
            cdf.getB().parse(contexts.get(allContexts.indexOf(cdf.getA())));
        }
    }
    public void generateFunctionsContexts() {
        contexts = loaded.stream().map(load -> {
            List<Path> locallyImported = ctxts.get(load.getA()).imports.entrySet().stream().filter(entry -> entry.getValue() == null).map(entry -> new File(entry.getKey()).toPath()).collect(Collectors.toList());
            locallyImported.addAll(autoImportedStd);
            return new FunctionsContext(load.getA(), load.getB(), allStructMethods().collect(Collectors.toList()), locallyImported, loaded);
        }).collect(Collectors.toList());
        contexts.get(0).setEntryPoint();
    }
    public void parseAllFunctionsContexts() {
        contexts.parallelStream().forEach(FunctionsContext::parseRekursivelie);
    }
}
