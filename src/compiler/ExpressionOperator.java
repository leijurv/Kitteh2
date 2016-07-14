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
    public Type calcType() {
        //TODO
        //this'll be hard =/
        //phrasing
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Type A = a.getType();
        Type B = b.getType();
        Type result = op.onApplication(A, B);
        System.out.println("Getting type of " + A + " " + op + " " + B + ": " + result);
        return result;
    }
    @Override
    public String toString() {
        return "(" + a + ")" + op + "(" + b + ")";
    }
    public static void TAC(ExpressionOperator op) {
        //TODO recursively generate TAC
        //TODO if we allow ++ and -- that could mess up the DAG
    }
}
