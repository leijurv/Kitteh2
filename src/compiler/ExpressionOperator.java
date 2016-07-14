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
public class ExpressionOperator extends Expression {
    Operator op;
    Expression a;
    Expression b;
    public ExpressionOperator(Expression a, Operator op, Expression b) {
        this.a = a;
        this.b = b;
        this.op = op;
    }
    @Override
    public Type getType() {
        //TODO
        //this'll be hard =/
        //phrasing
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return a.getType();
    }
    public String toString() {
        return "(" + a + ")" + op + "(" + b + ")";
    }
}
