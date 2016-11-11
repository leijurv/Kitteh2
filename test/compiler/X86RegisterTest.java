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
import compiler.type.TypeNumerical;
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
        testRegister(X86Register.A, "%rax", "%eax", "%ax", "%al");
        testRegister(X86Register.B, "%rbx", "%ebx", "%bx", "%bl");
        testRegister(X86Register.C, "%rcx", "%ecx", "%cx", "%cl");
        testRegister(X86Register.D, "%rdx", "%edx", "%dx", "%dl");
        testRegister(X86Register.SI, "%rsi", "%esi", "%si", "%sil");
        testRegister(X86Register.DI, "%rdi", "%edi", "%di", "%dil");
        testRegister(X86Register.R8, "%r8", "%r8d", "%r8w", "%r8b");
        testRegister(X86Register.R9, "%r9", "%r9d", "%r9w", "%r9b");
        testRegister(X86Register.R10, "%r10", "%r10d", "%r10w", "%r10b");
        testRegister(X86Register.R11, "%r11", "%r11d", "%r11w", "%r11b");
        testRegister(X86Register.R12, "%r12", "%r12d", "%r12w", "%r12b");
        testRegister(X86Register.R13, "%r13", "%r13d", "%r13w", "%r13b");
        testRegister(X86Register.R14, "%r14", "%r14d", "%r14w", "%r14b");
        testRegister(X86Register.R15, "%r15", "%r15d", "%r15w", "%r15b");
    }
    static final TypeNumerical[] types = {new TypeInt64(), new TypeInt32(), new TypeInt16(), new TypeInt8()};
    public void testRegister(X86Register reg, String... requiredValues) {
        assertEquals(types.length, requiredValues.length);
        for (int i = 0; i < types.length; i++) {
            assertEquals(requiredValues[i], reg.getRegister(types[i]));
        }
    }
}
