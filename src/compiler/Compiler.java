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
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 *
 * @author leijurv
 */
public class Compiler {
    public static long streamTime() {
        long a = System.currentTimeMillis();
        IntStream.range(0, 5).sum();
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
        ArrayList<String> k = Preprocessor.preprocess(new String(program));
        ArrayList<Object> lol = new ArrayList<>();
        for (String l : k) {
            lol.add(l);
        }
        ArrayList<Command> commands = Processor.parse(lol, null);
        System.out.println(commands);
        FunctionsContext gc = new FunctionsContext(commands);
        gc.parseRekursively();
        System.out.println(commands);
        for (Command com : commands) {
            com.staticValues();
        }
        System.out.println(commands);
        StringBuilder resp = new StringBuilder();
        resp.append(HEADER);
        resp.append('\n');
        for (Command com : commands) {
            CommandDefineFunction wew = (CommandDefineFunction) com;
            wew.generateX86(resp);
        }
        resp.append(FOOTER);
        resp.append('\n');
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
