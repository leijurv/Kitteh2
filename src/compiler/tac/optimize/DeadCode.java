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
 * This optimization doesn't come up in any of the test cases. It eliminates
 * things like: if false { anything with internal jumps, like a for loop or
 * another if}
 *
 * @author leijurv
 */
public class DeadCode extends TACOptimization {
    int rangeBegin = -1;
    int rangeEnd = -1;
    @Override
    public void reset(List<TACStatement> statements) {
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
        if (rangeBegin == -1 || rangeEnd == -1) {
            return;
        }
        int localRangeBegin = rangeBegin - blockBegin;
        int localRangeEnd = rangeEnd - blockBegin;
        if (localRangeEnd <= localRangeBegin) {
            throw new RuntimeException(rangeBegin + " " + rangeEnd + " " + localRangeBegin + " " + localRangeEnd);
        }
        if (localRangeEnd <= 0) {
            return;
        }
        if (localRangeBegin > block.size() - 1) {
            return;
        }
        localRangeBegin = Math.max(0, localRangeBegin);
        localRangeEnd = Math.min(block.size(), localRangeEnd);
        block.subList(localRangeBegin, localRangeEnd).clear();
    }
}
