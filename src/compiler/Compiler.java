/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.CommandDefineFunction;
import compiler.tac.optimize.OptimizationSettings;
import compiler.util.CompilationState;
import compiler.util.MultiThreadedLoader;
import compiler.x86.RegisterAllocator;
import compiler.x86.X86Format;
import compiler.x86.X86Function;
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
    static transient boolean VERBOSE = false;//TODO these four should be in some form of CLI args object passed around, a la OptimizationSettings
    static transient boolean METRICS = false;
    static transient boolean DETERMINISTIC = false;
    static transient boolean OBFUSCATE = false;
    public static boolean deterministic() {
        return DETERMINISTIC;
    }
    public static boolean verbose() {
        return VERBOSE;
    }
    public static boolean metrics() {
        return METRICS;
    }
    public static boolean obfuscate() {
        return OBFUSCATE;
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
        List<X86Function> reachables = X86Function.gen(commands.parallelStream().map(settings::coloncolon).collect(Collectors.toList()));
        long f = System.currentTimeMillis();
        if (VERBOSE) {
            System.out.println("TAC generation took " + (f - e) + "ms overall");
        }
        if (VERBOSE) {
            for (X86Function pair : reachables) {
                System.out.println("TAC FOR " + pair.getName());
                for (int i = 0; i < pair.getStatements().size(); i++) {
                    System.out.println(i + ":     " + pair.getStatements().get(i).toString(false));
                }
                System.out.println();
            }
        }
        long g = System.currentTimeMillis();
        RegisterAllocator.allocate(reachables);
        long h = System.currentTimeMillis();
        String asm = X86Format.assembleFinalFile(reachables);
        long i = System.currentTimeMillis();
        String loll = ("static " + (e - d) + " tacgen " + (f - e) + " debugtac " + (g - f) + " allocation " + (h - g) + " x86gen " + (i - h));
        if (VERBOSE) {
            System.out.println(loll);
            System.err.println(loll);
            System.out.println("Completely done, returning x86 asm string of length " + asm.length());
        }
        return asm;
    }
}
