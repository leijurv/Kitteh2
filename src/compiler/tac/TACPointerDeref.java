/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.type.TypeStruct;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACPointerDeref extends TACStatement {
    public TACPointerDeref(String deref, String dest) {
        super(deref, dest);
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
        return params[1] + " = *" + params[0];
    }
    @Override
    public void printx86(X86Emitter emit) {
        X86Param source = params[0];
        X86Param dest = params[1];
        emit.addStatement("movq " + source.x86() + ", %rax");
        if (dest.getType() instanceof TypeNumerical) {
            TypeNumerical d = (TypeNumerical) dest.getType();
            emit.addStatement("mov" + d.x86typesuffix() + " (%rax), " + X86Register.C.getRegister(d));
            emit.addStatement("mov" + d.x86typesuffix() + " " + X86Register.C.getRegister(d) + ", " + dest.x86());
        } else if (dest.getType() instanceof TypeStruct) {
            TypeStruct ts = (TypeStruct) dest.getType();
            moveStruct(0, "%rax", ((VarInfo) dest).getStackLocation(), ts, emit);
        } else {
            throw new InvalidPathException("", "");
        }
    }
    public static void moveStruct(int sourceStackLocation, String sourceRegister, int destLocation, TypeStruct struct, X86Emitter emit) {
        int size = struct.getSizeBytes();
        //this is a really bad way to do this
        for (int i = 0; i + 8 <= size; i += 8) {
            emit.addStatement("movq " + (i + sourceStackLocation) + "(" + sourceRegister + "), %rcx");
            emit.addStatement("movq %rcx, " + (destLocation + i) + "(%rbp)");
        }
        for (int i = size - size % 8; i + 1 <= size; i++) {
            emit.addStatement("movb " + (i + sourceStackLocation) + "(" + sourceRegister + "), %cl");
            emit.addStatement("movb %cl, " + (destLocation + i) + "(%rbp)");
        }
    }
}
