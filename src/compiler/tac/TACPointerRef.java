/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.x86.X86Emitter;
import compiler.x86.X86Register;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACPointerRef extends TACStatement {
    public String sourceName;
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
    public List<String> requiredVariables() {
        return Arrays.asList(sourceName, destName);
    }
    @Override
    public List<String> modifiedVariables() {
        return Arrays.asList();
    }
    @Override
    public String toString0() {
        //return "Put the value " + source + " into the location specified by " + dest;
        return "*" + dest + " = " + (source == null ? "CONST " + sourceName : source);
    }
    @Override
    public void printx86(X86Emitter emit) {
        TypeNumerical d = (TypeNumerical) ((TypePointer) dest.getType()).pointingTo();
        emit.addStatement("mov" + d.x86typesuffix() + " " + (source == null ? "$" + sourceName : source.x86()) + ", " + X86Register.C.getRegister(d));
        emit.addStatement("movq " + dest.x86() + ", %rax");
        emit.addStatement("mov" + d.x86typesuffix() + " " + X86Register.C.getRegister(d) + ", (%rax)");
    }
}
