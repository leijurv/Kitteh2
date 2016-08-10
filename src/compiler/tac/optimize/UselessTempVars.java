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
 *
 * @author leijurv
 */
public class UselessTempVars extends TACOptimization {
    public UselessTempVars(ArrayList<TACStatement> statements) {
        super(statements);
    }
    @Override
    public void run() {
        for (int i = 0; i < size() - 1; i++) {
            if (!(get(i) instanceof TACConst)) {
                continue;
            }
            TACConst curr = (TACConst) get(i);
            String valSet = curr.varName;
            if (!valSet.startsWith("t")) {
                continue;
            }
            TACStatement next = get(i + 1);
            if (next instanceof TACStandard) {
                TACStandard n = (TACStandard) next;
                if (n.secondName.equals(valSet)) {
                    System.out.println("Optimizing " + valSet + " " + curr + "    " + next);
                    n.secondName = curr.val;
                    n.second = curr.vall;
                    remove(i);
                    i = 0;
                    continue;
                }
                if (n.firstName.equals(valSet)) {
                    System.out.println("Optimizing " + valSet + " " + curr + "    " + next);
                    n.firstName = curr.val;
                    n.first = curr.vall;
                    remove(i);
                    i = 0;
                    continue;
                }
            }
        }
    }
}
