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
 *
 * @author leijurv
 */
public class JumpOver extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        if (block.isEmpty()) {
            return;
        }
        if (block.get(block.size() - 1) instanceof TACJump) {//have this one separate because it can fire for any type of tacjump
            int dest = ((TACJump) block.get(block.size() - 1)).jumpTo();
            if (dest == blockBegin + block.size()) {
                block.remove(block.size() - 1);
            }
        }
    }
}
