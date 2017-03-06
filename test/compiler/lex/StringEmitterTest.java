/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author leijurv
 */
public class StringEmitterTest {
    /**
     * Test of peek method, of class StringEmitter.
     */
    @Test
    public void testPeek() {
        System.out.println("peek");
        StringEmitter instance = new StringEmitter("wew");
        char expResult = 'w';
        char result = instance.peek();
        assertEquals(expResult, result);
    }
    /**
     * Test of pop method, of class StringEmitter.
     */
    @Test
    public void testPop() {
        System.out.println("pop");
        StringEmitter instance = new StringEmitter("abc");
        assertEquals(instance.pop(), 'a');
        assertEquals(instance.peek(), 'b');
    }
    /**
     * Test of has method, of class StringEmitter.
     */
    @Test
    public void testHas() {
        System.out.println("has");
        assertEquals(new StringEmitter("").has(), false);
        assertEquals(new StringEmitter("a").has(), true);
    }
    /**
     * Test of currentPos method, of class StringEmitter.
     */
    @Test
    public void testCurrentPos() {
        System.out.println("currentPos");
        StringEmitter instance = new StringEmitter("cat");
        assertEquals(instance.pos(), 0);
        instance.pop();
        assertEquals(instance.pos(), 1);
    }
    /**
     * Test of substring method, of class StringEmitter.
     */
    @Test
    public void testSubstring() {
        System.out.println("substring");
        StringEmitter instance = new StringEmitter("dank");
        int start = 1;
        instance.pop();
        instance.pop();
        instance.pop();
        assertEquals(instance.substringSince(start), "an");
    }
}
