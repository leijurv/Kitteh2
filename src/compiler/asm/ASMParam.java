/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.asm;
import compiler.type.Type;

/**
 *
 * @author leijurv
 */
public interface ASMParam {
    default String x86() {
        throw new UnsupportedOperationException(getClass() + " has not implemented ASMParam.x86");
    }
    default String toASM(ASMArchitecture arch) {
        switch (arch) {
            case X86:
                return x86();
            default:
                throw new UnsupportedOperationException("Unsupported architecture " + arch);
        }
    }
    Type getType();
}
