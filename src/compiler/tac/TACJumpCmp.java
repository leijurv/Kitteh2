/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.Operator;
import compiler.X86Emitter;
import compiler.X86Register;
import compiler.type.TypeNumerical;

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
        first = context.getRequired(firstName);
        second = context.getRequired(secondName);
        if (!first.getType().equals(second.getType())) {
            throw new IllegalStateException("apples to oranges " + first + " " + second);
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        TypeNumerical type = (TypeNumerical) first.getType();
        emit.addStatement("mov" + type.x86typesuffix() + " " + first.x86() + ", " + X86Register.C.getRegister(type));
        emit.addStatement("mov" + type.x86typesuffix() + " " + second.x86() + ", " + X86Register.A.getRegister(type));
        emit.addStatement("cmp" + type.x86typesuffix() + " " + X86Register.A.getRegister(type) + ", " + X86Register.C.getRegister(type));
        Operator o = neg ? op.invert() : op;
        emit.addStatement(o.tox86() + " " + emit.lineToLabel(jumpTo));
    }
}
