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
        if (TACFunctionCall.RETURN_REGISTERS.contains(register)) {
            //this dynamic check is here in case i add a return / syscall register and forget
            throw new IllegalStateException(register + "");
        }
        HashSet<String> encountered = new HashSet<>();
        https://en.wikipedia.org/wiki/Register_allocation
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACStandard || block.get(i) instanceof TACCast || block.get(i) instanceof TACPointerDeref || block.get(i) instanceof TACFunctionCall || block.get(i) instanceof TACConst) {
                List<String> modVars = block.get(i).modifiedVariables();
                if (modVars.size() != 1) {
                    if (block.get(i) instanceof TACFunctionCall) {
                        encountered.addAll(modVars);//all are being set
                        continue; //lets leave multiple returns alone for now
                    }
                    throw new RuntimeException();
                }
                String mod = modVars.get(0);
                if (encountered.contains(mod)) {//not our first time seeing this variable, and previous passes failed
                    continue;
                }
                if (mod.contains(X86Register.REGISTER_PREFIX)) {//we've already got this one =D
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
                if (!(vf instanceof VarInfo)) {//idk
                    throw new IllegalStateException(vf.getClass() + " " + vf);
                }
                if (((VarInfo) vf).getStackLocation() > 0) {// an argument
                    //TODO if we can replace an argument with a register, do so, and make all calling functions go along
                    //will work, because function calls to other kitteh functions don't assume anything about register preservation
                    continue;
                }
                Type lmao = vf.getType();
                if (!(lmao instanceof TypeNumerical)) {//a struct or something idk
                    continue;
                }
                if (lmao instanceof TypeFloat) {//that can only be on the XMM registers
                    continue;
                }
                int lastUsage = lastUsage(block, mod);
                if (lastUsage <= i) {
                    if (!isTemp) {
                        continue;
                    }
                    throw new RuntimeException(block + mod);//last usage of a TEMP VARIABLE is BEFORE it was set first?????
                }
                /*if (block.get(i) instanceof TACCast) {
                System.out.println(mod + "  " + (lastUsage - i) + " last usage " + block.get(lastUsage) + " setting " + block.get(i));
                }*/
                boolean bc = false;
                for (int j = i + 1; j < lastUsage; j++) {
                    //it doesn't matter if i or lastUsage is a function call
                    if (block.get(j) instanceof TACFunctionCall) {
                        TACFunctionCall tfc = (TACFunctionCall) block.get(j);
                        if (tfc.calling().equals("syscall")) {
                            if (register == X86Register.R11 || register == X86Register.C) {//syscall clobbers RCX and R11 of all registers for some ungodly reason
                                continue https;
                            }
                            List<X86Register> args = TACFunctionCall.SYSCALL_REGISTERS.subList(0, tfc.argsSize());//if this syscall only uses 1 argument register, the rest are actually ok to use
                            if (args.contains(register)) {//just make sure this register isn't one of the ones this syscall is using
                                continue https;
                            }
                            bc = true;
                            continue;
                        }
                        if ((tfc.calling().equals("malloc") || tfc.calling().equals("free")) && (register == X86Register.B || register == X86Register.R12 || register == X86Register.R13 || register == X86Register.R14 || register == X86Register.R15)) {
                            //malloc and free follow the ABI (unlike kitteh2 ahem)
                            //so they preserve B and R12 through R15
                            bc = true;
                            continue;
                        }
                        continue https;
                    }
                }
                if (block.get(lastUsage) instanceof TACFunctionCall) {
                    TACFunctionCall tfc = (TACFunctionCall) block.get(lastUsage);
                    if (tfc.calling().equals("syscall") && TACFunctionCall.SYSCALL_REGISTERS.contains(register)) {
                        //TODO check which argument it is. if we're considering replacing with RDI, and it's about to be passed *as RDI*, that's fine lol
                        continue;
                    }
                    if ((tfc.calling().equals("malloc") || tfc.calling().equals("free")) && register == X86Register.DI) {//RDI passes argument to free and malloc
                        //TODO this may not be necesary
                        //the last usage is being passed to malloc / free, and it's *already in* rdi
                        //movslq to the same register is actually allowed, so no worries there
                        //otherwise, it's fine to use rdi (since it's already the argument for its last usage)
                        continue;
                    }
                }
                //TODO print of a float clobbers A register
                if (lastUsage - i <= maxDistance || maxDistance == -1) {
                    if (!isTemp) {
                        if (externalJumps(block, i, lastUsage)) {
                            /* System.out.println(mod);
                            System.out.println(block);
                            System.out.println(i);
                            System.out.println(lastUsage);
                            System.out.println(block.subList(i, lastUsage + 1));*/
                            while (true) {
                                lastUsage++;
                                if (lastUsage >= block.size() || block.get(lastUsage) instanceof TACFunctionCall) {//yes, there are NO function calls of any kind allowed in the extension
                                    continue https;
                                }
                                if (!externalJumps(block, i, lastUsage)) {
                                    break;
                                }
                            }
                            if (compiler.Compiler.verbose()) {
                                System.out.println("Expanded to allow");
                            }
                            //only way to get to here is if the break in no external jumps fires
                        }
                        if (compiler.Compiler.verbose()) {
                            System.out.println("Allowing " + mod + " " + i + " " + lastUsage + " " + register + " " + bc);
                        }
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
                    if (lastUsage > i) {//respect the extension, because it assumes that this register will remain unchanged for the extended section
                        i = lastUsage - 1; //-1 because i++
                    }                    //setting i to lastUsage-1 ensures that two overlapping sections won't use the same register
                }
            } else {
                encountered.addAll(block.get(i).modifiedVariables());//don't miss any sets not covered in the if
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
                boolean outside = i < start || i > end + 1;//TODO make sure that this +1 doesn't cause weird behavior when lastUsage is expanding
                if (dest > start && dest <= end && outside) {
                    //jumps to the internal region, after the setting and before or at the usage, from outside, are not ok
                    return true;
                }
            }
        }
        return false;
    }
}
