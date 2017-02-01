/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACJump;
import compiler.tac.TACReturn;
import compiler.tac.TACStatement;
import compiler.tac.TempVarUsage;
import compiler.x86.X86Register;
import java.util.List;

/**
 * x=Something
 *
 * ... (x isn't used and there are no jumps away nor jump destinations)
 *
 * x is overwritten or the function returns
 *
 * "x=Something" can be removed because that set is useless
 *
 * @author leijurv
 */
public class UnusedAssignment extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size(); i++) {
            List<String> mv = block.get(i).modifiedVariables();
            if (block.get(i).toString(true).contains("Struct") && !block.get(i).toString(false).contains("*Struct")) {
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
        }
    }
    static boolean usedAfter(List<TACStatement> block, String varName, int lineNumber) {//only return false when its certain that it's unused
        for (int i = lineNumber + 1; i < block.size(); i++) {
            if (block.get(i).requiredVariables().contains(varName)) {//yes it is directly used
                return true;
            }
            if (block.get(i) instanceof TACJump) {//we're only looking at this block, if control passes away who knows if it might be used
                return true;
            }
            if (block.get(i).modifiedVariables().contains(varName)) {//this value is overwritten, so a previous assignment is useless
                return false;
            }
            if (block.get(i) instanceof TACReturn) {//if we return and it isn't used (%rax=x counts as using x), then safe to delete
                return false;
            }
        }
        return true;//might be used after this block
    }
}
