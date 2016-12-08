/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.CommandDefineFunction;
import compiler.command.FunctionsContext;
import compiler.parse.Line;
import compiler.parse.Processor;
import compiler.preprocess.Preprocessor;
import compiler.tac.TACStatement;
import compiler.tac.optimize.OptimizationSettings;
import compiler.util.Kitterature;
import compiler.util.Pair;
import compiler.x86.X86Format;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class Compiler {
    private static Pair<List<CommandDefineFunction>, Context> load(Path name, boolean preImport) throws IOException {
        byte[] program;
        if (preImport) {
            program = Kitterature.getResource(name.toString());
        } else {
            program = Files.readAllBytes(name);
        }
        List<Line> lines = Preprocessor.preprocess(new String(program));
        Context context = new Context(name + "");
        List<CommandDefineFunction> cmds = Processor.initialParse(lines, context);
        return new Pair<>(cmds, context);
    }
    public static String compile(Path main, OptimizationSettings settings) throws IOException {
        long a = System.currentTimeMillis();
        int entrypoint = 0;
        LinkedList<Path> toLoad = new LinkedList<>();
        HashSet<Path> alrImp = new HashSet<>();
        List<Pair<Path, List<CommandDefineFunction>>> loaded = new ArrayList<>();
        HashMap<Path, Context> ctxts = new HashMap<>();
        boolean preImport = true;
        Path importpath = Paths.get("bigint.k");
        entrypoint++;
        toLoad.add(importpath);
        alrImp.add(importpath);
        while (true) {
            if (toLoad.isEmpty()) {
                if (!preImport) {
                    break;
                } else {
                    toLoad.add(main);
                    alrImp.add(main);
                    preImport = false;
                    continue;
                }
            }
            Path path = toLoad.pop();
            System.out.println("Loading " + path);
            Pair<List<CommandDefineFunction>, Context> funcs = load(path, preImport);
            Context context = funcs.getB();
            System.out.println("Imports: " + context.imports);
            HashMap<String, String> fix = new HashMap<>();
            HashSet<String> rmv = new HashSet<>();
            for (Entry<String, String> imp : context.imports.entrySet()) {
                String toImportName = imp.getKey() + ".k";
                File toImport = new File(path.toFile().getParent(), toImportName);
                if (!toImport.exists() && !preImport) {
                    throw new IllegalStateException("Can't import " + toImport + " because " + toImport + " doesn't exist" + imp);
                }
                Path impPath = new File(Kitterature.trimPath(toImport.toString())).toPath();
                System.out.println("Replacing path " + toImport.toPath() + " with " + impPath);
                if (!toImport.getCanonicalPath().equals(impPath.toFile().getCanonicalPath())) {
                    throw new RuntimeException(toImport.toPath() + " " + impPath + " " + toImport.getCanonicalPath() + " " + impPath.toFile().getCanonicalPath());
                }
                rmv.add(imp.getKey());
                fix.put(impPath + "", imp.getValue());
                if (!alrImp.contains(impPath)) {
                    toLoad.push(impPath);
                    alrImp.add(impPath);
                }
            }
            System.out.println("FIXING " + fix + " " + rmv);
            for (String s : rmv) {
                context.imports.remove(s);
            }
            for (Entry<String, String> entry : fix.entrySet()) {
                context.imports.put(entry.getKey(), entry.getValue());
            }
            ctxts.put(path, context);
            loaded.add(new Pair<>(path, funcs.getA()));
        }
        long b = System.currentTimeMillis();
        for (Pair<Path, List<CommandDefineFunction>> pair : loaded) {
            Context context = ctxts.get(pair.getA());
            for (Pair<Path, List<CommandDefineFunction>> oth : loaded) {
                if (oth.getA() != null && !oth.getA().toFile().exists()) {
                    System.out.println("Assuming stdlib for " + oth.getA());
                    context.insertStructsUnderPackage(null, ctxts.get(oth.getA()));
                }
            }
            for (Entry<String, String> imp : context.imports.entrySet()) {
                Path importing = new File(imp.getKey()).toPath();
                String underName = imp.getValue();
                Context importedContext = ctxts.get(importing);
                if (importedContext == null) {
                    throw new IllegalStateException(ctxts + " " + importing + " " + imp);
                }
                context.insertStructsUnderPackage(underName, importedContext);
            }
        }
        long c = System.currentTimeMillis();
        for (Pair<Path, List<CommandDefineFunction>> pair : loaded) {
            for (CommandDefineFunction cdf : pair.getB()) {
                cdf.parseHeader();
            }
        }
        List<FunctionsContext> contexts = loaded.stream().map(load -> new FunctionsContext(load.getA(), load.getB(), ctxts.get(load.getA()).imports.entrySet().stream().filter(entry -> entry.getValue() == null).map(entry -> new File(entry.getKey()).toPath()).collect(Collectors.toList()), loaded)).collect(Collectors.toList());
        contexts.get(entrypoint).setEntryPoint();//the actual main-main function we'll start with is in the first file loaded plus the number of stdlib files we imported
        long d = System.currentTimeMillis();
        System.out.println("load: " + (b - a) + "ms, structs: " + (c - b) + "ms, funcContext: " + (d - c) + "ms");
        System.out.println();
        System.out.println("---- END IMPORTS, BEGIN PARSING ----");
        System.out.println();
        contexts.parallelStream().forEach(FunctionsContext::parseRekursivelie);
        List<CommandDefineFunction> flattenedList = loaded.stream().map(Pair::getB).flatMap(List::stream).collect(Collectors.toList());
        return generateASM(flattenedList, settings);
    }
    public static String compile(String program, OptimizationSettings settings) {
        long a = System.currentTimeMillis();
        List<Line> lines = Preprocessor.preprocess(program);
        System.out.println("> DONE PREPROCESSING");
        long b = System.currentTimeMillis();
        List<CommandDefineFunction> commands = Processor.initialParse(lines, new Context(null));
        System.out.println("> DONE PROCESSING");
        for (CommandDefineFunction cdf : commands) {
            cdf.parseHeader();
        }
        long c = System.currentTimeMillis();
        FunctionsContext fc = new FunctionsContext(null, commands, Arrays.asList(), Arrays.asList(new Pair<>(null, commands)));
        fc.parseRekursivelie();
        fc.setEntryPoint();
        System.out.println("> DONE PARSING");
        return generateASM(commands, settings);
    }
    private static String generateASM(List<CommandDefineFunction> commands, OptimizationSettings settings) {
        long d = System.currentTimeMillis();
        if (settings.staticValues()) {
            commands.parallelStream().forEach(CommandDefineFunction::staticValues);
        }
        System.out.println("> DONE STATIC VALUES");
        long e = System.currentTimeMillis();
        List<Pair<String, List<TACStatement>>> wew = commands.parallelStream()
                .map(com -> new Pair<>(com.getHeader().name, com.totac(settings)))
                .collect(Collectors.toList());
        long f = System.currentTimeMillis();
        Context.printFull = false;
        for (Pair<String, List<TACStatement>> pair : wew) {
            System.out.println("TAC FOR " + pair.getA());
            for (int i = 0; i < pair.getB().size(); i++) {
                System.out.println(i + ":     " + pair.getB().get(i));
            }
            System.out.println();
        }
        long g = System.currentTimeMillis();
        String asm = X86Format.assembleFinalFile(wew);
        long h = System.currentTimeMillis();
        String loll = ("static " + (e - d) + " tacgen " + (f - e) + " debugtac " + (g - f) + " x86gen " + (h - g));
        System.out.println(loll);
        System.err.println(loll);
        return asm;
    }
}
