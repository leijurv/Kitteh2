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
public class TokenOperator extends Token {
    Operator op;
    public TokenOperator(Operator op) {
        this.op = op;
    }
    @Override
    public String toString() {
        return op.toString();
    }
}
