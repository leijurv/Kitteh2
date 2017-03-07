/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.x86.X86Const;
import compiler.x86.X86Emitter;
import compiler.x86.X86Memory;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACPointerRef extends TACStatement {
    int offset;
    public TACPointerRef(X86Param source, X86Param dest, int offset) {
        super(source, dest);
        this.offset = offset;
        if (!(params[1].getType() instanceof TypePointer)) {
            throw new IllegalStateException("what");
        }
        if (!((TypePointer) params[1].getType()).pointingTo().equals(params[0].getType())) {
            throw new IllegalStateException("what");
        }
    }
    @Override
    public List<X86Param> requiredVariables() {
        return Arrays.asList(params);
    }
    @Override
    public List<X86Param> modifiedVariables() {
        return Arrays.asList();
    }
    @Override
    public String toString() {
        //return "Put the value " + source + " into the location specified by " + dest;
        return "*" + params[1] + (offset == 0 ? "" : "+" + offset) + " = " + params[0];
    }
    @Override
    public void printx86(X86Emitter emit) {
        if (params[0].getType() instanceof TypeNumerical) {
            X86Param source = emit.alternative(params[0], (TypeNumerical) params[0].getType(), false);
            if (source == null) {
                if (params[0] instanceof X86Const) {
                    source = params[0];
                } else {
                    source = emit.putInRegister(params[0], (TypeNumerical) params[0].getType(), X86Register.C);
                }
            }
            X86Register ohno = X86Register.A;
            if (source instanceof X86TypedRegister && ((X86TypedRegister) source).getRegister() == X86Register.A) {
                ohno = X86Register.C;
            }
            X86TypedRegister othersource = emit.putInRegister(params[1], (TypeNumerical) params[1].getType(), ohno);
            X86Memory dest = new X86Memory(offset, othersource.getRegister(), params[0].getType());
            emit.uncheckedMove(source, dest);
        } else if (params[0].getType() instanceof TypeStruct) {
            TypeStruct ts = (TypeStruct) params[0].getType();
            emit.move(params[1], X86Register.C);
            TACPointerDeref.moveStruct(((Context.VarInfo) params[0]).getStackLocation(), X86Register.BP, offset, X86Register.C, ts, emit);
            throw new RuntimeException("This actually just doesn't work");
        } else {
            throw new InvalidPathException("", "");
        }
    }
}
