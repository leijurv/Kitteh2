/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACJump;
import compiler.tac.TACReturn;
import compiler.tac.TACStatement;
import java.util.List;

/**
 * Eliminates jumps to unconditional jumps. If jump A's destination is jump B,
 * and jump B is unconditional, you can set A's destination to be equal to B's
 * destination.
 *
 * @author leijurv
 */
public class DoubleJump extends TACOptimization {
    @Override
    public List<TACStatement> go(List<TACStatement> stmts) {//don't use the framework of keeping track of jump destinations, because that would be self destructive. also we won't add or remove statements
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.get(i) instanceof TACJump) {
                TACJump first = (TACJump) stmts.get(i);
                if (first.jumpTo() >= stmts.size()) {
                    for (int j = 0; j < stmts.size(); j++) {
                        System.out.println(j + ":  " + stmts.get(j));
                    }
                    throw new IllegalStateException();
                }
                TACStatement dest = stmts.get(first.jumpTo());
                if (dest instanceof TACReturn && first.getClass() == TACJump.class) {//if the jump is unconditional and the destination is a return
                    stmts.set(i, dest);//we can just return. jumping to a return is redundant
                }
                if (dest instanceof TACJump) {
                    TACJump second = (TACJump) dest;
                    boolean unconditionalSecond = dest.getClass() == TACJump.class;
                    if (unconditionalSecond) {
                        first.setJumpTo(second.jumpTo());
                    }
                }
            }
        }
        return stmts;
    }
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
