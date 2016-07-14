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
public class TACJump extends TACStatement {
    String exp;
    int jumpTo;
    boolean neg;
    public TACJump(String exp, int jumpTo, boolean negated) {
        this.exp = exp;
        this.jumpTo = jumpTo;
        this.neg = negated;
    }
    @Override
    public String toString() {
        return "jump to " + jumpTo + " if " + (neg ? "not " : "") + exp;
    }
}
