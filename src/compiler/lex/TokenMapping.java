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
        MAPPINGSput(STARTPAREN, '(');
        MAPPINGSput(ENDPAREN, ')');
        MAPPINGSput(COMMA, ',');
        MAPPINGSput(OPERATOR.create(Operator.MULTIPLY), '*');
        MAPPINGSput(OPERATOR.create(Operator.DIVIDE), '/');
        MAPPINGSput(OPERATOR.create(Operator.MOD), '%');
        MAPPINGSput(SEMICOLON, ';');
        MAPPINGSput(STARTBRAKT, '[');
        MAPPINGSput(ENDBRKT, ']');
        MAPPINGSput(PERIOD, '.');
        MAPPINGSput(OPERATOR.create(Operator.NOT_EQUAL), "≠", "!=");
        MAPPINGSput(OPERATOR.create(Operator.GREATER_OR_EQUAL), "≥", ">=");
        MAPPINGSput(OPERATOR.create(Operator.LESS_OR_EQUAL), "≤", "<=");
    }
    public static void MAPPINGSput(Token t, char c) {
        MAPPINGS.put(c + "", t);
    }
    public static void MAPPINGSput(Token t, String... strs) {
    }
    public static boolean mapsToToken(char ch) {
        return MAPPINGS.containsKey(ch + "");
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
