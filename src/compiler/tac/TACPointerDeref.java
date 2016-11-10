/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.X86Emitter;
import compiler.X86Register;
import compiler.type.TypeNumerical;

/**
 *
 * @author leijurv
 */
public class TACPointerDeref extends TACStatement {
    public final String sourceName;
    public VarInfo source;
    public final String destName;
    public VarInfo dest;
    public TACPointerDeref(String deref, String dest) {
        this.sourceName = deref;
        this.destName = dest;
    }
    @Override
    protected void onContextKnown() {
        source = context.getRequired(sourceName);
        dest = context.getRequired(destName);
    }
    @Override
    public String toString0() {
        return "Dereference " + source + " into " + dest;
    }
    @Override
    public void printx86(X86Emitter emit) {
        emit.addStatement("movq " + source.x86() + ", %rax");
        TypeNumerical d = (TypeNumerical) dest.getType();
        emit.addStatement("mov" + d.x86typesuffix() + " (%rax), " + X86Register.B.getRegister(d));
        emit.addStatement("mov" + d.x86typesuffix() + " " + X86Register.B.getRegister(d) + ", " + dest.x86());
    }
}
