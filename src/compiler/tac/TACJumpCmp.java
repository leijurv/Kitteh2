/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Operator;
import compiler.type.TypeNumerical;
import compiler.x86.X86Const;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACJumpCmp extends TACJump {
    Operator op;
    public TACJumpCmp(String first, String second, Operator op, int jumpTo) {
        super(jumpTo, first, second);
        this.op = op;
    }
    @Override
    public String toString0() {
        return "jump to " + jumpTo + " if " + (params[0] == null ? "CONST " + paramNames[0] : params[0]) + " " + op + " " + (params[1] == null ? "CONST " + paramNames[1] : params[1]);
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
        if (first != null && second != null && !first.getType().equals(second.getType())) {
            throw new IllegalStateException("an apple and an orange snuck in");
        }
        if (first == null && second == null) {
            throw new IllegalStateException("hey i need at least either the apple or the orange");
        }
        TypeNumerical type = first == null ? (TypeNumerical) second.getType() : (TypeNumerical) first.getType();
        if (first == null) {
            first = new X86Const(paramNames[0], type);
        }
        if (second == null) {
            second = new X86Const(paramNames[1], type);
        }
        emit.addStatement("mov" + type.x86typesuffix() + " " + first.x86() + ", " + X86Register.C.getRegister(type));
        emit.addStatement("mov" + type.x86typesuffix() + " " + second.x86() + ", " + X86Register.A.getRegister(type));
        emit.addStatement("cmp" + type.x86typesuffix() + " " + X86Register.A.getRegister(type) + ", " + X86Register.C.getRegister(type));
        emit.addStatement(op.tox86jump() + " " + emit.lineToLabel(jumpTo));
    }
}
