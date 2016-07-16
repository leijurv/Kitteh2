/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.Operator;

/**
 *
 * @author leijurv
 */
public class TACJumpCmp extends TACJump {
    VarInfo first;
    VarInfo second;
    String firstName;
    String secondName;
    Operator op;
    boolean neg;
    public TACJumpCmp(String first, String second, Operator op, int jumpTo, boolean negated) {
        super(jumpTo);
        this.op = op;
        this.firstName = first;
        this.secondName = second;
        this.neg = negated;
    }
    @Override
    public String toString0() {
        return "jump to " + jumpTo + " if " + (neg ? "not " : "") + first + " " + op + " " + second;
    }
    @Override
    public void onContextKnown() {
        first = context.get(firstName);
        second = context.get(secondName);
    }
}
