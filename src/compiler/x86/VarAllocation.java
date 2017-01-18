/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.x86;
import compiler.tac.TACStatement;
import compiler.x86.RegAllocation;
import compiler.x86.X86Register;
import static compiler.x86.X86Register.*;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class VarAllocation {
    public List<TACStatement> go(List<TACStatement> stmts) {
        for (X86Register r : new X86Register[]{DI, R10, R9, R11, R8, SI, B, R12, R13, R14, R15}) {
            RegAllocation.allocate(stmts, -1, r, true, true);
        }
        return stmts;
    }
}
