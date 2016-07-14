/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
public class ExpressionParser {
    private static Expression parseImpl(ArrayList<Object> o, Optional<Type> desiredType, Context context) {//the comments are todos, in order that they should be inserted (I got the order from kittehv1, assuming I
        for (int i = 0; i < o.size(); i++) {
            Object ob = o.get(i);
            if (ob instanceof TokenNum) {
                Number num = Double.parseDouble(((TokenNum) ob).val);//anything can be a double. even an int.
                o.set(i, new ExpressionConstNum(num, desiredType.isPresent() ? desiredType.get() : new TypeInt32()));//TODO this is shit
            }
            if (ob instanceof TokenVariable) {
                String name = ((TokenVariable) ob).val;
                Expression ex = new ExpressionVariable(name, context);
                Type type = ex.getType();
                if (type == null) {
                    throw new IllegalStateException("Trying to use " + name + " before it's defined");
                }
                o.set(i, ex);
            }
        }
        if (o.size() == 1) {
            if (o.get(0) instanceof Token) {
                throw new IllegalStateException();
            }
            return (Expression) o.get(0);
        }
        //variable definitions / settings (=, :=)
        //recursively call parseImpl on the contents of parentheses
        //inline array definitions a={5,6,7}     TODO: DECIDE TO USE { LIKE C/JAVA OR [ LIKE PYTHON/JAVASCRIPT
        //only three items in o; assume the middle one must be an operator
        //function calls (a TokenVariable then an Expression / ArrayList<Expression>)
        //getting array item (like arr[ind])
        //
        for (Operator op : Operator.ORDER) {
            for (int i = 0; i < o.size(); i++) {
                if (o.get(i) instanceof TokenOperator && ((TokenOperator) o.get(i)).op.equals(op)) {
                }
            }
        }
        return null;
    }
    public static Expression parse(List<Token> tokens, Optional<Type> desiredType, Context context) {
        return parseImpl(new ArrayList<>(tokens), desiredType, context);//this both casts each item from Token to Object, as well as cloning the arraylist because we are going to BUTCHER it
    }
    public static Expression parse(ArrayList<Token> tokens, Optional<Type> desiredType, Context context) {
        return parseImpl(new ArrayList<>(tokens), desiredType, context);//this both casts each item from Token to Object, as well as cloning the arraylist because we are going to BUTCHER it
    }
}
