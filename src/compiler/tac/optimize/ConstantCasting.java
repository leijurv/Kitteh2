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
                if (con.params[0] == null) {
                    try {
                        Integer.parseInt(con.paramNames[0]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                } else {
                    continue;
                }
                if (!UselessTempVars.isTempVariable(con.paramNames[1])) {
                    continue;
                }
                if (block.get(i + 1) instanceof TACCast) {
                    TACCast cast = (TACCast) block.get(i + 1);
                    if (cast.paramNames[0].equals(con.paramNames[1]) && cast.params[0].equals(con.params[1])) {
                        con.replace(con.paramNames[1], cast.paramNames[1], cast.params[1]);
                        block.remove(i + 1);
                        i--;
                    }
                }
            }
        }
    }
}
