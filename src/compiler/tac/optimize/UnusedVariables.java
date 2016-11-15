/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACConst;
import compiler.tac.TACStatement;
import compiler.type.TypeStruct;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class UnusedVariables extends TACOptimization {
    public UnusedVariables(ArrayList<TACStatement> statements) {
        super(statements);
    }
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
