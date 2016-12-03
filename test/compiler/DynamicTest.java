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
    public final String filename;
    public DynamicTest(String s) {
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
        CompilerTest.verifyFileCompilationTrue(filename);
    }
    @Parameters
    public static Collection filenames() {
        return Arrays.stream(new File("test/tests/").listFiles()).map(x -> x.getName()).filter(x -> x.endsWith(".k")).map(x -> x.substring(0, x.length() - 2)).collect(Collectors.toList());
    }
}
