/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACJump;
import compiler.tac.TACStatement;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class UnusedAssignment extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACFunctionCall) {
                continue;
            }
            if (block.get(i).toString().contains("Struct")) {
                continue;
            }
            List<String> mv = block.get(i).modifiedVariables();
            if (mv.isEmpty()) {
                continue;
            }
            if (mv.size() != 1) {
                throw new RuntimeException();
            }
            if (!usedAfter(block, mv.get(0), i)) {
                block.remove(i);
                return;
            }
        }
    }
    static boolean usedAfter(List<TACStatement> block, String varName, int lineNumber) {
        for (int i = lineNumber + 1; i < block.size(); i++) {
            if (block.get(i).requiredVariables().contains(varName)) {
                return true;
            }
            if (block.get(i) instanceof TACJump) {
                return true;
            }
            if (block.get(i).modifiedVariables().contains(varName)) {
                return false;
            }
        }
        return true;//might be used after this block
    }
}
