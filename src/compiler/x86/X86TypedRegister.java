/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.Type;
import compiler.type.TypeNumerical;

/**
 *
 * @author leijurv
 */
public class X86TypedRegister implements X86Param {
    private final X86Register register;
    private final TypeNumerical type;
    public X86TypedRegister(X86Register register, TypeNumerical type) {
        this.register = register;
        this.type = type;
    }
    @Override
    public String x86() {
        return register.getRegister(type);
    }
    @Override
    public String getName() {
        return x86();
    }
    @Override
    public Type getType() {
        return type;
    }
    @Override
    public String toString() {
        return x86();
    }
}
