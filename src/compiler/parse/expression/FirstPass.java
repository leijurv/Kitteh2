/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.Keyword;
import compiler.expression.Expression;
import compiler.expression.ExpressionConst;
import compiler.expression.ExpressionConstChar;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionConstStr;
import compiler.expression.ExpressionVariable;
import compiler.token.Token;
import static compiler.token.TokenType.*;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeInt32;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
class FirstPass implements ExpressionParseStep {
    @Override
    public boolean apply(ArrayList<Object> o, Optional<Type> desiredType, Context context) {
        int currentlyInParentheses = 0;
        for (int i = 0; i < o.size(); i++) {
            if (!(o.get(i) instanceof Token)) {
                continue;
            }
            Token ob = (Token) o.get(i);
            switch (ob.tokenType()) {
                case STARTPAREN:
                    currentlyInParentheses++;
                    break;
                case ENDPAREN:
                    currentlyInParentheses--;
                    break;
                case NUM:
                    if (currentlyInParentheses == 0) {//at any parenthetical level except the top, desiredType may be different, may as well parse down there
                        String val = (String) ob.data();
                        Number num;
                        TypeNumerical toUse;
                        if (desiredType.isPresent() && desiredType.get() instanceof TypeNumerical) {
                            if (!(desiredType.get() instanceof TypeBoolean) && !(desiredType.get() instanceof TypePointer)) {
                                toUse = (TypeNumerical) desiredType.get();
                            } else {
                                toUse = new TypeInt32();//something like bool x = 5 < y
                                //TODO infer type of 5 to match in the case of x := 5 < (long)y
                            }
                        } else {
                            //eh, let it be an int
                            toUse = new TypeInt32();
                        }
                        if (val.contains(".")) {
                            //System.out.println("Parsing " + val + " as double: " + toUse);
                            num = Double.parseDouble(val);
                        } else {
                            //System.out.println("Parsing " + val + " as non float: " + toUse);
                            num = Integer.parseInt(val);
                        }
                        o.set(i, new ExpressionConstNum(num, toUse));
                    }
                    break;
                case STRING:
                    o.set(i, new ExpressionConstStr((String) ob.data()));
                    break;
                case CHAR:
                    o.set(i, new ExpressionConstChar((Character) ob.data()));
                    break;
                case VARIABLE:
                    if (i != o.size() - 1 && (o.get(i + 1) == STARTPAREN || o.get(i + 1) == ACCESS)) {
                        //this is a pattern like f(
                        //indicates function call
                        //let's just like not
                        continue;
                    }
                    if (i != 0 && (o.get(i - 1) == PERIOD || o.get(i - 1) == ACCESS)) {
                        //struct field access like a.field1
                        //field1 isn't a real variable with a type on its own
                        //don't turn it to an expressionvariable
                        continue;
                    }
                    String name = (String) ob.data();
                    if (context.getStruct(name) != null) {
                        continue;
                    }
                    Expression ex = new ExpressionVariable(name, context);
                    ex.getType();
                    o.set(i, ex);
                    break;
                case KEYWORD:
                    ExpressionConst ec = ((Keyword) ob).getConstVal();
                    if (ec != null) {
                        o.set(i, (Expression) ec);
                    }
                    break;
                case INCREMENT:
                case DECREMENT:
                    throw new IllegalStateException("No " + ob + " in an expressios, only as a line on its own");
            }
        }
        return false;
    }
}
