/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 * TODO merge with TokenNum
 *
 * @author leijurv
 */
public class ExpressionNum extends Expression {
    TypeNumerical type;
    Number val;
    public ExpressionNum(TypeNumerical type, Number val) {
        this.type = type;
        this.val = val;
    }
    public ExpressionNum(Number val) {
        this(new TypeInt32(), val);
    }
    @Override
    public Type calcType() {
        return type;
    }
}
