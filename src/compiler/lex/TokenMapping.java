/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.Operator;
import compiler.token.Token;
import static compiler.token.TokenType.*;
import java.util.HashMap;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author leijurv
 */
public class TokenMapping {
    private static final HashMap<Character, Token> MAPPINGS = new HashMap<>();
    static {
        MAPPINGS.put('(', STARTPAREN);
        MAPPINGS.put(')', ENDPAREN);
        MAPPINGS.put(',', COMMA);
        MAPPINGS.put('*', OPERATOR.create(Operator.MULTIPLY));
        MAPPINGS.put('/', OPERATOR.create(Operator.DIVIDE));
        MAPPINGS.put('%', OPERATOR.create(Operator.MOD));
        MAPPINGS.put(';', SEMICOLON);
        MAPPINGS.put('[', STARTBRAKT);
        MAPPINGS.put(']', ENDBRKT);
        MAPPINGS.put('.', PERIOD);
        MAPPINGS.put('≠', OPERATOR.create(Operator.NOT_EQUAL));
        MAPPINGS.put('≥', OPERATOR.create(Operator.GREATER_OR_EQUAL));
        MAPPINGS.put('≤', OPERATOR.create(Operator.LESS_OR_EQUAL));
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
