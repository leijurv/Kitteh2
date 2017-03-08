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
import compiler.x86.X86Const;
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
    public TACJumpCmp(X86Param first, X86Param second, Operator op, int jumpTo) {
        super(jumpTo, first, second);
        this.op = op;
        if (!params[0].getType().equals(params[1].getType())) {
            throw new IllegalStateException("apples to oranges " + params[0] + " " + params[1]);
        }
    }
    @Override
    public String toString() {
        return "jump to " + jumpTo + " if " + params[0] + " " + op + " " + params[1];
    }
    @Override
    public List<X86Param> requiredVariables() {
        return Arrays.asList(params);
    }
    @Override
    public void printx86(X86Emitter emit) {
        X86Param first = params[0];
        X86Param second = params[1];
        if (!first.getType().equals(second.getType())) {
            throw new IllegalStateException("an apple and an orange snuck in" + this);
        }
        Operator o = createCompare(first, second, op, emit);
        String jump = X86Comparison.tox86jump(o);
        if (first.getType() instanceof TypeFloat) {
            jump = jump.replace("l", "b").replace("g", "a");//i actually want to die
        }
        emit.addStatement(jump + " " + emit.lineToLabel(jumpTo));
    }
    public static Operator createCompare(X86Param first0, X86Param second0, Operator op, X86Emitter emit) {
        X86Param first = first0;
        X86Param second = second0;
        X86Param secondAlt = emit.alternative(second, false);
        if (secondAlt != null) {
            second = secondAlt;
        }
        X86Param firstAlt = emit.alternative(first, false);
        if (firstAlt != null) {
            first = firstAlt;
        }
        Operator o = op;
        if (first instanceof X86Const) {
            X86Param tmp = first;
            first = second;
            second = tmp;
            o = o.swap();
        }
        TypeNumerical type = (TypeNumerical) first.getType();
        X86Param fst = type instanceof TypeFloat ? X86Register.XMM0.getRegister(type) : X86Register.C.getRegister(type);
        if (first instanceof X86TypedRegister || second instanceof X86Const || first instanceof X86Const) {
            fst = first;
        } else {
            emit.move(first, fst);
        }
        String comparison = "cmp" + type.x86typesuffix();
        if (first.getType() instanceof TypeFloat) {
            comparison = "ucomiss";//please, x86, why
        }
        emit.addStatement(comparison + " " + second.x86() + ", " + fst.x86());
        return o;
    }
}
