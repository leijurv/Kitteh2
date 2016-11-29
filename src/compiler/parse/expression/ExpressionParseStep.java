/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.parse.expression;
import compiler.Context;
import compiler.type.Type;
import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author leijurv
 */
public interface ExpressionParseStep {
    boolean apply(ArrayList<Object> o, Optional<Type> desiredType, Context context);
}
