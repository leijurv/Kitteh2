/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.type.TypeNumerical;

/**
 *
 * @author leijurv
 */
public enum X86Register {
    A, B, C, D;
    public String getRegister(TypeNumerical version) {
        return "%" + version.x86registerprefix() + toString().toLowerCase() + version.x86registersuffix();//e.g. %eax
    }
}
