/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.command.Command;
import compiler.command.CommandDefineFunction;
import compiler.parse.Processor;
import compiler.preprocess.Preprocessor;
import compiler.tac.TACStatement;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.util.Pair;

/**
 *
 * @author leijurv
 */
public class Compiler {
    public static long streamTime() {
        long a = System.currentTimeMillis();
        IntStream.range(0, 5).parallel().sum();
        long b = System.currentTimeMillis();
        return b - a;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        System.out.println("First stream: " + streamTime());//almost always several hundred ms
        System.out.println("Second stream: " + streamTime());//almost always zero
        byte[] program = Files.readAllBytes(new File("/Users/leijurv/Documents/test.k").toPath());
        String asm = compile(new String(program));
        new FileOutputStream("/Users/leijurv/Documents/blar.s").write(asm.getBytes());
    }
    public static String compile(String program) {
        long a = System.currentTimeMillis();
        ArrayList<Object> lol = Preprocessor.preprocess(program);
        System.out.println("> DONE PREPROCESSING: " + lol);
        long b = System.currentTimeMillis();
        ArrayList<Command> commands = Processor.parse(lol, null);
        System.out.println("> DONE PROCESSING: " + commands);
        long c = System.currentTimeMillis();
        FunctionsContext gc = new FunctionsContext(commands);
        gc.parseRekursively();
        System.out.println("> DONE PARSING: " + commands);
        long d = System.currentTimeMillis();
        for (Command com : commands) {
            com.staticValues();
        }
        System.out.println("> DONE STATIC VALUES: " + commands);
        long e = System.currentTimeMillis();
        List<Pair<String, ArrayList<TACStatement>>> wew = commands.parallelStream()
                .map(com -> (CommandDefineFunction) com)
                .map(com -> new Pair<>(com.getHeader().name, com.totac()))
                .collect(Collectors.toList());
        long f = System.currentTimeMillis();
        for (Pair<String, ArrayList<TACStatement>> pair : wew) {
            Context.VarInfo.printFull = true;
            System.out.println("TAC FOR " + pair.getKey());
            for (int i = 0; i < pair.getValue().size(); i++) {
                System.out.println(i + ":     " + pair.getValue().get(i));
            }
            System.out.println();
        }
        for (Pair<String, ArrayList<TACStatement>> pair : wew) {
            Context.VarInfo.printFull = false;
            System.out.println("TAC FOR " + pair.getKey());
            for (int i = 0; i < pair.getValue().size(); i++) {
                System.out.println(i + ":     " + pair.getValue().get(i));
            }
            System.out.println();
            Context.VarInfo.printFull = true;
        }
        long g = System.currentTimeMillis();
        StringBuilder resp = new StringBuilder();
        resp.append(HEADER);
        resp.append('\n');
        resp.append(wew.parallelStream().map(pair -> CommandDefineFunction.generateX86(pair.getKey(), pair.getValue())).collect(Collectors.joining()));
        resp.append(FOOTER);
        resp.append('\n');
        long h = System.currentTimeMillis();
        String loll = ("overall " + (h - a) + " preprocessor " + (b - a) + " processor " + (c - b) + " parse " + (d - c) + " static " + (e - d) + " tacgen " + (f - e) + " debugtac " + (g - f) + " x86gen " + (h - g));
        System.out.println(loll);
        System.err.println(loll);
        return resp.toString();
    }
    private static final String HEADER = "    .section    __TEXT,__text,regular,pure_instructions\n"
            + "    .macosx_version_min 10, 10";
    private static final String FOOTER = "\n"
            + ".section	__TEXT,__cstring,cstring_literals\n"
            + "lldformatstring:\n"
            + "	.asciz	\"%lld\\n\"\n"
            + ".subsections_via_symbols";
}
