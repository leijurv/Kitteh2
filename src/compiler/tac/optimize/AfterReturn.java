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
 * Any statement after a return / unconditional jump and before a jump
 * destination is unreachable.
 *
 * @author leijurv
 */
public class AfterReturn extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACReturn || block.get(i).getClass() == TACJump.class) {
                while (i + 1 < block.size()) {
                    block.remove(i + 1);
                }
            }
        }
    }
}
