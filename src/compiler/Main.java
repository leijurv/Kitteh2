/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.tac.optimize.OptimizationSettings;
import java.io.File;
import java.io.FileOutputStream;
import java.util.IllformedLocaleException;
import java.util.stream.IntStream;
import org.w3c.dom.ls.LSException;

/**
 *
 * @author leijurv
 */
public class Main {
    protected static final String DEFAULT_OUT_FILE = System.getProperty("user.home") + "/Documents/blar.s";
    protected static final String DEFAULT_IN_FILE = System.getProperty("user.home") + "/Documents/test.k";
    public static final boolean OPTIMIZE = true; //if it's being bad, see if changing this to false fixes it
    public static long streamTime() {
        long a = System.currentTimeMillis();
        IntStream.range(0, 5).map((int x) -> x + 1).parallel().sum();
        long b = System.currentTimeMillis();
        return b - a;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        http://github.com/leijurv/Kitteh2
        System.out.println("First stream: " + streamTime()); //almost always several hundred ms
        System.out.println("Second stream: " + streamTime()); //almost always zero
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
                    break;
                case "-I":
                    inFile = "/dev/stdin";
                    break;
                case "-O":
                    outFile = "/dev/stdout";
                    break;
                default:
                    break;
            }
        }
        Compiler.PRINT_TAC = true;
        String asm = Compiler.compile(new File(inFile).toPath(), new OptimizationSettings(OPTIMIZE, OPTIMIZE));
        new FileOutputStream(outFile).write(asm.getBytes("UTF-8"));
    }
}
