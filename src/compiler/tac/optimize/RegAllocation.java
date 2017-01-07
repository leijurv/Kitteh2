/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.Context.VarInfo;
import compiler.tac.TACCast;
import compiler.tac.TACConst;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACJump;
import compiler.tac.TACPointerDeref;
import compiler.tac.TACStandard;
import compiler.tac.TACStatement;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import compiler.x86.X86TempRegister;
import compiler.x86.X86TypedRegister;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class RegAllocation {
    static void allocate(List<TACStatement> block, int maxDistance, X86Register register, boolean allowNormal, boolean allowTemp) {
        if (TACFunctionCall.RETURN_REGISTERS.contains(register) || TACFunctionCall.SYSCALL_REGISTERS.contains(register)) {
            //this dynamic check is here in case i add a return / syscall register and forget
            throw new IllegalStateException(register + "");
        }
        HashSet<String> encountered = new HashSet<>();
        wew:
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACStandard || block.get(i) instanceof TACCast || block.get(i) instanceof TACPointerDeref || block.get(i) instanceof TACFunctionCall || block.get(i) instanceof TACConst) {
                List<String> modVars = block.get(i).modifiedVariables();
                if (modVars.size() != 1) {
                    if (block.get(i) instanceof TACFunctionCall) {
                        encountered.addAll(modVars);
                        continue; //lets leave multiple returns alone for now
                    }
                    throw new RuntimeException();
                }
                String mod = modVars.get(0);
                if (encountered.contains(mod)) {
                    continue;
                }
                if (mod.contains("%")) {
                    continue;
                }
                encountered.add(mod);
                boolean isTemp = UselessTempVars.isTempVariable(mod);
                if (isTemp && !allowTemp) {
                    continue;
                }
                if (!isTemp && !allowNormal) {
                    continue;
                }
                X86Param vf = block.get(i).modifiedVariableInfos().get(0);
                if (!(vf instanceof VarInfo)) {
                    continue;
                }
                if (((VarInfo) vf).getStackLocation() > 0) {
                    continue;
                }
                Type lmao = vf.getType();
                if (!(lmao instanceof TypeNumerical)) {
                    continue;
                }
                if (lmao instanceof TypeFloat) {
                    continue;
                }
                int lastUsage = lastUsage(block, mod);
                if (lastUsage <= i) {
                    if (!isTemp) {
                        continue;
                    }
                    throw new RuntimeException(block + mod);
                }
                /*if (block.get(i) instanceof TACCast) {
                System.out.println(mod + "  " + (lastUsage - i) + " last usage " + block.get(lastUsage) + " setting " + block.get(i));
                }*/
                boolean bc = false;
                for (int j = i + 1; j < lastUsage; j++) {
                    //i itself can't be a function call, and it doesn't matter if lastUsage is
                    if (block.get(j) instanceof TACFunctionCall) {
                        TACFunctionCall tfc = (TACFunctionCall) block.get(j);
                        if (tfc.calling().equals("syscall") && register != X86Register.R11 && register != X86Register.C) {
                            //we already know that register isn't one of the syscall arg registers
                            //so just make sure it isn't R11 or C because syscall clobbers RCX and R11 of all registers for some ungodly reason
                            bc = true;
                            continue;
                        }
                        if ((tfc.calling().equals("malloc") || tfc.calling().equals("free")) && (register == X86Register.B || register == X86Register.R12 || register == X86Register.R13 || register == X86Register.R14 || register == X86Register.R15)) {
                            //malloc and free follow the ABI (unlike kitteh2 ahem)
                            //so they preserve B and R12 through R15
                            bc = true;
                            //if (!allowNonTemp) {//idk why
                            continue;
                            //}
                        }
                        continue wew;
                    }
                }
                if (lastUsage - i <= maxDistance || maxDistance == -1) {
                    if (!isTemp) {
                        if (externalJumps(block, i, lastUsage)) {
                            /* System.out.println(mod);
                            System.out.println(block);
                            System.out.println(i);
                            System.out.println(lastUsage);
                            System.out.println(block.subList(i, lastUsage + 1));*/
                            while (lastUsage < block.size()) {
                                if (block.get(lastUsage) instanceof TACFunctionCall) {
                                    lastUsage--;
                                    break;
                                }
                                if (!externalJumps(block, i, lastUsage)) {
                                    //System.out.println("Expanded to allow");
                                    break;
                                }
                                lastUsage++;
                            }
                            if (lastUsage == block.size()) {
                                continue;
                            }
                        }
                        if (externalJumps(block, i, lastUsage)) {
                            continue;
                        }
                        //System.out.println("Allowing " + mod + " " + i + " " + lastUsage + " " + register + " " + bc);
                    }
                    /*if (maxDistance != 1) {
                    System.out.println("REPLACING " + maxDistance + " " + register);
                    }*/
                    //ok
                    X86TypedRegister xtr = new X86TempRegister(register, (TypeNumerical) lmao, mod);
                    //System.out.println("REPALCE " + block);
                    for (int j = i; j <= lastUsage; j++) {
                        if (block.get(j).modifiedVariables().contains(mod) || block.get(j).requiredVariables().contains(mod)) {
                            block.get(j).replace(mod, xtr.x86(), xtr);
                        }
                    }
                    if (lastUsage > i) {
                        i = lastUsage - 1; //-1 because i++
                    }                    //setting i to lastUsage-1 ensures that two overlapping sections won't use the same register
                }
            } else {
                encountered.addAll(block.get(i).modifiedVariables());
            }
        }
    }
    private static int lastUsage(List<TACStatement> block, String varName) {
        for (int i = block.size() - 1; i >= 0; i--) {
            if (block.get(i).requiredVariables().contains(varName)) {
                return i;
            }
        }
        return -1;
    }
    private static boolean externalJumps(List<TACStatement> block, int start, int end) {
        for (int k = 0; k < block.size(); k++) {
            if (block.get(k) instanceof TACJump) {
                TACJump tj = (TACJump) block.get(k);
                int dest = tj.jumpTo();
                int i = k;
                if (i >= start && i <= end && dest >= start && dest <= end) {//internal jumps are A-ok
                    continue;
                }
                //jumps to before the region are OK because the region begins with resetting the value
                if (dest <= start) {
                    continue;
                }
                if (dest <= end && i >= start) {
                    //return true;
                }
                if (dest < i) {
                    //return true;
                }
                boolean outside = i < start || i > end + 1;//TODO make sure that this +1 doesn't cause weird behavior when lastUsage is expanding
                boolean inside = i >= start && i <= end;//last statement can be a jump
                if (dest >= start && dest <= end) {
                    // return true;
                }
                if (inside) {
                    // return true;
                }
                if (dest > start && dest <= end && outside) {
                    //jumps to the internal region, after the setting and before or at the usage, from outside, are not ok
                    return true;
                }
                if ((dest < start || dest > end) && inside) {
                    //System.out.println("Jump from " + i + " to " + dest + " where start is " + start + " and end is " + end);
                    //return true;
                }
            }
        }
        return false;
    }
}
