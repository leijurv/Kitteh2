/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.type.*;
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
public class TACPointerDeref extends TACStatement {//TODO (*a).b doesn't need to copy of all of *a onto the stack to get b, it can just add that offset to a then do *(a+offset)
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
            moveStruct(0, "%rax", ((VarInfo) dest).getStackLocation(), "%rbp", ts, emit);
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
                String reg = X86Register.R8.getRegister(tn).x86();
                emit.addStatement("mov" + tn.x86typesuffix() + " " + (i + sourceStackLocation) + "(" + sourceRegister + "), " + reg);
                emit.addStatement("mov" + tn.x86typesuffix() + " " + reg + ", " + (destLocation + i) + "(" + destRegister + ")");
                i += tn.getSizeBytes();
            }
        }
    }
}
