/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.type.Type;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
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
public class TACArrayDeref extends TACStatement {
    public TACArrayDeref(X86Param array, X86Param index, X86Param dest) {
        super(array, index, dest);
    }
    @Override
    public String toString() {
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
            throw new IllegalStateException(dest.getType() + " " + pointingTo);
        }
        String sourceStr = "(" + arr.x86() + ", " + ind.x86() + ", " + pointingTo.getSizeBytes() + ")";
        X86Param source = new X86Param() {
            @Override
            public String x86() {
                return sourceStr;
            }
            @Override
            public Type getType() {
                return dest.getType();
            }
        };
        emit.move(source, dest);
        if (dest != params[2]) {
            emit.move(dest, params[2]);
        }
    }
    @Override
    public List<X86Param> requiredVariables() {
        return Arrays.asList(params[0], params[1]);
    }
    @Override
    public List<X86Param> modifiedVariables() {
        return Arrays.asList(params[2]);
    }
}
