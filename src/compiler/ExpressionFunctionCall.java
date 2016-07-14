/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class ExpressionFunctionCall extends Expression {
    String funcName;
    ArrayList<Expression> args;
    public ExpressionFunctionCall(String funcName, ArrayList<Expression> args) {
        this.funcName = funcName;
        this.args = args;
    }
    @Override
    public Type getType() {
        return new TypeVoid();
    }
    public String toString() {
        return funcName + args;
    }
}
