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
import compiler.util.Pair;
import compiler.x86.X86Format;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.xml.crypto.NoSuchMechanismException;

/**
 *
 * @author leijurv
 */
public class Compiler {
    private static Pair<List<CommandDefineFunction>, Context> load(Path name) throws IOException {
        byte[] program = Files.readAllBytes(name);
        List<Line> lines = Preprocessor.preprocess(new String(program));
        Context context = new Context(name + "");
        List<CommandDefineFunction> cmds = Processor.initialParse(lines, context);
        return new Pair<>(cmds, context);
    }
    public static String compile(Path main, OptimizationSettings settings) throws IOException {
        List<Path> toLoad = new ArrayList<>();
        HashSet<Path> alreadyLoaded = new HashSet<>();
        toLoad.add(main);
        List<Pair<Path, List<CommandDefineFunction>>> loaded = new ArrayList<>();
        while (!toLoad.isEmpty()) {
            Path path = toLoad.remove(0);
            alreadyLoaded.add(path);
            System.out.println("Loading " + path);
            Pair<List<CommandDefineFunction>, Context> funcs = load(path);
            Context context = funcs.getValue();
            System.out.println("Imports: " + context.imports);
            for (Entry<String, String> imp : context.imports.entrySet()) {
                String toImportName = imp.getValue() + ".k";
                File toImport = new File(path.toFile().getParent(), toImportName);
                if (!toImport.exists()) {
                    throw new IllegalStateException("Can't import " + toImport + " because " + toImport + " doesn't exist" + imp);
                }
                Path impPath = new File(compiler.parse.Util.trimPath(toImport.toString())).toPath();
                System.out.println("Replacing path " + toImport.toPath() + " with " + impPath);
                if (!toImport.getCanonicalPath().equals(impPath.toFile().getCanonicalPath())) {
                    throw new RuntimeException(toImport.toPath() + " " + impPath + " " + toImport.getCanonicalPath() + " " + impPath.toFile().getCanonicalPath());
                }
                imp.setValue(impPath + "");
                if (!alreadyLoaded.contains(impPath) && !toLoad.contains(impPath)) {
                    toLoad.add(impPath);
                }
            }
            loaded.add(new Pair<>(path, funcs.getKey()));
        }
        System.out.println(loaded);
        List<FunctionsContext> contexts = loaded.stream().map(pair -> new FunctionsContext(pair.getValue(), loaded)).collect(Collectors.toList());
        if (!contexts.get(0).hasMain()) {
            throw new NoSuchMechanismException("You need a main function");
        }
        contexts.get(0).setEntryPoint();
        contexts.parallelStream().forEach(FunctionsContext::parseRekursivelie);
        List<CommandDefineFunction> flattenedList = loaded.stream().map(Pair::getValue).flatMap(List::stream).collect(Collectors.toList());
        return generateASM(flattenedList, settings);
    }
    public static String compile(String program, OptimizationSettings settings) {
        long a = System.currentTimeMillis();
        List<Line> lines = Preprocessor.preprocess(program);
        System.out.println("> DONE PREPROCESSING: " + lines);
        long b = System.currentTimeMillis();
        List<CommandDefineFunction> commands = Processor.initialParse(lines, new Context(null));
        System.out.println("> DONE PROCESSING: " + commands);
        long c = System.currentTimeMillis();
        FunctionsContext fc = new FunctionsContext(commands, Arrays.asList(new Pair<>(null, commands)));
        fc.parseRekursivelie();
        if (!fc.hasMain()) {
            throw new NoSuchMechanismException("You need a main function");
        }
        fc.setEntryPoint();
        System.out.println("> DONE PARSING: " + commands);
        long d = System.currentTimeMillis();
        return generateASM(commands, settings);
    }
    private static String generateASM(List<CommandDefineFunction> commands, OptimizationSettings settings) {
        if (settings.staticValues()) {
            commands.parallelStream().forEach(CommandDefineFunction::staticValues);
        }
        System.out.println("> DONE STATIC VALUES: " + commands);
        long e = System.currentTimeMillis();
        List<Pair<String, List<TACStatement>>> wew = commands.parallelStream()
                .map(com -> new Pair<>(com.getHeader().name, com.totac(settings)))
                .collect(Collectors.toList());
        long f = System.currentTimeMillis();
        Context.printFull = false;
        for (Pair<String, List<TACStatement>> pair : wew) {
            System.out.println("TAC FOR " + pair.getKey());
            for (int i = 0; i < pair.getValue().size(); i++) {
                System.out.println(i + ":     " + pair.getValue().get(i));
            }
            System.out.println();
        }
        long g = System.currentTimeMillis();
        String asm = X86Format.assembleFinalFile(wew);
        long h = System.currentTimeMillis();
        //String loll = ("overall " + (h - a) + " preprocessor " + (b - a) + " processor " + (c - b) + " parse " + (d - c) + " static " + (e - d) + " tacgen " + (f - e) + " debugtac " + (g - f) + " x86gen " + (h - g));
        //System.out.println(loll);
        //System.err.println(loll);
        return asm;
    }
}
