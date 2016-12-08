/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.expression.Expression;
import compiler.expression.ExpressionCast;
import compiler.util.Parse;
import compiler.token.Token;
import static compiler.token.TokenType.*;
import compiler.type.Type;
import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
class Casting extends TokenBased {
    public Casting() {
        super(STARTPAREN);
    }
    @Override
    protected boolean apply(int i, ArrayList<Object> o, Optional<Type> desiredType, Context context) {
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
        Type type = Parse.typeFromTokens(inBrkts, context);
        Expression casting = (Expression) o.remove(i);
        o.add(i, new ExpressionCast(casting, type));
        return true;
    }
}
