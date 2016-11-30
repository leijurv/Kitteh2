/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.expression.ExpressionConditionalJumpable;
import compiler.expression.ExpressionInvert;
import compiler.token.TokenType;
import compiler.type.Type;
import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
class Not extends TokenBased {
    public Not() {
        super(TokenType.NOT);
    }
    @Override
    protected boolean apply(int i, ArrayList<Object> o, Optional<Type> desiredType, Context context) {
        o.set(i, new ExpressionInvert((ExpressionConditionalJumpable) o.remove(i + 1)));
        return true;
    }
}
