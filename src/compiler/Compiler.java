/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.Command;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.crypto.NoSuchMechanismException;
import org.w3c.dom.ls.LSException;

/**
 *
 * @author leijurv
 */
public class Compiler {
    public static long streamTime() {
        long a = System.currentTimeMillis();
        IntStream.range(0, 5).map(x -> x + 1).parallel().sum();
        long b = System.currentTimeMillis();
        return b - a;
    }
    protected static String DEFAULT_IN_FILE = System.getProperty("user.home") + "/Documents/test.k";
    protected static String DEFAULT_OUT_FILE = System.getProperty("user.home") + "/Documents/blar.s";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        http://github.com/leijurv/Kitteh2
        System.out.println("First stream: " + streamTime());//almost always several hundred ms
        System.out.println("Second stream: " + streamTime());//almost always zero
        String inFile = DEFAULT_IN_FILE;
        String outFile = DEFAULT_OUT_FILE;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-i":
                    if (i + 1 == args.length) {
                        throw new IllformedLocaleException("You gotta give a file");
                    }
                    inFile = args[++i];
                    break;
                case "-o":
                    if (i + 1 == args.length) {
                        throw new LSException((short) "urmum".hashCode(), "You gotta give a file");
                    }
                    outFile = args[++i];
                case "-I":
                    inFile = "/dev/stdin";
                    break;
                case "-O":
                    outFile = "/dev/stdout";
                    break;
            }
        }
        //byte[] program = Files.readAllBytes(new File(inFile).toPath());
        //String asm = compile(new String(program), new OptimizationSettings(OPTIMIZE, OPTIMIZE));
        File dir = new File(inFile).getParentFile();
        String cont = new File(inFile).getName().split(".k")[0];
        String asm = Compiler.compile(dir, cont, new OptimizationSettings(OPTIMIZE, OPTIMIZE));
        new FileOutputStream(outFile).write(asm.getBytes());
    }
    public static final boolean OPTIMIZE = true;//if it's being bad, see if changing this to false fixes it
    public static Pair<List<Command>, Context> load(File dir, String name, OptimizationSettings settings) throws IOException {
        byte[] program = Files.readAllBytes(new File(dir, name + ".k").toPath());
        List<Line> lines = Preprocessor.preprocess(new String(program));
        Context context = new Context(name);
        ArrayList<Command> cmds = Processor.parse(new ArrayList<>(lines), context);
        return new Pair<>(cmds, context);
    }
    public static String compile(File dir, String mainName, OptimizationSettings settings) throws IOException {
        List<String> toLoad = new ArrayList<>();
        HashSet<String> alreadyLoaded = new HashSet<>();
        toLoad.add(mainName);
        List<Pair<String, List<Command>>> loaded = new ArrayList<>();
        while (!toLoad.isEmpty()) {
            String path = toLoad.remove(0);
            alreadyLoaded.add(path);
            System.out.println("Loading " + new File(dir, path + ".k"));
            Pair<List<Command>, Context> funcs = load(dir, path, settings);
            Context context = funcs.getValue();
            System.out.println("Imports: " + context.imports);
            for (Entry<String, String> imp : context.imports.entrySet()) {
                String toImport = imp.getValue();
                if (!new File(dir, toImport + ".k").exists()) {
                    throw new IllegalStateException("Can't import " + toImport + " because " + new File(dir, toImport + ".k") + " doesn't exist");
                }
                if (!alreadyLoaded.contains(toImport) && !toLoad.contains(toImport)) {
                    toLoad.add(toImport);
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
        List<CommandDefineFunction> flattenedList = loaded.stream().map(Pair::getValue).flatMap(List::stream).map(CommandDefineFunction.class::cast).collect(Collectors.toList());
        return generateASM(flattenedList, settings);
    }
    public static String compile(String program, OptimizationSettings settings) {
        long a = System.currentTimeMillis();
        List<Line> lines = Preprocessor.preprocess(program);
        System.out.println("> DONE PREPROCESSING: " + lines);
        long b = System.currentTimeMillis();
        ArrayList<Command> commands = Processor.parse(new ArrayList<>(lines), new Context(""));
        System.out.println("> DONE PROCESSING: " + commands);
        long c = System.currentTimeMillis();
        FunctionsContext fc = new FunctionsContext(commands, Arrays.asList(new Pair<>("", commands)));
        fc.parseRekursivelie();
        if (!fc.hasMain()) {
            throw new NoSuchMechanismException("You need a main function");
        }
        fc.setEntryPoint();
        System.out.println("> DONE PARSING: " + commands);
        long d = System.currentTimeMillis();
        List<CommandDefineFunction> cdfs = commands.stream().map(CommandDefineFunction.class::cast).collect(Collectors.toList());
        return generateASM(cdfs, settings);
    }
    public static String generateASM(List<CommandDefineFunction> commands, OptimizationSettings settings) {
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
