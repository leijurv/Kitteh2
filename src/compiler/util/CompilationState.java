/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import compiler.Compiler;
import compiler.Context;
import compiler.command.CommandDefineFunction;
import compiler.command.FunctionsContext;
import compiler.type.TypeStruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
    private final List<Pair<Path, List<CommandDefineFunction>>> loaded = new ArrayList<>();
    private final Map<Path, Context> ctxts = new HashMap<>();
    private final Map<Path, Map<String, TypeStruct>> importz = new HashMap<>();
    private final List<Path> autoImportedStd;
    private List<FunctionsContext> contexts;
    /**
     * Initialize a compilation state and add all auto-imported standard library
     * files to the list of files to be imported
     *
     * @param main
     * @throws IOException
     */
    public CompilationState(Path main) throws IOException {
        autoImportedStd = Kitterature.listStandardLibraryFiles();
        toLoad.add(main);
        for (Path path : autoImportedStd) {
            toLoad.add(path);
            if (path.toFile().exists()) {
                throw new RuntimeException("Standard library " + path + " is ambiguous: " + path.toFile().getCanonicalPath() + " also exists");
            }
        }
    }
    LinkedList<Path> toLoad() {
        return toLoad;
    }
    synchronized void add(Path path, Context context, boolean isFirst, List<CommandDefineFunction> d) {
        ctxts.put(path, context);
        loaded.add(isFirst ? 0 : loaded.size(), new Pair<>(path, d));
        importz.put(path, context.structsCopy());
    }
    /**
     * Merge of allStructMethods() and functions()
     *
     * @return
     */
    public List<CommandDefineFunction> allFunctions() {
        Stream<CommandDefineFunction> structMethods = importz.values().stream().map(Map::values).flatMap(Collection::stream).flatMap(TypeStruct::getStructMethods).map(Pair::getB);
        Stream<CommandDefineFunction> normalMethods = loaded.stream().map(Pair::getB).flatMap(List::stream);
        return Stream.of(structMethods, normalMethods).flatMap(x -> x).collect(Collectors.toList());
    }
    /**
     * Generate functions context objects for each file. This includes passing
     * to the FunctionsContext constructor: which files were imported and under
     * what aliases, which files were locally imported, all methods for all
     * structs, locally imported and auto imported standard library methods
     */
    public void generateFunctionsContexts() {
        //to be honest, i wrote the below line just for fun to make it as complicated as possible
        contexts = loaded.parallelStream().map(load -> new FunctionsContext(load.getA(), load.getB(), importz.values().stream().map(Map::values).flatMap(Collection::stream).flatMap(TypeStruct::getStructMethods).map(Pair::getB).collect(Collectors.toList()), Stream.of(ctxts.get(load.getA()).imports.entrySet().stream().filter(entry -> entry.getValue() == null).map(Map.Entry::getKey).map(File::new).map(File::toPath), autoImportedStd.stream()).flatMap(x -> x).collect(Collectors.toList()), loaded)).collect(Collectors.toList());
        contexts.get(0).setEntryPoint();
    }
    public void parseAllFunctions() {
        long start = System.currentTimeMillis();
        Stream<Runnable> parseNormalMethods = contexts.stream().flatMap(FunctionsContext::parseRekursivelie);
        Stream<Pair<Context, CommandDefineFunction>> structMethods = importz.values().stream().map(Map::values).flatMap(Collection::stream).flatMap(TypeStruct::getStructMethods);
        List<Context> orderedCtxts = loaded.stream().map(Pair::getA).map(ctxts::get).collect(Collectors.toList());
        Stream<Runnable> parseStructMethods = structMethods.<Runnable>map(cdf -> () -> cdf.getB().parse(contexts.get(orderedCtxts.indexOf(cdf.getA()))));
        Stream.of(parseNormalMethods, parseStructMethods).flatMap(x -> x).parallel().forEach(Runnable::run);
        long end = System.currentTimeMillis();
        System.out.println("Parsing all functions took: " + (end - start) + "ms");
    }
    /**
     * Insert locally imported structs into local contexts. Then parse struct
     * contents (including fields and method headers), but don't allocate field
     * locations or parse method bodies. Next, once all structs know the types
     * of their fields, allocate field locations now that every struct should
     * know what types it has
     */
    public void insertStructs() {
        for (Pair<Path, List<CommandDefineFunction>> pair : loaded) {
            Context context = ctxts.get(pair.getA());
            for (Pair<Path, List<CommandDefineFunction>> oth : loaded) {
                if (oth.getA() != null && autoImportedStd.contains(oth.getA())) {
                    if (oth.getA().equals(pair.getA())) {
                        continue;
                    }
                    if (Compiler.verbose()) {
                        System.out.println("Assuming autoimported stdlib for " + oth.getA());
                    }
                    context.insertStructsUnderPackage(null, importz.get(oth.getA()));
                }
            }
            for (Map.Entry<String, String> imp : context.imports.entrySet()) {
                Path importing = new File(imp.getKey()).toPath();
                String underName = imp.getValue();
                context.insertStructsUnderPackage(underName, importz.get(importing));
            }
        }
        List<TypeStruct> allStructs = importz.values().stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
        allStructs.forEach(TypeStruct::parseContents);
        allStructs.forEach(TypeStruct::allocate);
    }
}
