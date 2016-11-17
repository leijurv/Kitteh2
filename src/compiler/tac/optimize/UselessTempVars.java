/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACCast;
import compiler.tac.TACConst;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACPointerDeref;
import compiler.tac.TACPointerRef;
import compiler.tac.TACStandard;
import compiler.tac.TACStatement;
import compiler.tac.TempVarUsage;
import java.util.List;

/**
 * Remove temp vars that have no point Like t1=5,a=t1+t2 should become a=5+t2
 *
 * @author leijurv
 */
public class UselessTempVars extends TACOptimization {
    public static boolean isTempVariable(String s) {
        if (!s.startsWith(TempVarUsage.TEMP_VARIABLE_PREFIX)) {//all temp vars start with t. variables starting with a t are not supported in kitteh
            return false;
        }
        try {
            Integer.parseInt(s.substring(TempVarUsage.TEMP_VARIABLE_PREFIX.length()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    @Override
    public void run(List<TACStatement> block, int blockBegin) {
        for (int ind = 0; ind < block.size() - 1; ind++) {
            if (!(block.get(ind) instanceof TACConst)) {
                continue;
            }
            TACConst curr = (TACConst) block.get(ind);
            String valSet = curr.destName;
            if (!isTempVariable(valSet)) {
                continue;
            }
            TACStatement next = block.get(ind + 1);
            if (next instanceof TACStandard) {
                TACStandard n = (TACStandard) next;
                if (n.secondName.equals(valSet)) {
                    //System.out.println("Optimizing " + valSet + " " + curr + "    " + next);
                    n.secondName = curr.sourceName;
                    n.second = curr.source;
                    block.remove(ind);
                    ind = Math.max(-1, ind - 2);
                    continue;
                }
                if (n.firstName.equals(valSet)) {
                    //System.out.println("Optimizing " + valSet + " " + curr + "    " + next);
                    n.firstName = curr.sourceName;
                    n.first = curr.source;
                    block.remove(ind);
                    ind = Math.max(-1, ind - 2);
                    continue;
                }
            }
            if (next instanceof TACConst) {
                TACConst c = (TACConst) next;
                if (c.sourceName.equals(valSet)) {
                    //System.out.println("Optimizing " + valSet + " " + curr + "    " + next);
                    c.sourceName = curr.sourceName;
                    c.source = curr.source;
                    block.remove(ind);
                    ind = Math.max(-1, ind - 2);
                    continue;
                }
            }
            if (next instanceof TACFunctionCall) {
                TACFunctionCall c = (TACFunctionCall) next;
                boolean shouldContinue = false;
                for (int i = 0; i < c.paramNames.size(); i++) {
                    if (c.paramNames.get(i).equals(valSet)) {
                        //System.out.println("Optimizing " + valSet + " " + curr + "    " + next);
                        c.paramNames.set(i, curr.sourceName);
                        c.params.set(i, curr.source);
                        block.remove(ind);
                        ind = Math.max(-1, ind - 2);
                        shouldContinue = true;
                        break;
                    }
                }
                if (shouldContinue) {
                    continue;
                }
            }
            if (next instanceof TACPointerRef) {
                TACPointerRef t = (TACPointerRef) next;
                if (t.sourceName.equals(valSet)) {
                    t.source = curr.source;
                    t.sourceName = curr.sourceName;
                    block.remove(ind);
                    ind = Math.max(-1, ind - 2);
                    continue;
                }
            }
            if (next instanceof TACPointerDeref) {
                TACPointerDeref t = (TACPointerDeref) next;
                if (t.sourceName.equals(valSet)) {
                    t.source = curr.source;
                    t.sourceName = curr.sourceName;
                    block.remove(ind);
                    ind = Math.max(-1, ind - 2);
                    continue;
                }
            }
            if (next instanceof TACCast) {
                TACCast t = (TACCast) next;
                if (t.inputName.equals(valSet) && curr.source != null) {
                    t.input = curr.source;
                    t.inputName = curr.sourceName;
                    block.remove(ind);
                    ind = Math.max(-1, ind - 2);
                    continue;
                }
            }
        }
    }
}
