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
    X86Param source;
    X86Param dest;
    public Cast(X86Param source, X86Param dest) {
        this.source = source;
        this.dest = dest;
    }
    @Override
    public String toString() {
        String cast = "movs" + ((TypeNumerical) source.getType()).x86typesuffix() + "" + ((TypeNumerical) dest.getType()).x86typesuffix() + " " + source.x86() + ", " + dest.x86();
        if (cast.equals("movsbw %al, %ax")) {
            cast = "cbtw";
        }
        if (cast.equals("movswl %ax, %eax")) {
            cast = "cwtl";
        }
        if (cast.equals("movslq %eax, %rax")) {//lol
            cast = "cltq";//lol
        }
        return cast;
    }
}
