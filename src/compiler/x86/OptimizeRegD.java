/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.type.TypeNumerical;
import java.util.List;

/**
 * This optimization removes a useless move in a scenario that can occur after
 * dataflowanalysis. Basically, tmpVar_%rdx = malloc(tmp_%eax);
 * otherVar_%someRegister = tmpVar_%rdx; This gets compiled to a call to malloc,
 * then moving the resulting %rax to %rdx. Then, dataflowanalysis realizes that
 * it can move from %rax instead of %rdx to the new register (because that
 * tacfunctioncall got compiled to the call then movq %rax, %rdx). This results
 * in a dangling useless move: call malloc; movq %rax, %rdx; movq %rax, %r11 (or
 * similar). The movq %rax, %rdx is useless, and this optimization removes it.
 * It just needs to make sure that %rdx isn't used again in this basic block;
 * until a jump destination.
 *
 * @author leijurv
 */
public class OptimizeRegD {
    public static void optimize(List<X86Statement> statements) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof Move) {
                Move m = (Move) statements.get(i);
                if (m.getDest() instanceof X86TypedRegister && ((X86TypedRegister) m.getDest()).getRegister() == X86Register.D && checkRegisterDUnusedUntilLabel(i, statements)) {
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
    public static boolean checkRegisterDUnusedUntilLabel(int pos, List<X86Statement> statements) {
        for (int j = pos + 1; j < statements.size(); j++) {
            if (statements.get(j) instanceof Label) {
                return true;
            }
            if (statements.get(j) instanceof Comment) {
                continue;
            }
            if (statements.get(j).toString().contains("syscall")) {//syscalls can use register D, so this optimization can't be applied
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
