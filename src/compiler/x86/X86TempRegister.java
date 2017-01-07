/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.TypeNumerical;

/**
 *
 * @author leijurv
 */
public class X86TempRegister extends X86TypedRegister {
    String varFrom;
    public X86TempRegister(X86Register register, TypeNumerical type, String variableFrom) {
        super(register, type);
        this.varFrom = variableFrom;
    }
    @Override
    public String toString() {
        return compiler.Compiler.verbose() ? (varFrom + "_" + super.toString()) : super.toString();
    }
}
