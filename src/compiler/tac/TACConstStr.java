/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.util.Obfuscator;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACConstStr extends TACStatement {
    private final String value;
    public TACConstStr(X86Param variable, String value) {
        super(variable);
        this.value = value;
        if (!(params[0].getType() instanceof TypePointer)) {
            throw new IllegalStateException(params[0] + " " + params[0].getType());
        }
    }
    @Override
    public String toString() {
        return params[0] + " = CONST STR \"" + value + "\"";
    }
    @Override
    public void printx86(X86Emitter emit) {
        emit.addStatement("leaq " + getLabel() + "(%rip), %rax");
        emit.dfa.markRegisterDirty(X86Register.A);
        emit.move(X86Register.A.getRegister((TypeNumerical) params[0].getType()), params[0]);
    }
    public String getLabel() {
        return "str" + Obfuscator.obfuscate(value);
    }
    public String getValue() {
        return value;
    }
    @Override
    public List<X86Param> requiredVariables() {
        return Arrays.asList();
    }
    @Override
    public List<X86Param> modifiedVariables() {
        return Arrays.asList(params[0]);
    }
}
