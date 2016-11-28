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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author leijurv
 */
public class TokenMapping {
    private static final Map<String, Token> MAPPINGS;
    static {
        HashMap<String, Token> map = new HashMap<>();
        map.put("≠", Operator.NOT_EQUAL);
        map.put("≥", Operator.GREATER_OR_EQUAL);
        map.put("≤", Operator.LESS_OR_EQUAL);
        put(SETEQUAL.create(false), map);
        put(SETEQUAL.create(true), map);
        for (Operator op : Operator.values()) {
            put(op, map);
        }
        for (TokenType tt : TokenType.values()) {
            if (tt.primitive()) {
                put(tt, map);
            }
        }
        MAPPINGS = Collections.unmodifiableMap(map);
        verifySane();
    }
    public static void verifySane() {
        for (String s : MAPPINGS.keySet()) {
            switch (s.length()) {
                case 1:
                case 2:
                    continue;
                default:
                    throw new IllegalStateException("Length " + s.length() + " illegal for mapping " + s);
            }
        }
    }
    public static void put(Token t, Map<String, Token> map) {
        map.put(t.toString(), t);
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
