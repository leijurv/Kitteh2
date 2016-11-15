/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACJump;
import compiler.tac.TACStatement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class JumpOver extends TACOptimization {
    public JumpOver(ArrayList<TACStatement> statements) {
        super(statements);
    }
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
        if (block.isEmpty()) {
            return;
        }
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).getClass() == TACJump.class) {//only if it's a non conditional tacjump
                int dest = ((TACJump) block.get(i)).jumpTo();
                if (dest == blockBegin + block.size()) {
                    while (i < block.size()) {
                        block.remove(i);
                    }
                }
            }
        }
    }
}
