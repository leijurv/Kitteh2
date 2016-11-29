/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.Context.VarInfo;
import compiler.tac.TACCast;
import compiler.tac.TACConst;
import compiler.tac.TACJump;
import compiler.tac.TACJumpBoolVar;
import compiler.tac.TACStatement;
import compiler.tac.TempVarUsage;
import compiler.type.TypeStruct;
import java.util.List;

/**
 * Remove temp vars that have no point Like t1=5,a=t1+t2 should become a=5+t2
 *
 * @author leijurv
 */
public class UselessTempVars extends TACOptimization {
    public static final boolean AGGRESSIVE = true;
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
            String valSet = curr.paramNames[1];
            if (valSet.contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX)) {
                continue;
            }
            int st = ind + 1;
            boolean tempVar = isTempVariable(valSet);
            if (!tempVar) {
                if (!AGGRESSIVE) {
                    continue;
                }
                block.add(ind, null);//<horrible hack>
                st++;
            }
            String currSourceName = curr.paramNames[0];
            VarInfo currSource = curr.params[0];
            if (currSourceName.equals(valSet)) {
                //replacement wouldn't... even do anything
                while (block.contains(null)) {
                    block.remove(null);
                }
                continue;
            }
            for (int usageLocation = st; usageLocation < block.size(); usageLocation++) {
                TACStatement next = block.get(usageLocation);
                if (curr.params[1] != null && curr.params[1].getType() instanceof TypeStruct && usageLocation > st) {
                    //break;
                    //this break used to be necesary to make the tests suceed, but I just commented it out and it doesn't make anything fail
                    //go figure
                    //leaving it here but commented out if it comes up in the future
                }
                if (next instanceof TACJumpBoolVar && next.requiredVariables().contains(valSet) && tempVar) {
                    throw new RuntimeException("This won't happen as of the current TAC generation of boolean statements " + next + " " + curr);//but if i change things in the future this could happen and isn't a serious error
                }
                boolean exemption = next instanceof TACCast && currSource == null;
                if (!exemption && next.requiredVariables().contains(valSet)) {
                    next.replace(valSet, currSourceName, currSource);
                    block.remove(ind);
                    ind = Math.max(-1, ind - 2);
                    break;
                }
                if (next.requiredVariables().contains(valSet)) {
                    //this temp variable is used in a context that does not allow for optimized insertion
                    //since temp variables are only used once, we can't insert it into an expression after its usage
                    if (!exemption) {
                        throw new RuntimeException(next + " " + curr);
                    }
                    break;
                }
                if (next.modifiedVariables().contains(valSet)) {
                    if (!tempVar) {//if it's not a temp var, this isn't actually a problem, just stahp
                        break;
                    }
                    //something is wrong
                    //something like:
                    //tmp0=1
                    // ... (temp0 not used)
                    //tmp0=2
                    //so somehow tmp0 was set but then unused
                    //again, this might be caused by a different optimization leaving dangling temp variables
                    throw new RuntimeException("Another optimization did something weird");
                }
                if (next.modifiedVariables().contains(currSourceName)) {
                    if (tempVar) {
                        //tmp0=b
                        // ... (tmp0 not used)
                        //b modified
                        throw new RuntimeException("this apparently doesn't happen but i can think of scenarios where it might");
                    }
                    //no longer would be a valid replacement
                    break;
                }
                if (next instanceof TACJump) {
                    if (!tempVar) {
                        //perfectly valid to set a non-temp-var then jump
                        break;
                    }
                    //if it gets to here, it means something is wrong
                    //it means that a temp variable is set, then goes unused up to a jump
                    //and since no temp variables are used again after a jump
                    //it means that somehow this temp variable was generated but went unused entirely
                    //it could indicate a bug in a different optimization btw
                    throw new RuntimeException("Another optimization did something weird");
                }
            }
            while (block.contains(null)) {//</horrible hack>
                block.remove(null);
            }
        }
    }
}
