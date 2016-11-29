/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse;
import compiler.Context;
import compiler.Keyword;
import compiler.Operator;
import compiler.expression.Expression;
import compiler.expression.ExpressionCast;
import compiler.expression.ExpressionConst;
import compiler.expression.ExpressionConstChar;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionConstStr;
import compiler.expression.ExpressionFunctionCall;
import compiler.expression.ExpressionOperator;
import compiler.expression.ExpressionPointerDeref;
import compiler.expression.ExpressionStructFieldAccess;
import compiler.expression.ExpressionVariable;
import compiler.parse.expression.ExpressionParseStep;
import compiler.parse.expression.Not;
import compiler.token.Token;
import static compiler.token.Token.is;
import static compiler.token.TokenType.*;
import compiler.type.Type;
import compiler.type.TypeBoolean;
import compiler.type.TypeInt32;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author leijurv
 */
public class ExpressionParser {
    private static Expression parseImpl(ArrayList<Object> o, Optional<Type> desiredType, Context context) {//the comments are todos, in order that they should be inserted (I got the order from kittehv1, assuming I
        //System.out.println("EXPARSE " + o + " " + desiredType);
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
                    if (i != o.size() - 1 && o.get(i + 1) == STARTPAREN) {
                        //this is a pattern like f(
                        //indicates function call
                        //let's just like not
                        continue;
                    }
                    if (i != 0 && o.get(i - 1) == PERIOD) {
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
        if (o.size() == 1) {
            return (Expression) o.get(0);
        }
        for (int i = 0; i < o.size(); i++) {//recursively call parseImpl on the contents of parentheses
            if (o.get(i) == STARTPAREN) {
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
                    return parseImpl(o, desiredType, context);
                }
                if (inParen.size() == 1 && Util.typeFromObjs(inParen.get(0), context) != null) {
                    //this is a cast, skip the rest and don't modify these parentheses
                    continue;
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
                    return parseImpl(o, desiredType, context);
                }
                if (inParen.size() != 1) {
                    throw new IllegalStateException("This has commas or is empty, but isn't a function call " + inParen);
                }
                o.add(i, parseImpl(inParen.get(0), desiredType, context));
                return parseImpl(o, desiredType, context);
            }
        }
        //inline array definitions a={5,6,7}     TODO: DECIDE TO USE { LIKE C/JAVA OR [ LIKE PYTHON/JAVASCRIPT
        //getting array item (like arr[ind])
        for (int i = 0; i < o.size(); i++) {
            if (o.get(i) == STARTBRAKT) {
                o.remove(i);
                int sq = 1;
                int j = i;
                ArrayList<Object> inBrkts = new ArrayList<>();
                while (j < o.size()) {
                    Object ob = o.remove(j);
                    if (ob == STARTBRAKT) {
                        sq++;
                        continue;
                    }
                    if (ob == ENDBRKT) {
                        sq--;
                        if (sq == 0) {
                            break;
                        }
                        continue;
                    }
                    inBrkts.add(ob);
                }
                if (sq != 0) {
                    throw new IllegalStateException("Mismatch " + o);
                }
                Expression index = parseImpl(inBrkts, Optional.of(new TypeInt32()), context);
                Expression array = (Expression) o.remove(i - 1);
                TypePointer tp = (TypePointer) array.getType();
                Type arrayContents = tp.pointingTo();
                ExpressionConstNum sizeofArrayContents = new ExpressionConstNum(arrayContents.getSizeBytes(), new TypeInt32());
                //so we want...
                //*(array + index * sizeof(arrayContents))
                Expression finalIndex = new ExpressionOperator(index, Operator.MULTIPLY, sizeofArrayContents);
                //*(array+finalIndex)
                Expression ptr = new ExpressionOperator(array, Operator.PLUS, finalIndex);
                //*(ptr)
                Expression element = new ExpressionPointerDeref(ptr);
                o.add(i - 1, element);
                return parseImpl(o, desiredType, context);
            }
            if (o.get(i) == PERIOD) {
                if (!is(o.get(i + 1), VARIABLE)) {
                    throw new RuntimeException();
                }
                String fieldName = (String) ((Token) o.remove(i + 1)).data();
                o.remove(i);
                Expression prev = (Expression) o.remove(i - 1);
                o.add(i - 1, new ExpressionStructFieldAccess(prev, fieldName));
                return parseImpl(o, desiredType, context);
            }
        }
        /*for (int i = 0; i < o.size(); i++) {
         //increment and decrement
         }*/
        //casting
        //casting comes after parentheses: (long)(a)
        //casting comes after array accesses: (long)a[1]
        //casting comes after increments: (long)a++
        //TODO should casting come before or after pointer dereferences?
        for (int i = 0; i < o.size(); i++) {
            if (o.get(i) == STARTPAREN) {
                o.remove(i);
                ArrayList<Token> inBrkts = new ArrayList<>();
                while (i < o.size()) {
                    Object ob = o.remove(i);
                    if (ob == STARTPAREN) {
                        throw new IllegalStateException("Start paren in cast??");
                    }
                    if (ob == ENDPAREN) {
                        break;
                    }
                    inBrkts.add((Token) ob);
                }
                Type type = Util.typeFromTokens(inBrkts, context);
                Expression casting = (Expression) o.remove(i);
                o.add(i, new ExpressionCast(casting, type));
                return parseImpl(o, desiredType, context);
            }
        }
        for (int i = 0; i < o.size(); i++) {
            if (o.get(i) == Operator.MULTIPLY) {
                if (i != 0 && o.get(i - 1) instanceof Expression) {
                    continue;
                }
                Expression point = (Expression) o.remove(i + 1);
                o.set(i, new ExpressionPointerDeref(point));
                return parseImpl(o, desiredType, context);
            }
        }
        for (ExpressionParseStep step : steps) {
            if (step.apply(o, desiredType, context)) {
                return parseImpl(o, desiredType, context);
            }
        }
        for (List<Operator> op : Operator.ORDER) {//order of operations
            for (int i = 0; i < o.size(); i++) {
                if (o.get(i) instanceof Operator && op.contains((Operator) o.get(i))) {
                    if (i == 0 || i == o.size() - 1) {
                        throw new IllegalStateException("Operator on edge. 411 hangs up on you.");
                    }
                    Expression rightSide = (Expression) o.remove(i + 1);
                    Operator tokOp = (Operator) o.remove(i);
                    Expression leftSide = (Expression) o.remove(i - 1);
                    o.add(i - 1, new ExpressionOperator(leftSide, tokOp, rightSide));
                    return parseImpl(o, desiredType, context);
                }
            }
        }
        throw new IllegalStateException("Unable to parse " + o);
    }
    private static final ExpressionParseStep[] steps = {new Not()};
    private static Expression purse(ArrayList<Object> o, Optional<Type> desiredType, Context context) {
        Expression r = parseImpl(o, desiredType, context);
        try {
            r.getType();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Exception while getting type of " + o, e);
        }
        if (desiredType.isPresent() && !desiredType.get().equals(r.getType())) {
            throw new IllegalStateException(o + " should have been type " + desiredType.get() + " but was actually type " + r.getType());
        }
        return r;
    }
    public static Expression parse(List<Token> tokens, Optional<Type> desiredType, Context context) {
        return purse(new ArrayList<>(tokens), desiredType, context);//this both casts each item from Token to Object, as well as cloning the arraylist because we are going to BUTCHER it
    }
}
