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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACCast extends TACStatement {
    public String inputName;
    public VarInfo input;
    public String destName;
    public VarInfo dest;
    public TACCast(String inputName, String dest) {
        this.inputName = inputName;
        this.destName = dest;
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(inputName);
    }
    @Override
    protected void onContextKnown() {
        input = context.getRequired(inputName);
        dest = context.getRequired(destName);
    }
    @Override
    public String toString0() {
        return dest + " = (" + dest.getType() + ") " + input;
    }
    @Override
    public void printx86(X86Emitter emit) {
        cast(input, dest, emit);
    }
    public static void cast(VarInfo input, VarInfo dest, X86Emitter emit) {
        TypeNumerical inp = (TypeNumerical) input.getType();
        TypeNumerical out = (TypeNumerical) dest.getType();
        if (inp.getSizeBytes() >= out.getSizeBytes()) {
            //down cast
            if (inp.equals(out)) {
                throw new IllegalStateException(input + " " + dest + " " + inp.getSizeBytes() + " " + out.getSizeBytes());
            }
            emit.addStatement("mov" + inp.x86typesuffix() + " " + input.x86() + ", " + X86Register.C.getRegister(inp));
        } else {
            //up cast
            emit.addStatement("movs" + inp.x86typesuffix() + "" + out.x86typesuffix() + " " + input.x86() + ", " + X86Register.C.getRegister(out));
        }
        emit.addStatement("mov" + out.x86typesuffix() + " " + X86Register.C.getRegister(out) + ", " + dest.x86());
    }
}
