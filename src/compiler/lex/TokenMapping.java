/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.lex;
import compiler.Operator;
import compiler.token.Token;
import compiler.token.TokenType;
import static compiler.token.TokenType.*;
import java.util.HashMap;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author leijurv
 */
public class TokenMapping {
    private static final HashMap<String, Token> MAPPINGS = new HashMap<>();
    static {
        put(OPERATOR.create(Operator.NOT_EQUAL), "≠");
        put(OPERATOR.create(Operator.GREATER_OR_EQUAL), "≥");
        put(OPERATOR.create(Operator.LESS_OR_EQUAL), "≤");
        put(OPERATOR.create(Operator.AND), "&&");
        put(SETEQUAL.create(false), "=");
        put(SETEQUAL.create(true), ":=");
        for (Operator op : Operator.values()) {
            put(OPERATOR.create(op), op.toString());
        }
        for (TokenType tt : TokenType.values()) {
            if (tt.primitive()) {
                put(tt, tt.toString());
            }
        }
    }
    public static void put(Token t, String... strs) {
        for (String str : strs) {
            MAPPINGS.put(str, t);
        }
    }
    public static boolean mapsToToken(String s) {
        return MAPPINGS.containsKey(s);
    }
    public static Token getStaticToken(String ch) {
        Token t = MAPPINGS.get(ch);
        if (t == null) {
            throw new WebServiceException();//lol
        }
        return t;
    }
}
