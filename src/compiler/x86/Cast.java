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
public class Cast extends X86Statement {
    private final X86Param source;
    private final X86Param dest;
    public Cast(X86Param source, X86Param dest) {
        if (source.getType().getSizeBytes() >= dest.getType().getSizeBytes()) {
            throw new IllegalStateException(source + " " + dest + " " + source.getType() + " " + dest.getType());
        }
        this.source = source;
        this.dest = dest;
    }
    @Override
    public String toString() {
        String cast = "movs" + ((TypeNumerical) source.getType()).x86typesuffix() + "" + ((TypeNumerical) dest.getType()).x86typesuffix() + " " + source.x86() + ", " + dest.x86();
        if ("movsbw %al, %ax".equals(cast)) {
            cast = "cbtw";
        }
        if ("movswl %ax, %eax".equals(cast)) {
            cast = "cwtl";
        }
        if ("movslq %eax, %rax".equals(cast)) {//lol
            cast = "cltq";//lol
        }
        return cast;
    }
}
