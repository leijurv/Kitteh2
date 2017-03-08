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
import compiler.expression.ExpressionConstBool;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionConstStr;
import compiler.expression.ExpressionVariable;
import compiler.token.Token;
import static compiler.token.TokenType.*;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeFloat;
import compiler.type.TypeInt32;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.x86.X86Format;
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
                case STARTBRAKT:
                    currentlyInParentheses++;
                    break;
                case ENDPAREN:
                case ENDBRKT:
                    currentlyInParentheses--;
                    break;
                case NUM:
                    if (currentlyInParentheses == 0) {//at any parenthetical level except the top, desiredType may be different, may as well parse down there
                        String val = (String) ob.data();
                        Number num;
                        TypeNumerical toUse;
                        if (desiredType.isPresent() && desiredType.get() instanceof TypeNumerical) {
                            if (!(desiredType.get() instanceof TypeBoolean) && !(desiredType.get() instanceof TypePointer) && !(desiredType.get() instanceof TypeFloat)) {
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
                    o.set(i, new ExpressionConstNum(0 + (Character) ob.data(), new TypeInt8()));
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
                    if ("MACOSX".equals(name)) {
                        o.set(i, new ExpressionConstBool(X86Format.MAC));
                        break;
                    }
                    Expression ex = new ExpressionVariable(name, context);
                    ex.getType();
                    o.set(i, ex);
                    break;
                case KEYWORD:
                    ExpressionConst ec = ((Keyword) ob).getConstVal();
                    if (ec != null) {
                        if (!(ec instanceof Expression)) {
                            throw new IllegalStateException();
                        }
                        o.set(i, ec);
                    }
                    break;
                case INCREMENT:
                case DECREMENT:
                    throw new IllegalStateException("No " + ob + " in an expressios, only as a line on its own");
                default:
                    break;
            }
        }
        return false;
    }
}
