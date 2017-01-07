/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Operator;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;
import compiler.x86.X86Comparison;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACJumpCmp extends TACJump {
    private final Operator op;
    public TACJumpCmp(String first, String second, Operator op, int jumpTo) {
        super(jumpTo, first, second);
        this.op = op;
    }
    @Override
    public String toString0() {
        return "jump to " + jumpTo + " if " + params[0] + " " + op + " " + params[1];
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(paramNames);
    }
    @Override
    public void onContextKnown() {
        if (!params[0].getType().equals(params[1].getType())) {
            throw new IllegalStateException("apples to oranges " + params[0] + " " + params[1]);
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        X86Param first = params[0];
        X86Param second = params[1];
        if (!first.getType().equals(second.getType())) {
            throw new IllegalStateException("an apple and an orange snuck in");
        }
        TypeNumerical type = (TypeNumerical) first.getType();
        X86TypedRegister noplease = type instanceof TypeFloat ? X86Register.XMM0.getRegister(type) : X86Register.C.getRegister(type);
        if (first instanceof X86TypedRegister) {
            noplease = (X86TypedRegister) first;
        } else {
            emit.addStatement("mov" + type.x86typesuffix() + " " + first.x86() + ", " + noplease.x86());
        }
        String comparison = "cmp" + type.x86typesuffix();
        if (first.getType() instanceof TypeFloat) {
            comparison = "ucomiss";//please, x86, why
        }
        emit.addStatement(comparison + " " + second.x86() + ", " + noplease.x86());
        String jump = X86Comparison.tox86jump(op);
        if (first.getType() instanceof TypeFloat) {
            jump = jump.replace("l", "b").replace("g", "a");//i actually want to die
        }
        emit.addStatement(jump + " " + emit.lineToLabel(jumpTo));
    }
}
