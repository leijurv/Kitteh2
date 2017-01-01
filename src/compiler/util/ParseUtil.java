/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import compiler.Context;
import compiler.Keyword;
import compiler.Operator;
import compiler.token.Token;
import compiler.token.TokenType;
import compiler.type.Type;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author leijurv
 */
public class ParseUtil {
    public static Type typeFromTokens(List<Token> tokens, Context context) {
        if (tokens.isEmpty()) {
            return null;
        }
        String accessing = null;
        if (tokens.size() > 1 && tokens.get(1) == TokenType.ACCESS) {
            accessing = (String) tokens.get(0).data();
            tokens = tokens.subList(2, tokens.size());
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
                if (accessing != null) {
                    name = accessing + "::" + name;
                }
                TypeStruct struct = context.getStruct(name);
                if (struct == null) {
                    return null;
                }
                tp = struct;
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
    public static List<List<Token>> splitList(List<Token> list, TokenType splitOn) {
        List<List<Token>> result = new ArrayList<>();
        List<Token> temp = new ArrayList<>();
        int numParen = 0;
        for (Token t : list) {
            if (t == TokenType.STARTPAREN) {
                numParen++;
            }
            if (t == TokenType.ENDPAREN) {
                numParen--;
            }
            if (t.tokenType() == splitOn && numParen == 0) {
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
    public static Type typeFromObjs(ArrayList<Object> o, Context context) {
        ArrayList<Token> tmp = new ArrayList<>(o.size());
        for (Object obj : o) {
            if (!(obj instanceof Token)) {
                return null;
            }
            tmp.add((Token) obj);
        }
        return ParseUtil.typeFromTokens(tmp, context);
    }
    /**
     * Recursively flatten an ArrayList of arbitrary depth, returning a stream
     * of all instances of a given class
     *
     * @param <T> The generic type to search for
     * @param searchingFor the class to search for
     * @param filter just a PredicateT to filter which elements to return
     * @param inp the arraylist to flatten
     * @return a stream of all items at any depth that are instances of
     * searchingFor
     */
    public static <T> Stream<T> filteredFlatten(Class<T> searchingFor, Predicate<? super T> filter, ArrayList<?> inp) {
        return Stream.of(inp.stream().filter(searchingFor::isInstance).map(searchingFor::cast).filter(filter), inp.stream().filter(ArrayList.class::isInstance).map(x -> filteredFlatten(searchingFor, filter, (ArrayList<?>) x)).flatMap(x -> x)).flatMap(x -> x);
    }
    public static <SearchingFor, SearchingIn> Stream<SearchingFor> flatten(ArrayList<SearchingIn> inp, Class<SearchingFor> searchingFor) {
        return filteredFlatten(searchingFor, x -> true, inp);
    }
}
