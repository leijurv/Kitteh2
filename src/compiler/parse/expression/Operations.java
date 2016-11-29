/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.Operator;
import compiler.expression.Expression;
import compiler.expression.ExpressionOperator;
import compiler.type.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
public class Operations implements ExpressionParseStep {
    @Override
    public boolean apply(ArrayList<Object> o, Optional<Type> desiredType, Context context) {
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
                    return true;
                }
            }
        }
        return false;
    }
}
