/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import static compiler.tac.optimize.OptimizationSettings.*;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ProcessBuilder.Redirect;
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
    private static final boolean OPTIMIZE = true; //if it's being bad, see if changing this to false fixes it
    public static long streamTime() {
        long a = System.currentTimeMillis();
        IntStream.range(0, 5).map((int x) -> x + 1).parallel().sum();
        long b = System.currentTimeMillis();
        return b - a;
    }
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        http://github.com/leijurv/Kitteh2
        System.out.println("First stream: " + streamTime()); //almost always several hundred ms
        System.out.println("Second stream: " + streamTime()); //almost always zero
        String inFile = DEFAULT_IN_FILE;
        String outFile = DEFAULT_OUT_FILE;
        boolean executable = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                default:
                    continue;
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
                case "-v":
                case "-V":
                case "-verbose":
                case "--verbose":
                    Compiler.VERBOSE = true;
                    break;
                case "-m":
                    Compiler.METRICS = true;
                    break;
                case "-d":
                    Compiler.DETERMINISTIC = true;
                    break;
                case "-e":
                case "-E":
                case "--e":
                case "--E":
                case "-executable":
                case "--executable":
                    executable = true;
            }
        }
        String asm = Compiler.compile(new File(inFile).toPath(), OPTIMIZE ? ALL : NONE);
        File asmFile = executable ? File.createTempFile("temp", ".s") : new File(outFile);
        try (FileOutputStream lol = new FileOutputStream(asmFile)) {
            lol.write(asm.getBytes("UTF-8"));
        }
        if (!executable) {
            return;
        }
        Process gcc = new ProcessBuilder("/usr/bin/gcc", "-o", outFile, asmFile.getAbsolutePath()).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
        System.out.println("GCC return value: " + gcc.waitFor());
    }
}
