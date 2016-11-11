/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.Operator;
import compiler.token.Token;
import compiler.token.TokenComma;
import compiler.token.TokenEndParen;
import compiler.token.TokenOperator;
import compiler.token.TokenSemicolon;
import compiler.token.TokenStartParen;
import java.util.HashMap;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author leijurv
 */
public class TokenMapping {
    private static final HashMap<Character, Token> MAPPINGS = new HashMap<>();
    static {
        MAPPINGS.put('(', new TokenStartParen());
        MAPPINGS.put(')', new TokenEndParen());
        MAPPINGS.put(',', new TokenComma());
        MAPPINGS.put('*', new TokenOperator(Operator.MULTIPLY));
        MAPPINGS.put('/', new TokenOperator(Operator.DIVIDE));
        MAPPINGS.put('%', new TokenOperator(Operator.MOD));
        MAPPINGS.put(';', new TokenSemicolon());
    }
    public static boolean charMapsToToken(char ch) {
        return MAPPINGS.containsKey(ch);
    }
    public static Token getStaticToken(char ch) {
        Token t = MAPPINGS.get(ch);
        if (t == null) {
            throw new WebServiceException();//lol
        }
        return t;
    }
}
