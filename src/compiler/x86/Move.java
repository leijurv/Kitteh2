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
public class Move extends X86Statement {
    private final X86Param source;
    private final X86Param dest;
    public Move(X86Param source, X86Param dest) {
        this.source = source;
        this.dest = dest;
    }
    @Override
    public String toString() {
        String a = ((TypeNumerical) dest.getType()).x86typesuffix();
        if (!a.equals(((TypeNumerical) source.getType()).x86typesuffix()) && !(source instanceof X86Const)) {
            throw new IllegalStateException(source.x86() + " " + dest.x86());
        }
        if (source.x86().equals("$0") && dest instanceof X86TypedRegister) {
            return "xor" + a + " " + dest.x86() + ", " + dest.x86();
        }
        return "mov" + a + " " + source.x86() + ", " + dest.x86();
    }
    public X86Param getSource() {
        return source;
    }
    public X86Param getDest() {
        return dest;
    }
}
