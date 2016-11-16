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
public class DeadCode extends TACOptimization {
    int rangeBegin = -1;
    int rangeEnd = -1;
    @Override
    public void reset(ArrayList<TACStatement> statements) {
        super.reset(statements);
        rangeBegin = -1;
        rangeEnd = -1;
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i).getClass() == TACJump.class) {
                TACJump tj = (TACJump) statements.get(i);
                if (tj.jumpTo() < i) {
                    continue;
                }
                if (tj.jumpTo() == i) {
                    throw new RuntimeException("infinite loop");
                }
                boolean dead = !accessibleFromExterior(i);
                if (dead) {
                    rangeBegin = i;
                    rangeEnd = tj.jumpTo();
                    break;
                }
            }
        }
    }
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        if (blockBegin >= rangeBegin && blockBegin < rangeEnd) {
            block.clear();
        }
    }
}
