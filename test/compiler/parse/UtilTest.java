/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author leijurv
 */
public class UtilTest {
    public UtilTest() {
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
    /**
     * Test of trimPath method, of class Util.
     */
    @Test
    public void testTrimPath() {
        System.out.println("trimPath");
        assertEquals("a", Util.trimPath("a"));
        assertEquals("../x", Util.trimPath("../x"));
        assertEquals("a/b", Util.trimPath("a/x/../b"));
        assertEquals("b/c/", Util.trimPath("b/c/d/e/../../"));
        assertEquals("wew/lad/cat", Util.trimPath("wew/lad/cat"));
        assertEquals("wew/lad/lol", Util.trimPath("wew/lad/cat/../lol"));
        assertEquals("../wew/lad/lol", Util.trimPath("../wew/lad/cat/../lol"));
        assertEquals("../../wew/lad/lol", Util.trimPath("../../wew/lad/cat/../lol"));
        assertEquals("../wew/lad/lol", Util.trimPath("../kitty/../wew/lad/cat/../lol"));
    }
}
