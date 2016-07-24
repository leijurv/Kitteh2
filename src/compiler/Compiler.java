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
        System.out.println("First stream: " + streamTime());
        System.out.println("Second stream: " + streamTime());
        byte[] program = Files.readAllBytes(new File("/Users/leijurv/Documents/functest.k").toPath());
        ArrayList<String> k = Preprocessor.preprocess(new String(program));
        ArrayList<Object> lol = new ArrayList<>();
        for (String l : k) {
            lol.add(l);
        }
        ArrayList<Command> commands = Processor.parse(lol, new Context());
        System.out.println(commands);
        for (Command com : commands) {
            //com.staticValues();
            //currently if you do something like
            //sum=0
            //for i<100
            //   sum=sum+i
            //then it'l optimize the sum=sum+i to sum=0+i because it doesn't deal with loops properly
            //TODO fix this and don't just comment it out
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
        new FileOutputStream("/Users/leijurv/Documents/blar.s").write(resp.toString().getBytes());
    }
    static String HEADER = "    .section    __TEXT,__text,regular,pure_instructions\n"
            + "    .macosx_version_min 10, 10";
    static String FOOTER = "\n"
            + ".section	__TEXT,__cstring,cstring_literals\n"
            + "L_.str:                                 ## @.str\n"
            + "	.asciz	\"%i\\n\"\n"
            + ".subsections_via_symbols";
}
