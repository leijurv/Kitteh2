/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.Context;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACJump;
import compiler.tac.TACReturn;
import compiler.tac.TACStatement;
import compiler.tac.TempVarUsage;
import compiler.x86.X86Register;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class UnusedAssignment extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size(); i++) {
            List<String> mv = block.get(i).modifiedVariables();
            Context.printFull = true;//TODO this is really bad, especially since optimizations are applied in a multithreaded manner
            if (block.get(i).toString().contains("Struct")) {
                continue;
            }
            if (block.get(i) instanceof TACFunctionCall) {
                continue;
            }
            if (mv.isEmpty()) {
                continue;
            }
            if (mv.size() != 1) {
                throw new RuntimeException();
            }
            if (mv.get(0).startsWith(X86Register.REGISTER_PREFIX)) {
                continue;
            }
            if (mv.get(0).contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX)) {
                continue;
            }
            if (usedAfter(block, mv.get(0), i)) {
                continue;
            }
            block.remove(i);
            return;
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
            if (block.get(i) instanceof TACReturn) {
                return false;
            }
        }
        return true;//might be used after this block
    }
}
