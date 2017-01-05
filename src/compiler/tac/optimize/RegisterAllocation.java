/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACCast;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACPointerDeref;
import compiler.tac.TACStandard;
import compiler.tac.TACStatement;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;
import compiler.x86.X86Register;
import static compiler.x86.X86Register.*;
import compiler.x86.X86TypedRegister;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class RegisterAllocation extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        //requirements for these registers:
        //shouldn't be any of the syscall registers
        //if they are a syscall register, lastUsage cannot be a function call to syscall
        /*X86Register.A,
        X86Register.DI,
        X86Register.SI,
        X86Register.D,
        X86Register.R10,
        X86Register.R8,
        X86Register.R9*/
        //can't be any of the return registers (which are currently A, C, and D)
        //
        //in case you were wondering, there really is no rhyme or reason to these choices for maxDistance and register
        allocate(block, 1, X86Register.B);
        allocate(block, 2, X86Register.R8);
        allocate(block, 3, X86Register.R9);
        for (X86Register r : new X86Register[]{R10, R11, R13, R14, R15}) {
            allocate(block, -1, r);
        }
    }
    private void allocate(List<TACStatement> block, int maxDistance, X86Register register) {
        if (TACFunctionCall.RETURN_REGISTERS.contains(register) || TACFunctionCall.SYSCALL_REGISTERS.contains(register)) {//this dynamic check is here in case i add a return / syscall register and forget
            throw new IllegalStateException(register + "");
        }
        wew:
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACStandard || block.get(i) instanceof TACCast || block.get(i) instanceof TACPointerDeref || block.get(i) instanceof TACFunctionCall) {
                List<String> modVars = block.get(i).modifiedVariables();
                if (modVars.size() != 1) {
                    if (block.get(i) instanceof TACFunctionCall) {
                        continue;//lets leave multiple returns alone for now
                    }
                    throw new RuntimeException();
                }
                String mod = modVars.get(0);
                if (!UselessTempVars.isTempVariable(mod)) {
                    continue;
                }
                Type lmao = block.get(i).modifiedVariableInfos().get(0).getType();
                if (!(lmao instanceof TypeNumerical)) {
                    continue;
                }
                if (lmao instanceof TypeFloat) {
                    continue;
                }
                int lastUsage = lastUsage(block, mod);
                if (lastUsage <= i) {
                    throw new RuntimeException(block + "");
                }
                /*if (block.get(i) instanceof TACCast) {
                    System.out.println(mod + "  " + (lastUsage - i) + " last usage " + block.get(lastUsage) + " setting " + block.get(i));
                }*/
                for (int j = i + 1; j < lastUsage; j++) {//i itself can't be a function call, and it doesn't matter if lastUsage is
                    if (block.get(j) instanceof TACFunctionCall) {//TODO it may possibly be OK for there to be a function in the middle if it's a syscall, depends on which registers it clobbers
                        continue wew;
                    }
                }
                if (lastUsage - i <= maxDistance || maxDistance == -1) {
                    /*if (maxDistance != 1) {
                        System.out.println("REPLACING " + maxDistance + " " + register);
                    }*/
                    //ok
                    X86TypedRegister xtr = register.getRegister((TypeNumerical) lmao);
                    //System.out.println("REPALCE " + block);
                    for (int j = i; j <= lastUsage; j++) {
                        if (block.get(j).modifiedVariables().contains(mod) || block.get(j).requiredVariables().contains(mod)) {
                            block.get(j).replace(mod, xtr.x86(), xtr);
                        }
                    }
                    i = lastUsage - 1;//-1 because i++
                    //setting i to lastUsage-1 ensures that two overlapping sections won't use the same register
                }
            }
        }
    }
    private int lastUsage(List<TACStatement> block, String varName) {
        for (int i = block.size() - 1; i >= 0; i--) {
            if (block.get(i).requiredVariables().contains(varName)) {
                return i;
            }
        }
        return -1;
    }
}
