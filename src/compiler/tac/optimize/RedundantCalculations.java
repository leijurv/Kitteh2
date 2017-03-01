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
import java.util.List;

/**
 * Remove some simple calculations that are redundant. Specifically, adding to
 * zero or multiplying by one.
 *
 * @author leijurv
 */
public class RedundantCalculations extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACStandard) {
                TACStandard ts = (TACStandard) (block.get(i));
                if (ts.op == Operator.PLUS) {
                    if (ts.params[0].x86().equals("$0")) {
                        TACConst repl = new TACConst(ts.params[2], ts.params[1]);
                        repl.copyFrom(ts);
                        repl.params[1] = ts.params[2];//ensure type is copied properly
                        repl.params[0] = ts.params[1];
                        //repl.setVars();
                        if (compiler.Compiler.verbose()) {
                            System.out.println(ts + " IS NOW " + repl);
                        }
                        block.set(i, repl);
                        continue;
                    }
                    if (ts.params[1].x86().equals("$0")) {
                        TACConst repl = new TACConst(ts.params[2], ts.params[0]);
                        repl.copyFrom(ts);
                        repl.params[1] = ts.params[2];
                        repl.params[0] = ts.params[0];
                        //repl.setVars();
                        if (compiler.Compiler.verbose()) {
                            System.out.println(ts + " IS NOW " + repl);
                        }
                        block.set(i, repl);
                        continue;
                    }
                }
                if (ts.op == Operator.MULTIPLY) {
                    if (ts.params[0].x86().equals("$1")) {
                        TACConst repl = new TACConst(ts.params[2], ts.params[1]);
                        repl.copyFrom(ts);
                        repl.params[1] = ts.params[2];
                        repl.params[0] = ts.params[1];
                        //repl.setVars();
                        if (compiler.Compiler.verbose()) {
                            System.out.println(ts + " IS NOW " + repl);
                        }
                        block.set(i, repl);
                        continue;
                    }
                    if (ts.params[1].x86().equals("$1")) {
                        TACConst repl = new TACConst(ts.params[2], ts.params[0]);
                        repl.copyFrom(ts);
                        repl.params[1] = ts.params[2];
                        repl.params[0] = ts.params[0];
                        //repl.setVars();
                        if (compiler.Compiler.verbose()) {
                            System.out.println(ts + " IS NOW " + repl);
                        }
                        block.set(i, repl);
                        continue;//unnecesary continue, I know, its here for Symmetry
                    }
                }
                //TODO shifting x by 0, or shifting 0 by x
            }
        }
    }
}
