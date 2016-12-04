/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACJump;
import compiler.tac.TACJumpBoolVar;
import compiler.tac.TACStatement;
import compiler.x86.X86Param;
import java.util.List;

/**
 * this may seem niche, and it is, but it makes the resulting tac for
 * (tm||ci)&&(!tm||!ci) shorter by 1 comparison/jump when tm is true
 *
 * @author leijurv
 */
public class ConditionalDoubleJump extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        throw new UnsupportedOperationException();
    }
    public List<TACStatement> go(List<TACStatement> stmts) {
        go0(stmts);
        return stmts;
    }
    public void go0(List<TACStatement> stmts) {
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.get(i) instanceof TACJumpBoolVar) {
                TACJumpBoolVar tjbr = (TACJumpBoolVar) stmts.get(i);
                X86Param knownValue = tjbr.params[0];
                for (int j = i + 1; j < stmts.size(); j++) {
                    if (!(stmts.get(j) instanceof TACJump)) {
                        break;
                    }
                    if (j != tjbr.jumpTo()) {
                        continue;
                    }
                    if (stmts.get(j) instanceof TACJumpBoolVar) {
                        TACJumpBoolVar second = (TACJumpBoolVar) stmts.get(j);
                        if (second.params[0].equals(knownValue)) {
                            boolean known = tjbr.invert ^ second.invert;
                            if (known) {
                                tjbr.setJumpTo(j + 1);
                            } else {
                                tjbr.setJumpTo(second.jumpTo());
                            }
                        }
                    }
                }
            }
        }
    }
}
