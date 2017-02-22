/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.TypeFloat;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.IllegalSelectorException;
import java.nio.file.ClosedWatchServiceException;
import java.util.FormatterClosedException;
import java.util.Locale;

/**
 *
 * @author leijurv
 */
public enum X86Register {
    A, B, C, D, SI, DI, R8, R9, R10, R11, R12, R13, R14, R15, XMM0, XMM1;
    public static final String REGISTER_PREFIX = "%";
    public static TypeNumerical typeFromRegister(String reg) {
        if (reg.startsWith(REGISTER_PREFIX)) {
            return typeFromRegister(reg.substring(1));
        }
        switch (reg.length()) {
            case 2:
                switch (reg.charAt(1)) {
                    case 'l':
                        return new TypeInt8();
                    case 'x':
                        return new TypeInt16();
                    default:
                        throw new IllegalBlockingModeException();
                }
            case 3:
                switch (reg.charAt(0)) {
                    case 'e':
                        return new TypeInt32();
                    case 'r':
                        return new TypeInt64();
                    default:
                        throw new FormatterClosedException();
                }
            default:
                throw new ClosedWatchServiceException();
        }
    }
    public X86TypedRegister getRegister(TypeNumerical version) {
        return new X86TypedRegister(this, version);
    }
    String getRegister1(TypeNumerical version) {
        return getRegister1(version, false);
    }
    public String getRegister1(TypeNumerical version, boolean allowSpills) {
        //technically shouldn't be modified without restoring on return: B, R12, R13, R14, R15
        /*
         Registers %rbp, %rbx and
%r12 through %r15 “belong” to the calling function and the called function is
required to preserve their values. In other words, a called function must preserve
these registers’ values for its caller.
        from the system V abi
         */
        //Actually, let's USE THESE REGISTERS ANYWAY =D
        //because live life on the edge
        /*if (!allowSpills) {
            switch (this) {
                case B:
                case R12:
                case R13:
                case R14:
                case R15:
                    //throw new NegativeArraySizeException("Can't use " + this + " because kitteh2 doesn't support callee spills");
            }
        }*/
        if ((this == XMM0 || this == XMM1) && !(version instanceof TypeFloat)) {
            throw new IllegalStateException();
        }
        switch (this) {
            case XMM0:
            case XMM1:
                return REGISTER_PREFIX + toString().toLowerCase(Locale.US);
            case A:
            case B:
            case C:
            case D:
                return REGISTER_PREFIX + version.x86registerprefix() + toString().toLowerCase(Locale.US) + version.x86registersuffix();//e.g. %eax
            case SI:
            case DI:
                return REGISTER_PREFIX + version.x86registerprefix() + toString().toLowerCase(Locale.US) + (version.x86registersuffix() == 'l' ? "l" : "");
            case R8:
            case R9:
            case R10:
            case R11:
            case R12:
            case R13:
            case R14:
            case R15:
                return REGISTER_PREFIX + toString().toLowerCase(Locale.US) + version.x86r_registersuffix();
        }
        throw new IllegalSelectorException();
    }
}
