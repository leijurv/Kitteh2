/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.type.Type;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
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
public class TACArrayRef extends TACStatement {
    public TACArrayRef(X86Param array, X86Param index, X86Param source) {
        super(array, index, source);
    }
    @Override
    public String toString() {
        return params[0] + "[" + params[1] + "] = " + params[2];
    }
    @Override
    public boolean usesDRegister() {//I'm sorry. I'm really really sorry.
        X86Emitter emit = new X86Emitter();
        printx86(emit);
        String aoeu = emit.withoutComments();
        for (TypeNumerical tn : TypeNumerical.INTEGER_TYPES) {
            if (aoeu.contains(X86Register.D.getRegister(tn).x86())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void printx86(X86Emitter emit) {
        //emit.addComment("cancer");
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
            //i'm sorry
            ind = (arr.getRegister() == X86Register.C ? (params[2] instanceof X86TypedRegister ? (((X86TypedRegister) params[2]).getRegister() == X86Register.A ? X86Register.D : X86Register.A) : X86Register.A) : X86Register.C).getRegister((TypeNumerical) params[1].getType());
            emit.move(params[1], ind);
        }
        if (ind.getType().getSizeBytes() < 8) {
            X86TypedRegister n = ind.getRegister().getRegister((TypeNumerical) params[0].getType());
            //apparently this cast is unnecesary
            //negative array indicies aren't supported anyway
            //emit.cast(ind, n);
            ind = n;
        }
        X86Param source;
        if (params[2] instanceof X86TypedRegister || params[2] instanceof X86Const) {
            source = params[2];
        } else {
            //i'm sorry
            source = (arr.getRegister() == X86Register.C ? (ind.getRegister() == X86Register.A ? X86Register.D : X86Register.A) : ind.getRegister() == X86Register.C ? X86Register.D : X86Register.C).getRegister(pointingTo);
            emit.move(params[2], source);
        }
        if (arr.getRegister() == ind.getRegister() || (source instanceof X86TypedRegister && (ind.getRegister() == ((X86TypedRegister) source).getRegister() || ((X86TypedRegister) source).getRegister() == arr.getRegister()))) {
            throw new IllegalStateException("not okay " + arr + " " + ind + " " + source + " for " + this);
        }
        if (!source.getType().equals(pointingTo)) {
            throw new RuntimeException(source.getType() + " " + pointingTo);
        }
        String destStr = "(" + arr.x86() + ", " + ind.x86() + ", " + pointingTo.getSizeBytes() + ")";
        X86Param destination = new X86Param() {
            @Override
            public String x86() {
                return destStr;
            }
            @Override
            public Type getType() {
                return source.getType();
            }
        };
        emit.move(source, destination);
    }
    @Override
    public List<X86Param> requiredVariables() {
        return Arrays.asList(params);
    }
    @Override
    public List<X86Param> modifiedVariables() {
        return Arrays.asList();
    }
}
