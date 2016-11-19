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
    }
    public void test(String a, String b) {
        StripComments st = new StripComments();
        List<Line> result = st.transform(a);
        assertEquals(result.toString(), b.split("\n", -1).length, result.size());
        for (int i = 0; i < b.split("\n", -1).length; i++) {
            assertEquals(b.split("\n", -1)[i], result.get(i).raw());
        }
    }
}
