/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACStatement;
import compiler.x86.X86Register;
import static compiler.x86.X86Register.*;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class VarAllocation extends TACOptimization {
    @Override
    public List<TACStatement> go(List<TACStatement> stmts) {
        for (X86Register r : new X86Register[]{DI, R10, R9, R11, R8, SI, B, R12, R13, R14, R15}) {
            RegAllocation.allocate(stmts, -1, r, true, true);
        }
        return stmts;
    }
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
