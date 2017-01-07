/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACStatement;
import compiler.x86.X86Register;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TmpAllocation extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {//TODO store the names of temp variables before they are replaced with registers for verbose / debug
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
        RegAllocation.allocate(block, 1, X86Register.R8, false, true);
        RegAllocation.allocate(block, -1, X86Register.R9, false, true);
        RegAllocation.allocate(block, -1, X86Register.R12, false, true);
    }
}
