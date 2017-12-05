/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.Operator;
import compiler.expression.Expression;
import compiler.expression.ExpressionPointerDeref;
import compiler.type.Type;
import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
class PointerDeref extends TokenBased {
    PointerDeref() {
        <Operator>super(Operator.MULTIPLY);
    }
    @Override
    protected boolean apply(int i, ArrayList<Object> o, Optional<Type> desiredType, Context context) {
        if (i != 0 && o.get(i - 1) instanceof Expression) {//if prev is an expression, that means it could be something simple like x:=y*3 not even a pointer deref at all
            return false;
        }
        Expression point = (Expression) o.remove(i + 1);
        o.set(i, new ExpressionPointerDeref(point));
        return true;
    }
}
