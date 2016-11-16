/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACCast;
import compiler.tac.TACConst;
import compiler.tac.TACStatement;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class ConstantCasting extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size() - 1; i++) {
            if (block.get(i) instanceof TACConst) {
                TACConst con = (TACConst) block.get(i);
                if (con.source == null) {
                    try {
                        Integer.parseInt(con.sourceName);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                } else {
                    continue;
                }
                if (!UselessTempVars.isTempVariable(con.destName)) {
                    continue;
                }
                if (block.get(i + 1) instanceof TACCast) {
                    TACCast cast = (TACCast) block.get(i + 1);
                    if (cast.inputName.equals(con.destName) && cast.input.equals(con.dest)) {
                        con.dest = cast.dest;
                        con.destName = cast.destName;
                        block.remove(i + 1);
                        i--;
                    }
                }
            }
        }
    }
}
