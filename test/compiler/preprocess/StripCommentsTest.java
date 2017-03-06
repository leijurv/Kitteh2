/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author leijurv
 */
public class StripCommentsTest {
    /**
     * Test of transform method, of class StripComments.
     */
    @Test
    public void testTransform() {
        System.out.println("transform");
        assertEquals("c", new StripComments(null).transform("c").get(0).raw());
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
        StripComments st = new StripComments(null);
        List<Line> result = st.transform(a);
        assertEquals(result.toString(), split.length, result.size());
        for (int i = 0; i < split.length; i++) {
            assertEquals(split[i], result.get(i).raw());
        }
    }
}
