/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
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
        StripComments instance = new StripComments();
        assertEquals(instance.transform("cat"), "cat");
        assertEquals(instance.transform("cat\n"), "cat\n");
        assertEquals(instance.transform("cat\nwew"), "cat\nwew");
        assertEquals(instance.transform("cat // memer"), "cat ");
        assertEquals(instance.transform("cat // memer\n"), "cat \n");
        assertEquals(instance.transform("cat // memer\nwew"), "cat \nwew");
        assertEquals(instance.transform("ca/*wew*/t"), "cat");
        assertEquals(instance.transform("ca/*we\nw*/t"), "cat");
        assertEquals(instance.transform("ca'//'t"), "ca'//'t");
        assertEquals(instance.transform("ca//'t"), "ca");
        assertEquals(instance.transform("ca\"//\"t"), "ca\"//\"t");
        assertEquals(instance.transform("ca//\"t"), "ca");
        assertEquals(instance.transform("ca'\"//'"), "ca'\"//'");
        assertEquals(instance.transform("ca''//'"), "ca''");
    }
}
