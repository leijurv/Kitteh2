/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author averycowan
 */
@RunWith(Parameterized.class)
public class DynamicTest {
    public final File filename;
    public DynamicTest(File s) {
        filename = s;
    }
    @Test
    public void testCompilation() throws Exception {
        if (filename.getName().endsWith(".k")) {
            System.out.println();
            System.out.println("Testing individual " + filename);
            String s = filename.getName();
            CompilerTest.verifyFileCompilationTrue(s.substring(0, s.length() - 2));
            return;
        }
        if (filename.isDirectory() && new File(filename, "output").exists()) {
            System.out.println();
            System.out.println("Testing package " + filename);
            CompilerTest.verifyPackageCompilation(filename);
        }
    }
    @Parameters
    public static Collection filenames() {
        return Arrays.stream(new File("test/tests/").listFiles()).collect(Collectors.toList());
        //return Arrays.stream(new File("test/tests/").listFiles()).map(x -> x.getName()).filter(x -> x.endsWith(".k")).map(x -> x.substring(0, x.length() - 2)).collect(Collectors.toList());
    }
}
