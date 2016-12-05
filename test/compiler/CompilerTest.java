/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.tac.optimize.OptimizationSettings;
import compiler.tac.optimize.TACOptimizer;
import compiler.tac.optimize.UselessTempVars;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author leijurv
 */
public class CompilerTest {
    public CompilerTest() {
    }
    public static String read(String testname) throws IOException {
        return new String(Files.readAllBytes(Paths.get("test/tests/" + testname)));
    }
    @BeforeClass
    public static void setUpClass() {
    }
    @AfterClass
    public static void tearDownClass() {
    }
    @Before
    public void setUp() {
    }
    @After
    public void tearDown() {
    }
    @Test
    public void testSimpleCompile() throws Exception {
        verifyCompilation("func main(){\nprint(5)\nprint(6)\n}", true, "5\n6\n");
        verifyCompilation("func main(){\nprint(5)\n}", true, "5\n");
        verifyCompilation("func main(){\na:=420\nprint(a)\n}", true, "420\n");
        verifyCompilation("func main(){\na:=420\nprint(a+a)\n}", true, "840\n");
        verifyCompilation("func main(){\na:=420\nprint((a-1)*3)\n}", true, "1257\n");
    }
    @Test
    public void testSimpleNonCompile() throws Exception {
        shouldntCompile("func main(){\nprint(5)\nprint6)\n}");
        shouldntCompile("");
        shouldntCompile("func main({}");
        shouldntCompile("func main(){}");
        shouldntCompile("func main(){\n}");
    }
    @Test
    public void testLinkedList() throws Exception {
        String[] structDefinitionVariants = {"struct linked{\n"//ensure that the position of fields in the struct doesn't affect execution
            + "	long this\n"
            + "	linked* next\n"
            + "	bool hasNext\n"
            + "}", "struct linked{\n"
            + "	long this\n"
            + "	bool hasNext\n"
            + "	linked* next\n"
            + "}", "struct linked{\n"
            + "	bool hasNext\n"
            + "	long this\n"
            + "	linked* next\n"
            + "}", "struct linked{\n"
            + "	bool hasNext\n"
            + "	linked* next\n"
            + "	long this\n"
            + "}", "struct linked{\n"
            + "	linked* next\n"
            + "	bool hasNext\n"
            + "	long this\n"
            + "}", "struct linked{\n"
            + "	linked* next\n"
            + "	long this\n"
            + "	bool hasNext\n"
            + "}"};
        String cont = "func main(){\n"
                + "	ll:=newLinked(1)\n"
                + "	for long i=1; i<(long)30; i=i+1{\n"
                + "		ll=add(factorial(i),ll)\n"
                + "	}\n"
                + "	pll(ll)\n"
                + "}\n"
                + "func newLinked(long val) linked*{\n"
                + "	linked* root=(linked*)malloc(17)\n"
                + "	root[0].this=val\n"
                + "	root[0].hasNext=false\n"
                + "	return root\n"
                + "}\n"
                + "func add(long i, linked* ptr) linked*{\n"
                + "	newRoot:=newLinked(i)\n"
                + "	newRoot[0].hasNext=true\n"
                + "	newRoot[0].next=ptr\n"
                + "	return newRoot\n"
                + "}\n"
                + "func pll(linked* ptr){\n"
                + "	print((*ptr).this)\n"
                + "	if (*ptr).hasNext {\n"
                + "		pll((*ptr).next)\n"
                + "	}\n"
                + "}\n"
                + "func factorial(long i) long{\n"
                + "	if i≥(long)1{\n"
                + "		return i*factorial(i-1)\n"
                + "	}\n"
                + "	return 1\n"
                + "}";
        String result = "-7055958792655077376\n"
                + "-5968160532966932480\n"
                + "-5483646897237262336\n"
                + "-1569523520172457984\n"
                + "7034535277573963776\n"
                + "-7835185981329244160\n"
                + "8128291617894825984\n"
                + "-1250660718674968576\n"
                + "-4249290049419214848\n"
                + "2432902008176640000\n"
                + "121645100408832000\n"
                + "6402373705728000\n"
                + "355687428096000\n"
                + "20922789888000\n"
                + "1307674368000\n"
                + "87178291200\n"
                + "6227020800\n"
                + "479001600\n"
                + "39916800\n"
                + "3628800\n"
                + "362880\n"
                + "40320\n"
                + "5040\n"
                + "720\n"
                + "120\n"
                + "24\n"
                + "6\n"
                + "2\n"
                + "1\n"
                + "1\n";
        String intVersionResult = "-1241513984\n"
                + "-1375731712\n"
                + "1484783616\n"
                + "-1853882368\n"
                + "2076180480\n"
                + "-775946240\n"
                + "862453760\n"
                + "-522715136\n"
                + "-1195114496\n"
                + "-2102132736\n"
                + "109641728\n"
                + "-898433024\n"
                + "-288522240\n"
                + "2004189184\n"
                + "2004310016\n"
                + "1278945280\n"
                + "1932053504\n"
                + "479001600\n"
                + "39916800\n"
                + "3628800\n"
                + "362880\n"
                + "40320\n"
                + "5040\n"
                + "720\n"
                + "120\n"
                + "24\n"
                + "6\n"
                + "2\n"
                + "1\n"
                + "1\n";
        for (String a : structDefinitionVariants) {
            String program = a + "\n" + cont;
            verifyCompilation(program, true, result);
            program = program.replace("(long)", "");
            program = program.replace("long", "int");
            verifyCompilation(program, true, intVersionResult);
        }
    }
    @Test
    public void testOverwriting() throws Exception {
        String header = "func main(){\nprint(test(1,2,3,4))\n}\nfunc test(int a,int b,int c,int j)int{\n";
        String footer = "	return a + (b + c * j + a) + j * (c * j)\n"
                + "}";
        String[] body = {"	print( b + c * j + a)\n",
            "	print(a+b)\n",
            "	print(c*j)\n"};
        String[] outputs = {"15\n", "3\n", "12\n"};
        for (int a = 0; a <= 1; a++) {
            for (int b = 0; b <= 1; b++) {
                for (int c = 0; c <= 1; c++) {
                    String program = header + (a == 1 ? body[0] : "") + (b == 1 ? body[1] : "") + (c == 1 ? body[2] : "") + footer;
                    String out = (a == 1 ? outputs[0] : "") + (b == 1 ? outputs[1] : "") + (c == 1 ? outputs[2] : "") + "64\n";
                    verifyCompilation(program, true, out);
                }
            }
        }
    }
    public static void shouldntCompile(String program) throws IOException, InterruptedException {
        verifyCompilation(program, false, null);
    }
    public static void verifyFileCompilationTrue(String filename) throws IOException, InterruptedException {
        verifyCompilation(read(filename + ".k"), true, read(filename + ".t"));
    }
    public static void verifyPackageCompilation(File dir) throws IOException, InterruptedException {
        File desiredOutput = new File(dir, "output");
        assertTrue("Package " + dir + " needs an " + desiredOutput, desiredOutput.exists());
        String desiredOut = new String(Files.readAllBytes(desiredOutput.toPath()));
        String asm = Compiler.compile(new File(dir, "main.k").toPath(), OptimizationSettings.ALL);
        verifyASM(asm, true, desiredOut);
    }
    public static void verifyCompilation(String program, boolean shouldCompile, String desiredExecutionOutput) throws IOException, InterruptedException {
        try {
            //first check with all optimizations
            //if it works with correct output with all optimizations, then we are gud
            verifyCompilation(program, shouldCompile, desiredExecutionOutput, OptimizationSettings.ALL, false);
        } catch (Exception e) {
            verifyCompilation(program, shouldCompile, desiredExecutionOutput, OptimizationSettings.NONE, true);
            //don't try/catch the no-optimization, because if that fails then that's the error we want to throw
            if (!shouldCompile) {
                return;
                //if it shouldn't compile, and the test was successful (i e it actually didn't compile)
                //we don't need to go on to check other things, it failed without even applying any optimizations
            }
            //ok so it works with none
            detective(program, desiredExecutionOutput, e);
            e.printStackTrace();
            throw new IllegalStateException("Detective failed" + e);//shouldn't get to here
        }
    }
    public static Object detective(String program, String desiredExecutionOutput, Exception withAll) {//setting the return type to non-void ensures that it cannot exit without throwing SOME exception
        //no exception with false,NONE
        //exception with true,ALL
        try {
            verifyCompilation(program, true, desiredExecutionOutput, new OptimizationSettings(false, true), false);
        } catch (Exception e) {
            //exception isn't caused by any optimization settings
            e.printStackTrace();
            throw new IllegalStateException("Exception caused by setting staticValues to true with optimizationsettings staying at NONE " + e);
        }
        //no exception with *,NONE
        //try enabling individual optimizations
        for (int i = 0; i < TACOptimizer.opt.size(); i++) {
            OptimizationSettings set = new OptimizationSettings(false, true);
            set.setEnabled(i, true);
            try {
                verifyCompilation(program, true, desiredExecutionOutput, set, false);
            } catch (Exception e) {
                //if enabling one on its own can trigger it, let's just throw that
                e.printStackTrace();
                throw new IllegalStateException("Caused by optimization " + i + " " + TACOptimizer.opt.get(i) + " " + e);
            }
        }
        int uselessTemp = TACOptimizer.opt.indexOf(UselessTempVars.class);
        for (int i = 0; i < TACOptimizer.opt.size(); i++) {
            if (i == uselessTemp) {
                continue;
            }
            OptimizationSettings set = new OptimizationSettings(false, true);
            set.setEnabled(i, true);
            set.setEnabled(uselessTemp, true);
            try {
                verifyCompilation(program, true, desiredExecutionOutput, set, false);
            } catch (Exception e) {
                //if enabling one with uselesstempvars can trigger it, let's just throw that
                e.printStackTrace();
                throw new IllegalStateException("Caused by uselesstempvars AND optimization " + i + " " + TACOptimizer.opt.get(i) + " " + e);
            }
        }
        withAll.printStackTrace();
        throw new IllegalStateException("Exception caused when all are enabled, but not when any are enabled individually, alone or with uselesstempvars" + withAll);
    }
    public static void verifyCompilation(String program, boolean shouldCompile, String desiredExecutionOutput, OptimizationSettings settings, boolean useAssert) throws IOException, InterruptedException {
        if (!shouldCompile) {
            assertNull(desiredExecutionOutput);
        }
        String compiled;
        try {
            compiled = Compiler.compile(program, settings);
            assertEquals(true, shouldCompile);
        } catch (Exception e) {
            if (shouldCompile) {
                throw e;
            }
            return;
        }
        assertNotNull(compiled);
        verifyASM(compiled, useAssert, desiredExecutionOutput);
    }
    public static void verifyASM(String compiled, boolean useAssert, String desiredExecutionOutput) throws IOException, InterruptedException {
        if (!new File("/usr/bin/gcc").exists()) {
            assertNull("GCC must exist", "GCC must exist");
        }
        File asm = File.createTempFile("kittehtest" + System.nanoTime() + "_" + compiled.hashCode(), ".s");
        File executable = new File(asm.getAbsolutePath().replace(".s", ".o"));
        assertEquals(false, executable.exists());
        assertEquals(true, asm.exists());
        System.out.println("Writing to file " + asm);
        try (FileOutputStream out = new FileOutputStream(asm)) {
            out.write(compiled.getBytes());
        }
        assertEquals(true, asm.exists());
        String[] compilationCommand = {"/usr/bin/gcc", "-o", executable.getAbsolutePath(), asm.getAbsolutePath()};
        System.out.println(Arrays.asList(compilationCommand));
        Process gcc = new ProcessBuilder(compilationCommand).start();
        if (!gcc.waitFor(10, TimeUnit.SECONDS)) {
            gcc.destroyForcibly();
            assertEquals("GCC timed out????", false, true);
        }
        System.out.println("GCC return value: " + gcc.waitFor());
        if (gcc.waitFor() != 0) {
            int j;
            StringBuilder result = new StringBuilder();
            while ((j = gcc.getErrorStream().read()) >= 0) {
                result.append((char) j);
            }
            while ((j = gcc.getInputStream().read()) >= 0) {
                result.append((char) j);
            }
            System.out.println(result);
            System.out.println("Oh well");
        }
        assertEquals(0, gcc.waitFor());
        assertEquals(true, executable.exists());
        Process ex = new ProcessBuilder(executable.getAbsolutePath()).redirectError(Redirect.INHERIT).start();
        if (!ex.waitFor(100, TimeUnit.SECONDS)) {
            ex.destroyForcibly();
            assertEquals("Subprocess timed out", false, true);
        }
        int j;
        StringBuilder result = new StringBuilder();
        while ((j = ex.getInputStream().read()) >= 0) {
            result.append((char) j);
        }
        System.out.println("Execution output \"" + result + "\"");
        if (useAssert) {
            assertEquals(desiredExecutionOutput, result.toString());
        } else if (!desiredExecutionOutput.equals(result.toString())) {
            throw new IllegalStateException(desiredExecutionOutput + "--" + result.toString());
        }
    }
}
