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
    A, B, C, D, SI, DI, R8, R9, R10, R11, R12, R13, R14, R15;
    public String getRegister(TypeNumerical version) {
        //technically shouldn't be modified without restoring on return: B, R12, R13, R14, R15
        /*
         Registers %rbp, %rbx and
%r12 through %r15 “belong” to the calling function and the called function is
required to preserve their values. In other words, a called function must preserve
these registers’ values for its caller.
        from the system V abi
         */
        switch (this) {
            case B:
            case R12:
            case R13:
            case R14:
            case R15:
                throw new NegativeArraySizeException("Can't use " + this + " because kitteh2 doesn't support callee spills");
        }
        switch (this) {
            case A:
            case B:
            case C:
            case D:
                return REGISTER_PREFIX + version.x86registerprefix() + toString().toLowerCase() + version.x86registersuffix();//e.g. %eax
            case SI:
            case DI:
                return REGISTER_PREFIX + version.x86registerprefix() + toString().toLowerCase() + (version.x86registersuffix() == 'l' ? "l" : "");
            case R8:
            case R9:
            case R10:
            case R11:
            case R12:
            case R13:
            case R14:
            case R15:
                return REGISTER_PREFIX + toString().toLowerCase() + version.x86r_registersuffix();
        }
        throw new IllegalStateException();
    }
    public static final String REGISTER_PREFIX = "%";
}
