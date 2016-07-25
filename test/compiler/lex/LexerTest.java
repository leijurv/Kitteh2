/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.Keyword;
import compiler.token.Token;
import compiler.token.TokenEndParen;
import compiler.token.TokenKeyword;
import compiler.token.TokenNum;
import compiler.token.TokenStartParen;
import compiler.token.TokenVariable;
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
        testLexing("wew()", new TokenVariable("wew"), new TokenStartParen(), new TokenEndParen());
        testLexing("420", new TokenNum("420"));
        testLexing("a420", new TokenVariable("a420"));
        testLexing("for 4", new TokenKeyword(Keyword.FOR), new TokenNum("4"));
    }
    public void testLexing(String input, Token... expected) {
        ArrayList<Token> expResult = new ArrayList<>(Arrays.asList(expected));
        ArrayList<Token> result = Lexer.lex(input);
        assertEquals(expResult, result);
    }
    /**
     * Test of readAlphanumerical method, of class Lexer.
     */
    @Test
    public void testReadAlphanumerical() {
        System.out.println("readAlphanumerical");
        Lexer instance = new Lexer("wew420.");
        String expResult = "wew420";
        String result = instance.readAlphanumerical();
        assertEquals(expResult, result);
    }
    /**
     * Test of readNumerical method, of class Lexer.
     */
    @Test
    public void testReadNumerical() {
        System.out.println("readNumerical");
        Lexer instance = new Lexer("5021.3(");
        String expResult = "5021.3";
        String result = instance.readNumerical();
        assertEquals(expResult, result);
    }
}
