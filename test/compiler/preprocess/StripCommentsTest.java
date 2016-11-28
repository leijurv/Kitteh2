/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import compiler.parse.Line;
import java.util.List;
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
public class StripCommentsTest {
    public StripCommentsTest() {
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
     * Test of transform method, of class StripComments.
     */
    @Test
    public void testTransform() {
        System.out.println("transform");
        test("cat", "cat");
        test("cat\n", "cat\n");
        test("cat\nwew", "cat\nwew");
        test("cat // memer", "cat ");
        test("cat // memer\n", "cat \n");
        test("cat // memer\nwew", "cat \nwew");
        test("ca/*wew*/t", "cat");
        test("ca/*we\nw*/t", "cat");
        test("ca'//'t", "ca'//'t");
        test("ca//'t", "ca");
        test("ca\"//\"t", "ca\"//\"t");
        test("ca//\"t", "ca");
        test("ca'\"//'", "ca'\"//'");
        test("ca''//'", "ca''");
        test("ab//cd/*xyz\nue", "ab\nue");
        test("ab//wew*/cat\nrc", "ab\nrc");
    }
    public void test(String a, String b) {
        String[] split = b.split("\n", -1);
        StripComments st = new StripComments();
        List<Line> result = st.transform(a);
        assertEquals(result.toString(), split.length, result.size());
        for (int i = 0; i < split.length; i++) {
            assertEquals(split[i], result.get(i).raw());
        }
    }
}
