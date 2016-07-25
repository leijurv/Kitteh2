/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
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
public class X86RegisterTest {
    public X86RegisterTest() {
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
     * Test of getRegister method, of class X86Register.
     */
    @Test
    public void testGetRegister() {
        System.out.println("getRegister");
        assertEquals(X86Register.A.getRegister(new TypeInt8()), "%al");
        assertEquals(X86Register.A.getRegister(new TypeInt16()), "%ax");
        assertEquals(X86Register.A.getRegister(new TypeInt32()), "%eax");
        assertEquals(X86Register.A.getRegister(new TypeInt64()), "%rax");
    }
}
