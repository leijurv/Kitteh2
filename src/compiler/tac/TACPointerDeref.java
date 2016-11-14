/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.Struct;
import compiler.X86Emitter;
import compiler.X86Register;
import compiler.type.TypeNumerical;
import compiler.type.TypeStruct;

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
        //return "Dereference " + source + " into " + dest;
        return dest + " = *" + source;
    }
    @Override
    public void printx86(X86Emitter emit) {
        emit.addStatement("movq " + source.x86() + ", %rax");
        if (dest.getType() instanceof TypeNumerical) {
            TypeNumerical d = (TypeNumerical) dest.getType();
            emit.addStatement("mov" + d.x86typesuffix() + " (%rax), " + X86Register.C.getRegister(d));
            emit.addStatement("mov" + d.x86typesuffix() + " " + X86Register.C.getRegister(d) + ", " + dest.x86());
        } else if (dest.getType() instanceof TypeStruct) {
            TypeStruct ts = (TypeStruct) dest.getType();
            moveStruct(0, "%rax", dest.getStackLocation(), ts.struct, emit);
        } else {
            throw new RuntimeException();
        }
    }
    public static void moveStruct(int sourceStackLocation, String sourceRegister, int destLocation, Struct struct, X86Emitter emit) {
        int size = new TypeStruct(struct).getSizeBytes();
        //this is a really bad way to do this
        for (int i = 0; i < size; i++) {
            emit.addStatement("movb " + (i + sourceStackLocation) + "(" + sourceRegister + "), %cl");
            emit.addStatement("movb %cl, " + (destLocation + i) + "(%rbp)");
        }
    }
}
