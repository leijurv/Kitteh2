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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
    public void testCompilation() throws Exception {
        System.out.println(filename);
        if (filename.getName().endsWith(".k")) {
            String s = filename.getName();
            CompilerTest.verifyFileCompilationTrue(s.substring(0, s.length() - 2));
            return;
        }
        if (filename.isDirectory()) {
            CompilerTest.verifyPackageCompilation(filename);
            return;
        }
    }
    @Parameters
    public static Collection filenames() {
        return Arrays.stream(new File("test/tests/").listFiles()).collect(Collectors.toList());
        //return Arrays.stream(new File("test/tests/").listFiles()).map(x -> x.getName()).filter(x -> x.endsWith(".k")).map(x -> x.substring(0, x.length() - 2)).collect(Collectors.toList());
    }
}
