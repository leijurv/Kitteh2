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
    private final HashSet<Thread> inProgress;
    private volatile transient Exception thrown;
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
        this.inProgress = new HashSet<>();
    }
    public void mainImportLoop() {
        for (int i = 0; i < toLoad.size(); i++) {//watch me whip, now watch me iterate over a linked list using indicies which is O(n^2)
            //rolls right off the tongue doesn't it
            importFileInNewThread(toLoad.get(i), i == 0);
        }
        while (true) {
            if (Compiler.verbose()) {
                System.out.println("Before wait");
            }
            try {
                synchronized (loaded) {
                    loaded.wait();
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            if (Compiler.verbose()) {
                System.out.println("Loop after wait");
            }
            synchronized (this) {
                if (thrown != null) {
                    if (thrown instanceof RuntimeException) {
                        throw (RuntimeException) thrown;
                        //if it's a runtimeexception, we can just throw it (but only if we cast it first)
                    } else {
                        throw new RuntimeException(thrown);//its a checked exception that we can't just throw
                    }
                    //TODO kill all those other threads
                }
                if (inProgress.isEmpty()) {
                    break;
                }
            }
        }
    }
    public void importFileInNewThread(Path path, boolean f) {
        if (Compiler.verbose()) {
            System.out.println("IFINT " + path);
        }
        Thread th = new Thread() {
            @Override
            public void run() {
                try {
                    importFile(path, f);
                } catch (IOException | RuntimeException ex) {
                    synchronized (CompilationState.this) {
                        if (thrown != null) {
                            System.out.println("At least two exceptions detected in loader threads, only throwing the first. Discarding " + ex);
                            return;
                        }
                        System.out.println("Got exception while loading " + path + ": " + ex);
                        thrown = ex;
                    }
                    synchronized (loaded) {
                        loaded.notifyAll();
                    }
                    synchronized (CompilationState.this) {
                        inProgress.remove(this);
                    }
                }
            }
        };
        synchronized (this) {
            inProgress.add(th);
        }
        th.start();
    }
    public void importFile(Path path, boolean f) throws IOException {
        Pair<Context, List<CommandDefineFunction>> loadResult = Loader.importPath(path);
        Context context = loadResult.getA();
        for (String str : context.imports.keySet()) {
            Path impPath = new File(str).toPath();
            synchronized (this) {
                if (alrImp.contains(impPath)) {
                    continue;
                } else {
                    alrImp.add(impPath);
                }
            }
            importFileInNewThread(impPath, false);
        }
        synchronized (this) {
            ctxts.put(path, context);
            loaded.add(f ? 0 : loaded.size(), new Pair<>(path, loadResult.getB()));
            allContexts.add(f ? 0 : allContexts.size(), context);
            importz.put(path, context.structsCopy());
        }
        if (Compiler.verbose()) {
            System.out.println(path + " done, notifying");
        }
        synchronized (this) {
            inProgress.remove(Thread.currentThread());
        }
        synchronized (loaded) {
            loaded.notifyAll();
        }
    }
    /**
     * Merge of allStructMethods() and functions()
     *
     * @return
     */
    public List<CommandDefineFunction> allFunctions() {
        return Stream.of(importz.values().stream().map(Map::values).flatMap(Collection::stream).flatMap(TypeStruct::getStructMethods).map(Pair::getB), loaded.stream().map(Pair::getB).flatMap(List::stream)).flatMap(x -> x).collect(Collectors.toList());
    }
    /**
     * Generate functions context objects for each file. This includes passing
     * to the FunctionsContext constructor: which files were imported and under
     * what aliases, which files were locally imported, all methods for all
     * structs, locally imported and auto imported standard library methods
     */
    public void generateFunctionsContexts() {
        contexts = loaded.parallelStream().map(load -> new FunctionsContext(load.getA(), load.getB(), importz.values().stream().map(Map::values).flatMap(Collection::stream).flatMap(TypeStruct::getStructMethods).map(Pair::getB).collect(Collectors.toList()), Stream.of(ctxts.get(load.getA()).imports.entrySet().stream().filter(entry -> entry.getValue() == null).map(Map.Entry::getKey).map(File::new).map(File::toPath), autoImportedStd.stream()).flatMap(x -> x).collect(Collectors.toList()), loaded)).collect(Collectors.toList());
        contexts.get(0).setEntryPoint();
    }
    public void parseAllFunctions() {
        long start = System.currentTimeMillis();
        Stream.of(contexts.stream().flatMap(FunctionsContext::parseRekursivelie), importz.values().stream().map(Map::values).flatMap(Collection::stream).flatMap(TypeStruct::getStructMethods).<Runnable>map(cdf -> () -> cdf.getB().parse(contexts.get(allContexts.indexOf(cdf.getA()))))).flatMap(x -> x).parallel().forEach(Runnable::run);
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
        importz.values().stream().map(Map::values).flatMap(Collection::stream).forEach(TypeStruct::parseContents);
        importz.values().stream().map(Map::values).flatMap(Collection::stream).forEach(TypeStruct::allocate);
    }
}
