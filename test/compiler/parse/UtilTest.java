/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.util.Kitterature;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author leijurv
 */
public class UtilTest {
    /**
     * Test of trimPath method, of class Util.
     */
    @Test
    public void testTrimPath() {
        System.out.println("trimPath");
        assertEquals("a", Kitterature.trimPath("a"));
        assertEquals("../x", Kitterature.trimPath("../x"));
        assertEquals("a/b", Kitterature.trimPath("a/x/../b"));
        assertEquals("b/c/", Kitterature.trimPath("b/c/d/e/../../"));
        assertEquals("wew/lad/cat", Kitterature.trimPath("wew/lad/cat"));
        assertEquals("wew/lad/lol", Kitterature.trimPath("wew/lad/cat/../lol"));
        assertEquals("../wew/lad/lol", Kitterature.trimPath("../wew/lad/cat/../lol"));
        assertEquals("../../wew/lad/lol", Kitterature.trimPath("../../wew/lad/cat/../lol"));
        assertEquals("../wew/lad/lol", Kitterature.trimPath("../kitty/../wew/lad/cat/../lol"));
    }
}
