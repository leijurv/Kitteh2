/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.Context.VarInfo;
import compiler.tac.TACArrayDeref;
import compiler.tac.TACCast;
import compiler.tac.TACConst;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACJump;
import compiler.tac.TACPointerDeref;
import compiler.tac.TACStandard;
import compiler.tac.TACStatement;
import compiler.tac.TempVarUsage;
import compiler.tac.optimize.UselessTempVars;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class RegAllocation {
    public static void allocate(List<TACStatement> block, int maxDistance, X86Register register, boolean allowNormal, boolean allowTemp, X86Function in) {
        HashSet<String> encountered = new HashSet<>();
        HashSet<Integer> used = new HashSet<>();
        boolean mode = false;
        https://en.wikipedia.org/wiki/Register_allocation
        for (int i = 0; i < block.size(); i++) {//TODO use more efficient data flow analysis to decide which vars to registerify instead of greedily doing the first viable variable it sees
            if (block.get(i) instanceof TACStandard || block.get(i) instanceof TACCast || block.get(i) instanceof TACPointerDeref || block.get(i) instanceof TACFunctionCall || block.get(i) instanceof TACConst || block.get(i) instanceof TACArrayDeref) {
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
                if (mod.contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX)) {
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
                    if (!allowed(block, j, register, in, mode)) {
                        continue https;
                    }
                }
                if (register == X86Register.D && block.get(lastUsage).usesDRegister()) {
                    continue;
                }
                if (block.get(lastUsage) instanceof TACFunctionCall) {
                    TACFunctionCall tfc = (TACFunctionCall) block.get(lastUsage);
                    if (tfc.calling().equals("syscall") && TACFunctionCall.SYSCALL_REGISTERS.contains(register)) {
                        //TODO check which argument it is. if we're considering replacing with RDI, and it's about to be passed *as RDI*, that's fine lol
                        int fi = Arrays.asList(tfc.paramNames).indexOf(mod);
                        int li = Arrays.asList(tfc.paramNames).lastIndexOf(mod);
                        if (fi != li || fi == -1 || li == -1) {
                            throw new IllegalStateException(tfc + " " + fi + " " + li + " " + mod);
                        }
                        if (register != TACFunctionCall.SYSCALL_REGISTERS.get(fi)) {
                            return;
                        }
                        throw new IllegalStateException("ALLOWING " + register + " " + mod + " " + tfc);//if this ever happens, throw an exception because i want to notice it and be happy
                    }
                    if ((tfc.calling().equals("malloc") || tfc.calling().equals("free")) && register == X86Register.DI) {//RDI passes argument to free and malloc
                        //the last usage is being passed to malloc / free, and it's *already in* rdi
                        //movslq to the same register is actually allowed, so no worries there
                        //otherwise, it's fine to use rdi (since it's already the argument for its last usage)
                        System.out.print(compiler.Compiler.verbose() ? "Allocating up to malloc / free " + mod : "");
                    }
                }
                //TODO print of a float clobbers A register
                if (lastUsage - i <= maxDistance || maxDistance == -1) {
                    if (!isTemp) {//TODO should extend be allowed on temp variables if allowNormal is true?
                        if (externalJumps(block, i, lastUsage)) {
                            if (!mode) {
                                continue;
                            }
                            /* System.out.println(mod);
                            System.out.println(block);
                            System.out.println(i);
                            System.out.println(lastUsage);
                            System.out.println(block.subList(i, lastUsage + 1));*/
                            while (true) {//TODO this is greedy
                                if (lastUsage >= block.size() || !allowed(block, lastUsage, register, in, mode)) {
                                    continue https;
                                }
                                if (!externalJumps(block, i, lastUsage)) {
                                    break;
                                }
                                lastUsage++;
                            }
                            if (lastUsage - i > maxDistance && maxDistance != -1) {
                                continue;
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
                    for (int j = i + 1; j <= lastUsage; j++) {
                        if (used.contains(j)) {
                            if (!mode) {
                                throw new IllegalStateException();
                            }
                            continue https;
                        }
                    }
                    if (in != null) {
                        in.used.add(register);
                    }
                    if (mode && compiler.Compiler.verbose()) {
                        System.out.println("Allocating because of cross-call permit " + mod);
                    }
                    //System.out.println("REPALCE " + block);
                    for (int j = i; j <= lastUsage; j++) {
                        used.add(j);
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
            if (i >= block.size() - 1 && !mode && in != null) {
                i = -1;
                mode = true;
                encountered.clear();
            }
        }
    }
    private static boolean allowed(List<TACStatement> block, int j, X86Register register, X86Function in, boolean mode) {
        if (register == X86Register.D && block.get(j).usesDRegister()) {
            return false;
        }
        //it doesn't matter if i or lastUsage is a function call
        if (block.get(j) instanceof TACFunctionCall) {
            TACFunctionCall tfc = (TACFunctionCall) block.get(j);
            String calling = tfc.calling();
            if (calling.equals("syscall")) {
                if (register == X86Register.R11 || register == X86Register.C) {//syscall clobbers RCX and R11 of all registers for some ungodly reason
                    return false;
                }
                List<X86Register> args = TACFunctionCall.SYSCALL_REGISTERS.subList(0, tfc.numArgs());//if this syscall only uses 1 argument register, the rest are actually ok to use
                if (args.contains(register)) {//just make sure this register isn't one of the ones this syscall is using
                    return false;
                }
                return true;
            }
            if ((tfc.calling().equals("malloc") || tfc.calling().equals("free")) && (register == X86Register.B || register == X86Register.R12 || register == X86Register.R13 || register == X86Register.R14 || register == X86Register.R15)) {
                //malloc and free follow the ABI (unlike kitteh2 ahem)
                //so they preserve B and R12 through R15
                return true;
            }
            if (in != null) {
                X86Function call = in.map.get(calling);
                if (call == null || call == in) {
                    return false;
                }
                if (call.allDescendants().contains(in)) {
                    return false;
                }
                if (!call.allocated) {
                    throw new IllegalStateException(in + " calls " + call);//if we depend on something that couldn't lead back to me yet is unallocated, that's bad
                }
                if (!call.allUsed().contains(register) && mode) {
                    if (compiler.Compiler.verbose()) {
                        System.out.println("CONSIDERING permitting " + register + " across call to " + call + " which only uses " + call.allUsed());
                    }
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    private static int lastUsage(List<TACStatement> block, String varName) {
        for (int i = block.size() - 1; i >= 0; i--) {
            if (block.get(i).requiredVariables().contains(varName) || block.get(i).modifiedVariables().contains(varName)) {
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
    public static void allocate(List<X86Function> fns) {
        while (true) {
            List<X86Function> ta = fns.stream().filter(X86Function::canAllocate).collect(Collectors.toList());
            if (compiler.Compiler.verbose()) {
                System.out.println("Allocating " + ta);
            }
            if (ta.isEmpty()) {
                return;
            }
            ta.parallelStream().forEach(X86Function::allocate);
        }
    }
}
