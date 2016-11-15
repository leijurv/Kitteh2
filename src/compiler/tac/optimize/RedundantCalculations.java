/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.Operator;
import compiler.tac.TACConst;
import compiler.tac.TACStandard;
import compiler.tac.TACStatement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class RedundantCalculations extends TACOptimization {
    public RedundantCalculations(ArrayList<TACStatement> statements) {
        super(statements);
    }
    @Override
    protected void run(List<TACStatement> block) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACStandard) {
                TACStandard ts = (TACStandard) (block.get(i));
                if (ts.op == Operator.PLUS) {
                    if (ts.firstName.equals("0")) {
                        TACConst repl = new TACConst(ts.resultName, ts.secondName);
                        repl.dest = ts.result;
                        repl.source = ts.second;
                        repl.context = ts.context;
                        block.set(i, repl);
                        continue;
                    }
                    if (ts.secondName.equals("0")) {
                        TACConst repl = new TACConst(ts.resultName, ts.firstName);
                        repl.dest = ts.result;
                        repl.source = ts.first;
                        repl.context = ts.context;
                        block.set(i, repl);
                        continue;
                    }
                }
                if (ts.op == Operator.MULTIPLY) {
                    if (ts.firstName.equals("1")) {
                        TACConst repl = new TACConst(ts.resultName, ts.secondName);
                        repl.dest = ts.result;
                        repl.source = ts.second;
                        repl.context = ts.context;
                        block.set(i, repl);
                        continue;
                    }
                    if (ts.secondName.equals("1")) {
                        TACConst repl = new TACConst(ts.resultName, ts.firstName);
                        repl.dest = ts.result;
                        repl.source = ts.first;
                        repl.context = ts.context;
                        block.set(i, repl);
                        continue;
                    }
                }
            }
        }
    }
}
