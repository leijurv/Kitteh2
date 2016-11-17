/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACConst;
import compiler.tac.TACStatement;
import compiler.type.TypeStruct;
import java.util.List;

/**
 * note that this is a different optimization than unusedassignment. this one
 * only applies to tacconsts and it follows jumps (conditional or otherwise) to
 * check all locations a variable might be used. this one only looks for
 * variables that are set and unused, NOT for variables that are set and reset
 * (that's unusedassignment's job)
 *
 * @author leijurv
 */
public class UnusedVariables extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size(); i++) {
            TACStatement ts = block.get(i);
            int pos = blockBegin + i;
            if (ts instanceof TACConst) {
                String dest = ((TACConst) ts).destName;
                if (dest.startsWith("%")) {
                    continue;
                }
                if (dest.contains("sketchymanual")) {
                    continue;//if you comment out this line, the tests fail.
                }
                if (((TACConst) ts).dest.getType() instanceof TypeStruct) {
                    continue;
                }
                if (!isUsedAtOrAfter(pos, dest)) {
                    block.remove(ts);
                    return;
                }
            }
        }
    }
}
