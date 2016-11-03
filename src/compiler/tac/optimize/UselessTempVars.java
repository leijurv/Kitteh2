/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACConst;
import compiler.tac.TACStandard;
import compiler.tac.TACStatement;
import java.util.ArrayList;

/**
 * Remove temp vars that have no point Like t1=5,a=t1+t2 should become a=5+t2
 *
 * @author leijurv
 */
public class UselessTempVars extends TACOptimization {
    public UselessTempVars(ArrayList<TACStatement> statements) {
        super(statements);
    }
    @Override
    public void run() {
        for (int ind = 0; ind < size() - 1; ind++) {
            if (!(get(ind) instanceof TACConst)) {
                continue;
            }
            TACConst curr = (TACConst) get(ind);
            String valSet = curr.destName;
            if (!valSet.startsWith("t")) {//all temp vars start with t. variables starting with a t are not supported in kitteh
                continue;
            }
            TACStatement next = get(ind + 1);
            if (next instanceof TACStandard) {
                TACStandard n = (TACStandard) next;
                if (n.secondName.equals(valSet)) {
                    System.out.println("Optimizing " + valSet + " " + curr + "    " + next);
                    n.secondName = curr.sourceName;
                    n.second = curr.source;
                    remove(ind);
                    ind = Math.max(-1, ind - 1);
                    continue;
                }
                if (n.firstName.equals(valSet)) {
                    System.out.println("Optimizing " + valSet + " " + curr + "    " + next);
                    n.firstName = curr.sourceName;
                    n.first = curr.source;
                    remove(ind);
                    ind = Math.max(-1, ind - 1);
                    continue;
                }
            }
        }
    }
}
