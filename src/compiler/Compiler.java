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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        http://github.com/leijurv/Kitteh2
        System.out.println("First stream: " + streamTime());//almost always several hundred ms
        System.out.println("Second stream: " + streamTime());//almost always zero
        byte[] program = Files.readAllBytes(new File("/Users/leijurv/Documents/test.k").toPath());
        String asm = compile(new String(program), new OptimizationSettings(OPTIMIZE, OPTIMIZE));
        new FileOutputStream("/Users/leijurv/Documents/blar.s").write(asm.getBytes());
    }
    public static final boolean OPTIMIZE = true;//if it's being bad, see if changing this to false fixes it
    public static String compile(String program, OptimizationSettings settings) {
        long a = System.currentTimeMillis();
        List<Line> lines = Preprocessor.preprocess(program);
        System.out.println("> DONE PREPROCESSING: " + lines);
        long b = System.currentTimeMillis();
        ArrayList<Command> commands = Processor.parse(lines);
        System.out.println("> DONE PROCESSING: " + commands);
        long c = System.currentTimeMillis();
        FunctionsContext.parseRekursively(commands);
        System.out.println("> DONE PARSING: " + commands);
        long d = System.currentTimeMillis();
        if (settings.staticValues()) {
            for (Command com : commands) {
                com.staticValues();
            }
        }
        System.out.println("> DONE STATIC VALUES: " + commands);
        long e = System.currentTimeMillis();
        List<Pair<String, List<TACStatement>>> wew = commands.parallelStream().map(CommandDefineFunction.class::cast)
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
        String loll = ("overall " + (h - a) + " preprocessor " + (b - a) + " processor " + (c - b) + " parse " + (d - c) + " static " + (e - d) + " tacgen " + (f - e) + " debugtac " + (g - f) + " x86gen " + (h - g));
        System.out.println(loll);
        System.err.println(loll);
        return asm;
    }
}
