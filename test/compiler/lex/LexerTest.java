/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.Keyword;
import compiler.token.Token;
import static compiler.token.TokenType.*;
import java.util.ArrayList;
import java.util.Arrays;
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
public class LexerTest {
    public LexerTest() {
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
     * Test of lex method, of class Lexer.
     */
    @Test
    public void testLex() {
        System.out.println("lex");
        testLexing("wew()", VARIABLE.create("wew"), STARTPAREN, ENDPAREN);
        testLexing("420", NUM.create("420"));
        testLexing("a420", VARIABLE.create("a420"));
        testLexing("for 4", KEYWORD.create(Keyword.FOR), NUM.create("4"));
    }
    public void testLexing(String input, Token... expected) {
        ArrayList<Token> expResult = new ArrayList<>(Arrays.asList(expected));
        ArrayList<Token> result = Lexer.lex(input);
        assertEquals(expResult, result);
    }
}
