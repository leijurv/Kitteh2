/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.x86.X86Emitter;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACArrayRef extends TACStatement {
    public TACArrayRef(String array, String index, String source) {
        super(array, index, source);
    }
    @Override
    protected void onContextKnown() {
    }
    @Override
    public String toString0() {
        return paramNames[0] + "[" + paramNames[1] + "] = " + paramNames[2];
    }
    @Override
    public void printx86(X86Emitter emit) {
        emit.addComment("cancer");
        TypeNumerical pointingTo = (TypeNumerical) ((TypePointer) params[0].getType()).pointingTo();
        X86TypedRegister arr;
        if (params[0] instanceof X86TypedRegister) {
            arr = (X86TypedRegister) params[0];
        } else {
            arr = X86Register.C.getRegister((TypeNumerical) params[0].getType());
            emit.move(params[0], arr);
        }
        X86TypedRegister ind;
        if (params[1] instanceof X86TypedRegister) {
            ind = (X86TypedRegister) params[1];
        } else {
            ind = (arr.getRegister() == X86Register.C ? X86Register.A : X86Register.C).getRegister((TypeNumerical) params[1].getType());
            emit.move(params[1], ind);
        }
        X86TypedRegister source;
        if (params[2] instanceof X86TypedRegister) {
            source = (X86TypedRegister) params[2];
        } else {
            source = (arr.getRegister() == X86Register.C ? (ind.getRegister() == X86Register.A ? X86Register.D : X86Register.A) : X86Register.C).getRegister(pointingTo);
            emit.move(params[2], source);
        }
        if (arr.getRegister() == ind.getRegister() || ind.getRegister() == source.getRegister() || source.getRegister() == arr.getRegister()) {
            throw new IllegalStateException("not okay " + arr + " " + ind + " " + source + " for " + this);
        }
        emit.addStatement("mov" + pointingTo.x86typesuffix() + " " + source.x86() + ", (" + arr.x86() + ", " + ind.x86() + ", " + pointingTo.getSizeBytes() + ")");
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(paramNames);
    }
    @Override
    public List<String> modifiedVariables() {
        return Arrays.asList();
    }
}
