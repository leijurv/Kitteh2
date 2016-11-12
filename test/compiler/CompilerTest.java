/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    public void testCompile() throws Exception {
        System.out.println("compile");
        verifyCompilation("func main(){\nprint(5)\nprint(6)\n}", true, "5\n6\n");
    }
    public void verifyCompilation(String program, boolean shouldCompile, String desiredExecutionOutput) throws IOException, InterruptedException {
        String compiled = null;
        boolean compiledSuccessfully;
        try {
            compiled = Compiler.compile(program);
            compiledSuccessfully = true;
        } catch (Exception e) {
            compiledSuccessfully = false;
        }
        assertEquals(shouldCompile, compiledSuccessfully);
        if (!compiledSuccessfully) {
            return;
        }
        assertNotNull(compiled);
        File asm = File.createTempFile("kittehtest" + System.nanoTime() + "_" + program.hashCode(), ".s");
        File executable = new File(asm.getAbsolutePath().replace(".s", ".o"));
        assertEquals(false, executable.exists());
        assertEquals(true, asm.exists());
        System.out.println("Writing to file " + asm);
        try (FileOutputStream out = new FileOutputStream(asm)) {
            out.write(compiled.getBytes());
        }
        assertEquals(true, asm.exists());
        String[] compilationCommand = {"gcc", "-o", executable.getAbsolutePath(), asm.getAbsolutePath()};
        System.out.println(Arrays.asList(compilationCommand));
        Process gcc = new ProcessBuilder(compilationCommand).redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start();
        System.out.println("GCC return value: " + gcc.waitFor());
        assertEquals(0, gcc.waitFor());
        assertEquals(true, executable.exists());
        Process ex = new ProcessBuilder(executable.getAbsolutePath()).redirectError(Redirect.INHERIT).start();
        ex.waitFor();
        int j;
        StringBuilder result = new StringBuilder();
        while ((j = ex.getInputStream().read()) >= 0) {
            result.append((char) j);
        }
        System.out.println("Execution output" + result);
        assertEquals(desiredExecutionOutput, result.toString());
    }
}
