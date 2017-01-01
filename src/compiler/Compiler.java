/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.CommandDefineFunction;
import compiler.tac.TACStatement;
import compiler.tac.optimize.OptimizationSettings;
import compiler.util.CompilationState;
import compiler.util.MultiThreadedLoader;
import compiler.util.Pair;
import compiler.util.Prune;
import compiler.x86.X86Format;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class Compiler {
    static transient boolean VERBOSE = false;//TODO these three should be in some form of CLI args object passed around, a la OptimizationSettings
    static transient boolean METRICS = false;
    static transient boolean DETERMINISTIC = false;
    public static boolean deterministic() {
        return DETERMINISTIC;
    }
    public static boolean verbose() {
        return VERBOSE;
    }
    public static boolean metrics() {
        return METRICS;
    }
    public static String compile(Path main, OptimizationSettings settings) throws IOException {
        long a = System.currentTimeMillis();
        CompilationState cs = new CompilationState(main);
        new MultiThreadedLoader(cs).mainImportLoop();
        long b = System.currentTimeMillis();
        cs.insertStructs();
        long c = System.currentTimeMillis();
        List<CommandDefineFunction> allFunctions = cs.allFunctions();
        allFunctions.parallelStream().forEach(CommandDefineFunction::parseHeader);
        long d = System.currentTimeMillis();
        cs.generateFunctionsContexts();
        long e = System.currentTimeMillis();
        if (VERBOSE) {
            System.out.println("load: " + (b - a) + "ms, structs: " + (c - b) + "ms, parseheaders: " + (d - c) + "ms, funcContext: " + (e - d) + "ms");
            System.out.println();
            System.out.println("---- END IMPORTS, BEGIN PARSING ----");
            System.out.println();
        }
        cs.parseAllFunctions();
        return generateASM(allFunctions, settings);
    }
    public static String compile(String program, OptimizationSettings settings) {
        try {
            File f = File.createTempFile("temp", ".k");
            f.deleteOnExit();
            try (FileOutputStream lol = new FileOutputStream(f)) {
                lol.write(program.getBytes("UTF-8"));
            }
            return compile(f.toPath(), settings);
        } catch (IOException ex) {
            Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    private static String generateASM(List<CommandDefineFunction> commands, OptimizationSettings settings) {
        long d = System.currentTimeMillis();
        if (settings.staticValues()) {
            commands.parallelStream().forEach(CommandDefineFunction::optimize);
        }
        if (VERBOSE) {
            System.out.println("> DONE STATIC VALUES");
        }
        long e = System.currentTimeMillis();
        List<Pair<String, List<TACStatement>>> wew = commands.parallelStream().map(settings::coloncolon).collect(Collectors.toList());
        long f = System.currentTimeMillis();
        if (VERBOSE) {
            System.out.println("TAC generation took " + (f - e) + "ms overall");
        }
        if (VERBOSE) {
            for (Pair<String, List<TACStatement>> pair : wew) {
                System.out.println("TAC FOR " + pair.getA());
                for (int i = 0; i < pair.getB().size(); i++) {
                    System.out.println(i + ":     " + pair.getB().get(i).toString(false));
                }
                System.out.println();
            }
        }
        wew = Prune.prune(wew);
        long g = System.currentTimeMillis();
        String asm = X86Format.assembleFinalFile(wew);
        long h = System.currentTimeMillis();
        String loll = ("static " + (e - d) + " tacgen " + (f - e) + " debugtac " + (g - f) + " x86gen " + (h - g));
        if (VERBOSE) {
            System.out.println(loll);
            System.err.println(loll);
            System.out.println("Completely done, returning x86 asm string of length " + asm.length());
        }
        return asm;
    }
}
