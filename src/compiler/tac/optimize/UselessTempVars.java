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
import compiler.type.TypeFloat;
import compiler.x86.X86Const;
import compiler.x86.X86Param;
import java.util.List;

/**
 * Remove vars that have no point
 *
 * Like
 *
 * t1=5
 *
 * a=t1+t2
 *
 * should become
 *
 * a=5+t2
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
            TACStatement curr = block.get(ind);
            if (block.get(ind) instanceof TACCast) {
                if (curr.params[0].getType().getSizeBytes() < curr.params[1].getType().getSizeBytes()) {
                    //it actually is a cast up or down
                    continue;
                }
                if (curr.params[0].getType() instanceof TypeFloat || curr.params[1].getType() instanceof TypeFloat) {
                    continue;
                }
                //continue;
            } else if (!(block.get(ind) instanceof TACConst)) {
                continue;
            }
            if (!(curr.params[1] instanceof VarInfo)) {
                continue;
            }
            VarInfo valSet = (VarInfo) curr.params[1];
            if (valSet.getName().contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX)) {
                continue;
            }
            X86Param currSourceName = curr.params[0];
            if (currSourceName instanceof VarInfo && ((VarInfo) currSourceName).getName().contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX)) {
                continue;
            }
            if (currSourceName.equals(valSet)) {
                //replacement wouldn't... even do anything
                continue;
            }
            X86Param currSource = curr.params[0];
            if (currSource instanceof VarInfo) {
                currSource = ((VarInfo) currSource).typed(curr.params[1].getType());
                //VarInfo vi = (VarInfo) currSource;
                //currSource = vi.getContext().new VarInfo(vi.getName(), curr.params[1].getType(), vi.getStackLocation());
                //vi.getContext().printFull = true;
                //System.out.println("Replaced " + valSet + " for " + vi + " with " + currSource + " in " + curr);
                //continue;
            } else {
                if (curr instanceof TACCast) {
                    throw new IllegalStateException(curr + "");
                }
                /*if (currSource instanceof X86Const) {
                    currSource = new X86Const(((X86Const) currSource).getValue(), (TypeNumerical) curr.params[1].getType());
                }*/
                if (currSource instanceof X86Const) {
                    //continue;
                } else {
                    throw new IllegalStateException(currSource + "");
                }
            }
            int st = ind + 1;
            boolean tempVar = isTempVariable(valSet.getName());
            if (!tempVar) {
                if (!AGGRESSIVE) {
                    continue;
                }
                block.add(ind, null);//<horrible hack>
                st++;
            }
            for (int usageLocation = st; usageLocation < block.size(); usageLocation++) {
                TACStatement next = block.get(usageLocation);
                //if (curr.params[1] != null && curr.params[1].getType() instanceof TypeStruct) {
                //break;
                //this break used to be necesary to make the tests suceed, but I just commented it out and it doesn't make anything fail
                //go figure
                //leaving it here but commented out if it comes up in the future
                //}
                if (next instanceof TACJumpBoolVar && next.requiredVariables().contains(valSet) && tempVar) {
                    throw new RuntimeException("This won't happen as of the current TAC generation of boolean statements " + next + " " + curr);//but if i change things in the future this could happen and isn't a serious error
                }
                boolean exemption = next instanceof TACCast && !(currSource instanceof VarInfo);
                if (!exemption && next.requiredVariables().contains(valSet)) {
                    next.replace(valSet, currSource);
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
                    //it means that a temp variable is set, then goes unused up to a jump
                    //this is actually ok
                    //it arises in cases like
                    //if somecondition jump to tmp=1
                    //tmp=0
                    //jump past tmp=1
                    //tmp=1
                    break;
                }
            }
            while (block.contains(null)) {//</horrible hack>
                block.remove(null);
            }
        }
    }
}
