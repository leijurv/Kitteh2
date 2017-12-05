/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACJump;
import compiler.tac.TACStatement;
import java.util.List;

/**
 * any jumps to the next statement, conditional or not, are useless and should
 * be removed. only check the last item of a block because a jump to the last
 * statement will always be the last item of a block, because the next item is
 * always a jump destination
 *
 * @author leijurv
 */
public class JumpOver extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        if (block.isEmpty()) {
            return;
        }
        if (block.get(block.size() - 1) instanceof TACJump) {
            int dest = ((TACJump) block.get(block.size() - 1)).jumpTo();
            if (dest == blockBegin + block.size()) {
                block.remove(block.size() - 1);
            }
        }
    }
}
