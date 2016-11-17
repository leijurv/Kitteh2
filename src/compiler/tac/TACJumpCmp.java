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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACJumpCmp extends TACJump {
    public VarInfo first;
    public VarInfo second;
    public String firstName;
    public String secondName;
    Operator op;
    public TACJumpCmp(String first, String second, Operator op, int jumpTo) {
        super(jumpTo);
        this.op = op;
        this.firstName = first;
        this.secondName = second;
    }
    @Override
    public String toString0() {
        return "jump to " + jumpTo + " if " + (first == null ? "CONST " + firstName : first) + " " + op + " " + (second == null ? "CONST " + secondName : second);
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(firstName, secondName);
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
        if (first != null && second != null && !first.getType().equals(second.getType())) {
            throw new IllegalStateException("an apple and an orange snuck in");
        }
        if (first == null && second == null) {
            throw new IllegalStateException("hey i need at least either the apple or the orange");
        }
        TypeNumerical type = first == null ? (TypeNumerical) second.getType() : (TypeNumerical) first.getType();
        emit.addStatement("mov" + type.x86typesuffix() + " " + (first == null ? "$" + firstName : first.x86()) + ", " + X86Register.C.getRegister(type));
        emit.addStatement("mov" + type.x86typesuffix() + " " + (second == null ? "$" + secondName : second.x86()) + ", " + X86Register.A.getRegister(type));
        emit.addStatement("cmp" + type.x86typesuffix() + " " + X86Register.A.getRegister(type) + ", " + X86Register.C.getRegister(type));
        emit.addStatement(op.tox86jump() + " " + emit.lineToLabel(jumpTo));
    }
}
