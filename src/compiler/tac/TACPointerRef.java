/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.X86Emitter;
import compiler.X86Register;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;

/**
 *
 * @author leijurv
 */
public class TACPointerRef extends TACStatement {
    public final String sourceName;
    public Context.VarInfo source;
    public final String destName;
    public Context.VarInfo dest;
    public TACPointerRef(String source, String dest) {
        this.sourceName = source;
        this.destName = dest;
    }
    @Override
    protected void onContextKnown() {
        source = context.getRequired(sourceName);
        dest = context.getRequired(destName);
        if (!(dest.getType() instanceof TypePointer)) {
            throw new IllegalStateException("what");
        }
        if (!((TypePointer) dest.getType()).pointingTo().equals(source.getType())) {
            throw new IllegalStateException("what");
        }
    }
    @Override
    public String toString0() {
        //return "Put the value " + source + " into the location specified by " + dest;
        return "*" + dest + " = " + source;
    }
    @Override
    public void printx86(X86Emitter emit) {
        TypeNumerical d = (TypeNumerical) source.getType();
        emit.addStatement("mov" + d.x86typesuffix() + " " + source.x86() + ", " + X86Register.C.getRegister(d));
        emit.addStatement("movq " + dest.x86() + ", %rax");
        emit.addStatement("mov" + d.x86typesuffix() + " " + X86Register.C.getRegister(d) + ", (%rax)");
    }
}
