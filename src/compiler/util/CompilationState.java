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
    private final List<Pair<Path, List<CommandDefineFunction>>> loaded = new ArrayList<>();
    private final HashMap<Path, Context> ctxts = new HashMap<>();
    private final ArrayList<Context> allContexts = new ArrayList<>();
    private final HashMap<Path, HashMap<String, TypeStruct>> importz = new HashMap<>();
    private final List<Path> autoImportedStd;
    private List<TypeStruct> structs = null;
    private List<FunctionsContext> contexts;
    /**
     * Initialize a compilation state and add all auto-imported standard library
     * files to the list of files to be imported
     *
     * @param main
     * @throws IOException
     */
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
    /**
     * Add a path to be imported sometime in the future, if it hasn't already
     *
     * @param impPath
     */
    public void newImport(Path impPath) {
        if (!alrImp.contains(impPath)) {
            toLoad.push(impPath);
            alrImp.add(impPath);
        }
    }
    /**
     * Called by loader once it's done importing a path
     *
     * @param path the path that it finished importing
     * @param context a new context for this file
     * @param functions the functions defined in this file
     */
    public void doneImporting(Path path, Context context, List<CommandDefineFunction> functions) {
        ctxts.put(path, context);
        loaded.add(new Pair<>(path, functions));
        allContexts.add(context);
        importz.put(path, context.structsCopy());
    }
    /**
     * Get all structs defined in all files. Should only be called after
     * mainImportLoop because it caches the result.
     *
     * @return
     */
    public List<TypeStruct> getStructs() {
        if (structs == null) {
            structs = loaded.stream().map(Pair::getA).map(importz::get).map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
        }
        return structs;
    }
    /**
     * All struct methods, alongside the file contexts in which they were
     * defined
     *
     * @return a stream of pairs, where each pair is a CDF for a struct method
     * and the context for the file in which the struct was defined
     */
    public Stream<Pair<Context, CommandDefineFunction>> structMethod() {
        return getStructs().stream().map(TypeStruct::getStructMethods).flatMap(Collection::stream);
    }
    /**
     * All methods for all structs imported
     *
     * @return
     */
    public Stream<CommandDefineFunction> allStructMethods() {
        return structMethod().map(Pair::getB);
    }
    /**
     * All functions (not struct methods) defined normally in all files imported
     *
     * @return
     */
    public Stream<CommandDefineFunction> functions() {
        return loaded.stream().map(Pair::getB).flatMap(List::stream);
    }
    /**
     * Run the main import loop, which consists of calling Loader.importPath on
     * every item in toLoad until toLoad.isEmpty
     *
     * @throws IOException
     */
    public void mainImportLoop() throws IOException {
        while (!toLoad.isEmpty()) {
            Loader.importPath(this, toLoad.pop());
        }
    }
    /**
     * Merge of allStructMethods() and functions()
     *
     * @return
     */
    public List<CommandDefineFunction> allFunctions() {
        return Stream.of(allStructMethods(), functions()).flatMap(x -> x).collect(Collectors.toList());
    }
    /**
     * Generate functions context objects for each file. This includes passing
     * to the FunctionsContext constructor: which files were imported and under
     * what aliases, which files were locally imported, all methods for all
     * structs, locally imported and auto imported standard library methods
     */
    public void generateFunctionsContexts() {
        contexts = loaded.stream().map(load -> {
            List<Path> locallyImported = ctxts.get(load.getA()).imports.entrySet().stream().filter(entry -> entry.getValue() == null).map(entry -> new File(entry.getKey()).toPath()).collect(Collectors.toList());
            locallyImported.addAll(autoImportedStd);
            return new FunctionsContext(load.getA(), load.getB(), allStructMethods().collect(Collectors.toList()), locallyImported, loaded);
        }).collect(Collectors.toList());
        contexts.get(0).setEntryPoint();
    }
    public void parseAllFunctions() {
        long start = System.currentTimeMillis();
        Stream.<Stream<Runnable>>of(contexts.stream().flatMap(FunctionsContext::parseRekursivelie), structMethod().map(cdf -> () -> cdf.getB().parse(contexts.get(allContexts.indexOf(cdf.getA()))))).flatMap(x -> x).parallel().forEach(Runnable::run);
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
                    if (Compiler.VERBOSE) {
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
        getStructs().stream().forEach(TypeStruct::parseContents);
        getStructs().stream().forEach(TypeStruct::allocate);
    }
}
