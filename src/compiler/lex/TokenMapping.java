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
    private static final HashMap<String, Token> MAPPINGS = new HashMap<>();
    static {
        put(STARTPAREN, "(");
        put(ENDPAREN, ")");
        put(COMMA, ",");
        put(OPERATOR.create(Operator.MULTIPLY), "*");
        put(OPERATOR.create(Operator.DIVIDE), "/");
        put(OPERATOR.create(Operator.MOD), "%");
        put(OPERATOR.create(Operator.PLUS), "+");
        put(OPERATOR.create(Operator.MINUS), "-");
        put(SEMICOLON, ";");
        put(STARTBRAKT, "[");
        put(ENDBRKT, "]");
        put(PERIOD, ".");
        put(OPERATOR.create(Operator.NOT_EQUAL), "≠", "!=");
        put(OPERATOR.create(Operator.GREATER_OR_EQUAL), "≥", ">=");
        put(OPERATOR.create(Operator.LESS_OR_EQUAL), "≤", "<=");
        put(OPERATOR.create(Operator.EQUAL), "==");
        put(SETEQUAL, "=");
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
