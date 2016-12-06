/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.expression.Expression;
import compiler.token.Token;
import compiler.type.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
public class ExpressionParser {
    static Expression parseImpl(ArrayList<Object> o, Optional<Type> desiredType, Context context) {//TODO replace recursion with loop
        new FirstPass().apply(o, desiredType, context);
        if (o.size() == 1) {
            return (Expression) o.get(0);
        }
        //inline array definitions a={5,6,7}     TODO: DECIDE TO USE { LIKE C/JAVA OR [ LIKE PYTHON/JAVASCRIPT
        for (ExpressionParseStep step : steps) {
            if (step.apply(o, desiredType, context)) {
                return parseImpl(o, desiredType, context);
            }
        }
        throw new IllegalStateException("Unable to parse " + o);
    }
    private static final ExpressionParseStep[] steps = {
        //recursively call parseImpl on the contents of parentheses
        new RecursiveParentheses(),
        //getting array item (like arr[ind])
        //and also struct fields
        //like a.b[0].c.d[3] would be parsed left to right with . and [ having the same precedence
        new StructFieldsAndArrays(),
        //casting
        //casting comes after parentheses: (long)(a)
        //casting comes after array accesses: (long)a[1]
        //casting comes after increments: (long)a++
        //TODO should casting come before or after pointer dereferences?
        new Casting(),
        new PointerDeref(),
        //! comes after pointer deref. !*boolArray should work I guess
        //definitely comes after struct fields and arrays, !boolArray[0] should work
        new Not(),
        //finally, the operators, in order of operations
        new Operations()
    };
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
