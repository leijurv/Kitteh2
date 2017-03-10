/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.TypeNumerical;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class OptimizeRegD {
    public static void optimize(List<X86Statement> statements) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof Move) {
                Move m = (Move) statements.get(i);
                if (m.getDest() instanceof X86TypedRegister && ((X86TypedRegister) m.getDest()).getRegister() == X86Register.D && doTheThing(i, statements)) {
                    if (compiler.Compiler.verbose()) {
                        statements.set(i, new Comment("REMOVED BECAUSE REDUNDANT " + statements.get(i)));
                    } else {
                        statements.remove(i);
                    }
                    i = -1;
                }
            }
        }
    }
    public static boolean doTheThing(int pos, List<X86Statement> statements) {
        for (int j = pos + 1; j < statements.size(); j++) {
            if (statements.get(j) instanceof Label) {
                /*System.out.println("Removing " + statements.get(pos));
                System.out.println("After " + statements.get(pos - 1));
                System.out.println("Before " + statements.get(pos + 1));*/
                return true;
            }
            if (statements.get(j) instanceof Comment) {
                continue;
            }
            if (statements.get(j).toString().contains("syscall")) {
                return false;
            }
            for (TypeNumerical type : TypeNumerical.INTEGER_TYPES) {
                if (statements.get(j).toString().contains(X86Register.D.getRegister1(type, true))) {
                    return false;
                }
            }
        }
        return true;
    }
}
