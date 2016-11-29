/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.Context;
import compiler.Keyword;
import compiler.Operator;
import compiler.Struct;
import compiler.token.Token;
import compiler.token.TokenType;
import compiler.type.Type;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class Util {
    public static Type typeFromTokens(List<Token> tokens, Context context) {
        return typeFromTokens(tokens, context, null);
    }
    public static Type typeFromTokens(List<Token> tokens, Context context, String selfRef) {
        if (tokens.isEmpty()) {
            return null;
        }
        Token first = tokens.get(0);
        Type tp;
        switch (first.tokenType()) {
            case KEYWORD:
                Keyword keyword = (Keyword) first;
                if (!keyword.isType()) {
                    return null;
                }
                tp = keyword.type;
                break;
            case VARIABLE:
                String name = (String) first.data();
                if (name.equals(selfRef)) {
                    tp = new TypeStruct(null);
                } else {
                    Struct struct = context.getStruct(name);
                    if (struct == null) {
                        return null;
                    }
                    tp = new TypeStruct(struct);
                }
                break;
            default:
                return null;
        }
        for (int i = 1; i < tokens.size(); i++) {
            if (tokens.get(i) != Operator.MULTIPLY) {
                return null;
            }
            tp = new <Type>TypePointer<Type>(tp); //if there are N *s, it's a N - nested pointer, so for every *, wrap the type in another TypePointer
        }
        return tp;
    }
    static List<List<Token>> splitList(List<Token> list, TokenType splitOn) {
        List<List<Token>> result = new ArrayList<>();
        List<Token> temp = new ArrayList<>();
        for (Token t : list) {
            if (t.tokenType() == splitOn) {
                result.add(temp);
                temp = new ArrayList<>();
            } else {
                temp.add(t);
            }
        }
        if (!list.isEmpty()) {
            result.add(temp);
        }
        return result;
    }
    static Type typeFromObjs(ArrayList<Object> o, Context context) {
        ArrayList<Token> tmp = new ArrayList<>(o.size());
        for (Object obj : o) {
            if (!(obj instanceof Token)) {
                return null;
            }
            tmp.add((Token) obj);
        }
        return Util.typeFromTokens(tmp, context);
    }
}
