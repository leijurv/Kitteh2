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
import compiler.expression.ExpressionConditionalJumpable;
import compiler.expression.ExpressionConst;
import compiler.expression.ExpressionConstChar;
import compiler.expression.ExpressionConstNum;
import compiler.expression.ExpressionConstStr;
import compiler.expression.ExpressionFunctionCall;
import compiler.expression.ExpressionInvert;
import compiler.expression.ExpressionOperator;
import compiler.expression.ExpressionPointerDeref;
import compiler.expression.ExpressionStructFieldAccess;
import compiler.expression.ExpressionVariable;
import compiler.token.Token;
import compiler.token.TokenType;
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
    public static boolean is(Object o, TokenType type) {
        return o instanceof Token && ((Token) o).tokenType == type;
    }
    private static Expression parseImpl(ArrayList<Object> o, Optional<Type> desiredType, Context context) {//the comments are todos, in order that they should be inserted (I got the order from kittehv1, assuming I
        //System.out.println("EXPARSE " + o + " " + desiredType);
        int currentlyInParentheses = 0;
        for (int i = 0; i < o.size(); i++) {
            if (!(o.get(i) instanceof Token)) {
                continue;
            }
            Token ob = (Token) o.get(i);
            switch (ob.tokenType) {
                case STARTPAREN:
                    currentlyInParentheses++;
                    break;
                case ENDPAREN:
                    currentlyInParentheses--;
                    break;
                case NUM:
                    if (currentlyInParentheses == 0) {//at any parenthetical level except the top, desiredType may be different, may as well parse down there
                        String val = (String) ob.data;
                        Number num;
                        TypeNumerical toUse = (desiredType.isPresent() && desiredType.get() instanceof TypeNumerical && !(desiredType.get() instanceof TypeBoolean) && !(desiredType.get() instanceof TypePointer)) ? (TypeNumerical) desiredType.get() : new TypeInt32();
                        if (val.contains(".")) {
                            //System.out.println("Parsing " + val + " as double: " + toUse);
                            num = Double.parseDouble(val);
                        } else {
                            //System.out.println("Parsing " + val + " as non float: " + toUse);
                            num = Integer.parseInt(val);
                        }
                        o.set(i, new ExpressionConstNum(num, toUse));//TODO this is mal
                    }
                    break;
                case STRING:
                    o.set(i, new ExpressionConstStr((String) ob.data));
                    break;
                case CHAR:
                    o.set(i, new ExpressionConstChar((Character) ob.data));
                    break;
                case VARIABLE:
                    if (i != o.size() - 1 && is(o.get(i - 1), TokenType.PERIOD)) {
                        //this is a pattern like f(
                        //indicates function call
                        //let's just like not
                        continue;
                    }
                    if (i != 0 && is(o.get(i - 1), TokenType.PERIOD)) {
                        //struct field access like a.field1
                        //field1 isn't a real variable with a type on its own
                        //don't turn it to an expressionvariable
                        continue;
                    }
                    String name = (String) ob.data;
                    if (context.getStruct(name) != null) {
                        continue;
                    }
                    Expression ex = new ExpressionVariable(name, context);
                    ex.getType();
                    o.set(i, ex);
                    break;
                case KEYWORD:
                    ExpressionConst ec = ((Keyword) ob.data).getConstVal();
                    if (ec != null) {
                        o.set(i, (Expression) ec);
                    }
                    break;
            }
        }
        if (o.size() == 1) {
            return (Expression) o.get(0);
        }
        for (int i = 0; i < o.size(); i++) {//recursively call parseImpl on the contents of parentheses
            if (o.get(i) instanceof TokenStartParen) {
                ArrayList<ArrayList<Object>> inParen = new ArrayList<>();
                ArrayList<Object> temp = new ArrayList<>();
                int numParens = 1;
                ArrayList<Object> copy = new ArrayList<>(o);
                int numToRemoveAti = 1;
                copy.remove(i);
                while (i < copy.size()) {
                    Object b = copy.remove(i);
                    numToRemoveAti++;
                    if (b instanceof TokenEndParen) {
                        numParens--;
                        if (numParens == 0) {
                            if (!temp.isEmpty()) {
                                inParen.add(temp);
                            }
                            break;
                        }
                    }
                    if (b instanceof TokenComma && numParens == 1) {
                        inParen.add(temp);
                        temp = new ArrayList<>();
                    } else {
                        temp.add(b);
                    }
                    if (b instanceof TokenStartParen) {
                        numParens++;
                    }
                }
                if (numParens != 0) {
                    throw new IllegalStateException("mismatched ( and )");
                }
                if (i != 0 && o.get(i - 1) instanceof TokenKeyword) {
                    TokenKeyword tk = ((TokenKeyword) o.get(i - 1));
                    if (tk.getKeyword().toString().equals(Keyword.SIZEOF.toString())) {
                        if (inParen.size() != 1) {
                            throw new RuntimeException();
                        }
                        Type type = typeFromObjs(inParen.get(0), context);
                        if (type == null) {
                            throw new RuntimeException();
                        }
                        for (int j = 0; j < numToRemoveAti; j++) {
                            o.remove(i);
                        }
                        o.set(i - 1, new ExpressionConstNum(type.getSizeBytes(), new TypeInt32()));
                        return parseImpl(o, desiredType, context);
                    }
                }
                if (inParen.size() == 1 && typeFromObjs(inParen.get(0), context) != null) {
                    //this is a cast, skip the rest and don't modify these parentheses
                    continue;
                } else {
                    //not a cast
                    for (int j = 0; j < numToRemoveAti; j++) {
                        o.remove(i);
                    }
                }
                //System.out.println("Doing replace " + o + " " + inParen);
                if (i != 0 && (o.get(i - 1) instanceof TokenVariable || o.get(i - 1) instanceof TokenKeyword)) {
                    String funcName;
                    if (o.get(i - 1) instanceof TokenVariable) {
                        funcName = ((TokenVariable) o.get(i - 1)).val;
                    } else {
                        TokenKeyword tk = ((TokenKeyword) o.get(i - 1));
                        funcName = tk.toString();//some functions that you call are also keywords
                    }
                    ArrayList<Type> desiredTypes = context.gc.getHeader(funcName).inputs();
                    //System.out.println("Expecting inputs: " + desiredTypes);
                    //tfw parallel expression parsing
                    //tfw this is a GOOD idea /s
                    if (inParen.size() != desiredTypes.size()) {
                        throw new SecurityException("mismatched arg count");
                    }
                    ArrayList<Expression> args = IntStream.range(0, inParen.size()).parallel().mapToObj(p -> parseImpl(inParen.get(p), Optional.of(desiredTypes.get(p)), context)).collect(Collectors.toCollection(ArrayList::new));
                    o.set(i - 1, new ExpressionFunctionCall(context, funcName, args));
                    return parseImpl(o, desiredType, context);
                }
                if (inParen.size() != 1) {
                    throw new IllegalStateException("This has commas or is empty, but isn't a function call " + inParen);
                }
                o.add(i, parseImpl(inParen.get(0), Optional.empty(), context));
                return parseImpl(o, desiredType, context);
            }
        }
        //inline array definitions a={5,6,7}     TODO: DECIDE TO USE { LIKE C/JAVA OR [ LIKE PYTHON/JAVASCRIPT
        //getting array item (like arr[ind])
        for (int i = 0; i < o.size(); i++) {
            if (o.get(i) instanceof TokenStartBrakt) {
                o.remove(i);
                int sq = 1;
                int j = i;
                ArrayList<Object> inBrkts = new ArrayList<>();
                while (j < o.size()) {
                    Object ob = o.remove(j);
                    if (ob instanceof TokenStartBrakt) {
                        sq++;
                        continue;
                    }
                    if (ob instanceof TokenEndBrkt) {
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
            if (o.get(i) instanceof TokenPeriod) {
                TokenVariable next = (TokenVariable) o.remove(i + 1);
                String fieldName = next.val;
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
            if (o.get(i) instanceof TokenStartParen) {
                o.remove(i);
                ArrayList<Token> inBrkts = new ArrayList<>();
                while (i < o.size()) {
                    Object ob = o.remove(i);
                    if (ob instanceof TokenStartParen) {
                        throw new IllegalStateException("Start paren in cast??");
                    }
                    if (ob instanceof TokenEndParen) {
                        break;
                    }
                    inBrkts.add((Token) ob);
                }
                Type type = Parser.typeFromTokens(inBrkts, context);
                Expression casting = (Expression) o.remove(i);
                o.add(i, new ExpressionCast(casting, type));
                return parseImpl(o, desiredType, context);
            }
        }
        for (int i = 0; i < o.size(); i++) {
            if (o.get(i) instanceof TokenOperator && ((TokenOperator) o.get(i)).op == Operator.MULTIPLY) {
                if (i != 0) {
                    if (o.get(i - 1) instanceof Expression) {
                        continue;
                    }
                }
                Expression point = (Expression) o.remove(i + 1);
                o.set(i, new ExpressionPointerDeref(point));
                return parseImpl(o, desiredType, context);
            }
        }
        for (int i = 0; i < o.size(); i++) {
            if (o.get(i) instanceof TokenNot) {
                o.set(i, new ExpressionInvert((ExpressionConditionalJumpable) o.remove(i + 1)));
                return parseImpl(o, desiredType, context);
            }
        }
        for (List<Operator> op : Operator.ORDER) {//order of operations
            for (int i = 0; i < o.size(); i++) {
                if (o.get(i) instanceof TokenOperator && op.contains(((TokenOperator) o.get(i)).op)) {
                    if (i == 0 || i == o.size() - 1) {
                        throw new IllegalStateException("Operator on edge. 411 hangs up on you.");
                    }
                    Expression rightSide = (Expression) o.remove(i + 1);
                    TokenOperator tokOp = (TokenOperator) o.remove(i);
                    Expression leftSide = (Expression) o.remove(i - 1);
                    o.add(i - 1, new ExpressionOperator(leftSide, tokOp.op, rightSide));
                    return parseImpl(o, desiredType, context);
                }
            }
        }
        throw new IllegalStateException("Unable to parse " + o);
    }
    public static Type typeFromObjs(ArrayList<Object> o, Context context) {
        ArrayList<Token> tmp = new ArrayList<>(o.size());
        for (Object obj : o) {
            if (!(obj instanceof Token)) {
                return null;
            }
            tmp.add((Token) obj);
        }
        return Parser.typeFromTokens(tmp, context);
    }
    private static Expression purse(ArrayList<Object> o, Optional<Type> desiredType, Context context) {
        Expression r = parseImpl(o, desiredType, context);
        try {
            r.getType();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Exception while getting type of " + o, e);
        }
        if (desiredType.isPresent()) {
            if (!desiredType.get().equals(r.getType())) {
                throw new IllegalStateException(o + " should have been type " + desiredType.get() + " but was actually type " + r.getType());
            }
        }
        return r;
    }
    public static Expression parse(List<Token> tokens, Optional<Type> desiredType, Context context) {
        return purse(new ArrayList<>(tokens), desiredType, context);//this both casts each item from Token to Object, as well as cloning the arraylist because we are going to BUTCHER it
    }
}
