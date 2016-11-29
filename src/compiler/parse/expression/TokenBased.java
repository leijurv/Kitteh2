/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.token.Token;
import compiler.token.TokenType;
import compiler.type.Type;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

/**
 *
 * @author leijurv
 */
public abstract class TokenBased implements ExpressionParseStep {
    final Predicate<Object> filter;
    TokenBased(TokenType type) {
        filter = o -> o instanceof Token && type.test((Token) o);
    }
    @Override
    public final boolean apply(ArrayList<Object> o, Optional<Type> desiredType, Context context) {
        for (int i = 0; i < o.size(); i++) {
            if (filter.test(o.get(i))) {
                if (apply(i, o, desiredType, context)) {
                    return true;
                }
            }
        }
        return false;
    }
    protected abstract boolean apply(int i, ArrayList<Object> o, Optional<Type> desiredType, Context context);
}
