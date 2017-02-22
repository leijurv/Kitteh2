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
import java.nio.file.Path;
import java.util.IllformedLocaleException;
import java.util.stream.IntStream;
import org.w3c.dom.ls.LSException;

/**
 *
 * @author leijurv
 */
public class Main {
    public static final boolean ALLOW_CLI = true;
    private static final String DEFAULT_OUT_FILE = System.getProperty("user.home") + "/Documents/blar.s";
    private static final String DEFAULT_IN_FILE = System.getProperty("user.home") + "/Documents/test.k";
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
        //^ for more accurate benchmarks of internal components, make sure that the java lambda / stream / forkjoinpool system is all "warmed up" before getting into the main compiler.
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
                    i++;
                    inFile = args[i];
                    break;
                case "-o":
                    if (i + 1 == args.length) {
                        throw new LSException((short) "wew".hashCode(), "You gotta give a file");
                    }
                    i++;
                    outFile = args[i];
                    continue;
                case "-I":
                    inFile = "/dev/stdin";
                    break;
                case "-O"://not compatible with executable
                    outFile = "/dev/stdout";
                    continue;
                case "-v"://lots of options
                case "-V":
                case "-verbose":
                case "--verbose":
                    if (ALLOW_CLI) {
                        Compiler.VERBOSE = true;
                    }
                    break;
                case "-m":
                    if (ALLOW_CLI) {
                        Compiler.METRICS = true;
                    }
                    continue;
                case "-d":
                    if (ALLOW_CLI) {
                        Compiler.DETERMINISTIC = true;
                    }
                    break;
                case "-e"://LOTS of options
                case "-E":
                case "--e":
                case "--E":
                case "-executable":
                case "--executable":
                    executable = true;
                    continue;
                case "-obf":
                case "--obf":
                    if (ALLOW_CLI) {
                        Compiler.OBFUSCATE = true;
                    }
                    break;
            }
        }
        Path inPath = new File(inFile).toPath();
        System.out.println("Input file: " + inFile);
        System.out.println("Output file: " + outFile);
        String asm = Compiler.compile(inPath, OPTIMIZE ? ALL : NONE);//<-- actual compilation
        File asmFile;
        if (executable) {
            asmFile = File.createTempFile("temp", ".s");
        } else {
            asmFile = new File(outFile);
        }
        try (FileOutputStream out = new FileOutputStream(asmFile)) {
            out.write(asm.getBytes("UTF-8"));
        }
        if (!executable) {
            return;
        }
        if (!new File("/usr/bin/gcc").exists()) {
            throw new RuntimeException("/usr/bin/gcc required to create executable");
        }
        Process gcc = new ProcessBuilder("/usr/bin/gcc", "-o", outFile, asmFile.getAbsolutePath()).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
        System.out.println("GCC return value: " + gcc.waitFor());//any other gcc output (stdout or stderr) will be outputted normally because of the redirects ^
    }
}
