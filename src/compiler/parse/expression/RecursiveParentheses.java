/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.Keyword;
import compiler.expression.Expression;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionFunctionCall;
import static compiler.parse.ExpressionParser.parseImpl;
import compiler.parse.Util;
import compiler.token.Token;
import static compiler.token.Token.is;
import static compiler.token.TokenType.COMMA;
import static compiler.token.TokenType.ENDPAREN;
import static compiler.token.TokenType.KEYWORD;
import static compiler.token.TokenType.STARTPAREN;
import static compiler.token.TokenType.VARIABLE;
import compiler.type.Type;
import compiler.type.TypeInt32;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author leijurv
 */
public class RecursiveParentheses extends TokenBased {
    public RecursiveParentheses() {
        super(STARTPAREN);
    }
    @Override
    protected boolean apply(int i, ArrayList<Object> o, Optional<Type> desiredType, Context context) {
        ArrayList<ArrayList<Object>> inParen = new ArrayList<>();
        ArrayList<Object> temp = new ArrayList<>();
        int numParens = 1;
        ArrayList<Object> copy = new ArrayList<>(o);
        int numToRemoveAti = 1;
        copy.remove(i);
        while (i < copy.size()) {
            Object b = copy.remove(i);
            numToRemoveAti++;
            if (b == ENDPAREN) {
                numParens--;
                if (numParens == 0) {
                    if (temp.isEmpty()) {
                        throw new IllegalStateException("Dangling comma");
                    }
                    inParen.add(temp);
                    break;
                }
            }
            if (b == COMMA && numParens == 1) {
                inParen.add(temp);
                temp = new ArrayList<>();
            } else {
                temp.add(b);
            }
            if (b == STARTPAREN) {
                numParens++;
            }
        }
        if (numParens != 0) {
            throw new IllegalStateException("mismatched ( and )");
        }
        if (i != 0 && o.get(i - 1) == Keyword.SIZEOF) {
            if (inParen.size() != 1) {
                throw new RuntimeException();
            }
            Type type = Util.typeFromObjs(inParen.get(0), context);
            if (type == null) {
                throw new RuntimeException();
            }
            for (int j = 0; j < numToRemoveAti; j++) {
                o.remove(i);
            }
            o.set(i - 1, new ExpressionConstNum(type.getSizeBytes(), new TypeInt32()));
            return true;
        }
        if (inParen.size() == 1 && Util.typeFromObjs(inParen.get(0), context) != null) {
            //this is a cast, skip the rest and don't modify these parentheses
            return false;
        } else {
            //not a cast
            for (int j = 0; j < numToRemoveAti; j++) {
                o.remove(i);
            }
        }
        //System.out.println("Doing replace " + o + " " + inParen);
        if (i != 0 && (is(o.get(i - 1), VARIABLE) || is(o.get(i - 1), KEYWORD))) {
            String funcName;
            if (is(o.get(i - 1), VARIABLE)) {
                funcName = (String) ((Token) o.get(i - 1)).data();
            } else {
                funcName = o.get(i - 1).toString();//some functions that you call are also keywords
            }
            List<Type> desiredTypes = context.gc.getHeader(funcName).inputs();
            //System.out.println("Expecting inputs: " + desiredTypes);
            //tfw parallel expression parsing
            //tfw this is a GOOD idea /s
            if (inParen.size() != desiredTypes.size()) {
                throw new SecurityException("mismatched arg count");
            }
            List<Expression> args = IntStream.range(0, inParen.size()).parallel().mapToObj(p -> parseImpl(inParen.get(p), Optional.of(desiredTypes.get(p)), context)).collect(Collectors.toList());
            o.set(i - 1, new ExpressionFunctionCall(context, funcName, args));
            return true;
        }
        if (inParen.size() != 1) {
            throw new IllegalStateException("This has commas or is empty, but isn't a function call " + inParen);
        }
        o.add(i, parseImpl(inParen.get(0), desiredType, context));
        return true;
    }
}
