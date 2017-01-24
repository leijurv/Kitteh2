/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.x86.X86Emitter;
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
public class TACPointerDeref extends TACStatement {
    int offset;
    public TACPointerDeref(String deref, String dest, int offset) {
        super(deref, dest);
        this.offset = offset;
    }
    @Override
    protected void onContextKnown() {
        if (!((TypePointer) params[0].getType()).pointingTo().equals(params[1].getType())) {
            throw new RuntimeException();
        }
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(paramNames[0]);
    }
    @Override
    public List<String> modifiedVariables() {
        return Arrays.asList(paramNames[1]);
    }
    @Override
    public String toString0() {
        //return "Dereference " + source + " into " + dest;
        return params[1] + " = *" + params[0] + (offset == 0 ? "" : "+" + offset);
    }
    @Override
    public void printx86(X86Emitter emit) {
        X86Param source = params[0];
        X86Param dest = params[1];
        X86Param loc;
        if (source instanceof X86TypedRegister) {
            loc = source;
        } else {
            loc = X86Register.A.getRegister((TypeNumerical) source.getType());
            emit.move(source, loc);
        }
        String off = offset == 0 ? "" : offset + "";
        if (dest.getType() instanceof TypeNumerical) {
            TypeNumerical d = (TypeNumerical) dest.getType();
            if (dest instanceof X86TypedRegister) {
                emit.moveStr(off + "(" + loc.x86() + ")", dest);
            } else {
                emit.moveStr(off + "(" + loc.x86() + ")", X86Register.C.getRegister(d));
                emit.move(X86Register.C, dest);
            }
        } else if (dest.getType() instanceof TypeStruct) {
            TypeStruct ts = (TypeStruct) dest.getType();
            moveStruct(offset, loc.x86(), ((VarInfo) dest).getStackLocation(), "%rbp", ts, emit);
        } else {
            throw new InvalidPathException("", "");
        }
    }
    public static void moveStruct(int sourceStackLocation, String sourceRegister, int destLocation, String destRegister, TypeStruct struct, X86Emitter emit) {
        int size = struct.getSizeBytes();
        //this is a really bad way to do this
        //still.
        //even though its a little smarter now
        int i = 0;
        for (TypeNumerical tn : new TypeNumerical[]{new TypeInt64(), new TypeInt32(), new TypeInt16(), new TypeInt8()}) {
            while (i + tn.getSizeBytes() <= size) {
                X86TypedRegister reg = X86Register.C.getRegister(tn);
                emit.moveStr((i + sourceStackLocation) + "(" + sourceRegister + ")", reg);
                emit.moveStr(reg, (destLocation + i) + "(" + destRegister + ")");
                i += tn.getSizeBytes();
            }
        }
    }
}
