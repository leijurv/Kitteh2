/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.x86.X86Emitter;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACArrayDeref extends TACStatement {
    public TACArrayDeref(String array, String index, String dest) {
        super(array, index, dest);
    }
    @Override
    protected void onContextKnown() {
    }
    @Override
    public String toString0() {
        return params[2] + " = " + params[0] + "[" + params[1] + "]";
    }
    @Override
    public void printx86(X86Emitter emit) {
        //emit.addComment("kancer");
        TypeNumerical pointingTo = (TypeNumerical) ((TypePointer) params[0].getType()).pointingTo();
        X86TypedRegister arr;
        if (params[0] instanceof X86TypedRegister) {
            arr = (X86TypedRegister) params[0];
        } else {
            arr = X86Register.C.getRegister((TypeNumerical) params[0].getType());
            emit.move(params[0], arr);
        }
        X86TypedRegister ind;
        if (params[1] instanceof X86TypedRegister) {
            ind = (X86TypedRegister) params[1];
        } else {
            ind = (arr.getRegister() == X86Register.C ? X86Register.A : X86Register.C).getRegister((TypeNumerical) params[1].getType());
            emit.move(params[1], ind);
        }
        if (ind.getType().getSizeBytes() < 8) {
            X86TypedRegister n = ind.getRegister().getRegister((TypeNumerical) params[0].getType());
            //apparently this cast is unnecesary
            //negative array indicies aren't supported anyway
            //emit.cast(ind, n);
            ind = n;
        }
        X86TypedRegister dest;
        if (params[2] instanceof X86TypedRegister) {
            dest = (X86TypedRegister) params[2];
        } else {
            dest = X86Register.C.getRegister(pointingTo);
        }
        if (arr.getRegister() == ind.getRegister()) {
            throw new IllegalStateException("not okay " + arr + " " + ind + " for " + this);
        }
        if (!dest.getType().equals(pointingTo)) {
            throw new RuntimeException(dest.getType() + " " + pointingTo);
        }
        emit.moveStr("(" + arr.x86() + ", " + ind.x86() + ", " + pointingTo.getSizeBytes() + ")", dest);
        if (dest != params[2]) {
            emit.move(dest, params[2]);
        }
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(paramNames[0], paramNames[1]);
    }
    @Override
    public List<String> modifiedVariables() {
        return Arrays.asList(paramNames[2]);
    }
}
