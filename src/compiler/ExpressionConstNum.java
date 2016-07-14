/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 *
 * @author leijurv
 */
public class ExpressionConstNum extends Expression {
    Number val;
    Type type;
    public ExpressionConstNum(Number val, Type type) {
        this.val = val;
        this.type = type;
    }
    @Override
    public Type getType() {
        return type;
    }
}
