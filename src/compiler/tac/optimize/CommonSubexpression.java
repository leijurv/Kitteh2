/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACConst;
import compiler.tac.TACStandard;
import compiler.tac.TACStatement;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class CommonSubexpression extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACStandard) {
                TACStandard ts = (TACStandard) block.get(i);
                for (int j = i + 1; j < block.size(); j++) {
                    List<String> mod = block.get(j).modifiedVariables();
                    if (mod.contains(ts.firstName) || mod.contains(ts.secondName) || mod.contains(ts.resultName)) {
                        break;
                    }
                    if (block.get(j) instanceof TACStandard) {
                        TACStandard o = (TACStandard) block.get(j);
                        if (o.op == ts.op && o.firstName.equals(ts.firstName) && o.secondName.equals(ts.secondName)) {
                            System.out.println("Optimizing " + i + " " + j + " " + ts + " " + o);
                            TACConst repl = new TACConst(o.resultName, ts.resultName);
                            repl.context = o.context;
                            repl.dest = o.result;
                            repl.source = ts.result;
                            block.set(j, repl);
                            return;
                        }
                    }
                }
            }
        }
    }
}
