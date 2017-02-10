/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.asm.ASMArchitecture;
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
    private static final ASMArchitecture DEFAULT_ARCH = ASMArchitecture.X86;
    private static final String DEFAULT_OUT_FILE = System.getProperty("user.home") + "/Documents/blar.s";
    private static final String DEFAULT_IN_FILE = System.getProperty("user.home") + "/Documents/test.k";
    private static final boolean OPTIMIZE = true; //if it's being bad, see if changing this to false fixes it
    public static final boolean ALLOW_CLI = true;
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
        ASMArchitecture arch = DEFAULT_ARCH;
        boolean executable = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                default:
                    continue;
                case "-a":
                case "--a":
                case "--arch":
                case "-arch":
                case "-architecture":
                case "--architecture":
                    if (i + 1 == args.length) {
                        throw new RuntimeException("No arch provided");
                    }
                    i++;
                    arch = ASMArchitecture.fromString(args[i]);
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
                        throw new LSException((short) "urmum".hashCode(), "You gotta give a file");
                    }
                    i++;
                    outFile = args[i];
                    continue;
                case "-I":
                    inFile = "/dev/stdin";
                    break;
                case "-O":
                    outFile = "/dev/stdout";
                    continue;
                case "-v":
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
                case "-e":
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
        String asm = Compiler.compile(new File(inFile).toPath(), OPTIMIZE ? ALL : NONE, arch);
        File asmFile = executable ? File.createTempFile("temp", ".s") : new File(outFile);
        try (FileOutputStream lol = new FileOutputStream(asmFile)) {
            lol.write(asm.getBytes("UTF-8"));
        }
        if (!executable) {
            return;
        }
        Process gcc = new ProcessBuilder("/usr/bin/gcc", "-o", outFile, asmFile.getAbsolutePath()).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
        System.out.println("GCC return value: " + gcc.waitFor());
        /*for (int i = 0; i < TACOptimizer.opt.size(); i++) {
            OptimizationSettings set = new OptimizationSettings(true, true);
            set.setEnabled(i, false);
            try {
                System.out.println("DISABLING " + TACOptimizer.opt.get(i));
                pls(inFile, outFile, executable, set);
            } catch (Exception e) {
                //if enabling one on its own can trigger it, let's just throw that
                e.printStackTrace();
                throw new IllegalStateException("Caused by optimization " + i + " " + TACOptimizer.opt.get(i) + " " + e);
            }
        }
    }
    public static void pls(String inFile, String outFile, boolean executable, OptimizationSettings set) throws Exception {
        String asm = Compiler.compile(new File(inFile).toPath(), set);
        File asmFile = executable ? File.createTempFile("temp", ".s") : new File(outFile);
        try (FileOutputStream lol = new FileOutputStream(asmFile)) {
            lol.write(asm.getBytes("UTF-8"));
        }
        if (!executable) {
            return;
        }
        Process gcc = new ProcessBuilder("/usr/bin/gcc", "-o", outFile, asmFile.getAbsolutePath()).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
        System.out.println("GCC return value: " + gcc.waitFor());
        new ProcessBuilder(outFile).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start().waitFor();
    }*/
    }
}
