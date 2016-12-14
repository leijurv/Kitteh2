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
import compiler.x86.X86Emitter;
import compiler.x86.X86Register;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACPointerRef extends TACStatement {
    public TACPointerRef(String source, String dest) {
        super(source, dest);
    }
    @Override
    protected void onContextKnown() {
        if (!(params[1].getType() instanceof TypePointer)) {
            throw new IllegalStateException("what");
        }
        if (!((TypePointer) params[1].getType()).pointingTo().equals(params[0].getType())) {
            throw new IllegalStateException("what");
        }
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(paramNames);
    }
    @Override
    public List<String> modifiedVariables() {
        return Arrays.asList();
    }
    @Override
    public String toString0() {
        //return "Put the value " + source + " into the location specified by " + dest;
        return "*" + params[1] + " = " + params[0];
    }
    @Override
    public void printx86(X86Emitter emit) {
        if (params[0].getType() instanceof TypeNumerical) {
            TypeNumerical d = (TypeNumerical) ((TypePointer) params[1].getType()).pointingTo();
            emit.addStatement("mov" + d.x86typesuffix() + " " + params[0].x86() + ", " + X86Register.C.getRegister(d));
            emit.addStatement("movq " + params[1].x86() + ", %rax");
            emit.addStatement("mov" + d.x86typesuffix() + " " + X86Register.C.getRegister(d) + ", (%rax)");
        } else if (params[0].getType() instanceof TypeStruct) {
            TypeStruct ts = (TypeStruct) params[0].getType();
            emit.addStatement("movq " + params[1].x86() + ", %rax");
            TACPointerDeref.moveStruct(((Context.VarInfo) params[0]).getStackLocation(), "%rbp", 0, "%rax", ts, emit);
        } else {
            throw new InvalidPathException("", "");
        }
    }
}
