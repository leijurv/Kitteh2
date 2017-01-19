/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACStatement;
import compiler.x86.RegAllocation;
import compiler.x86.X86Register;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TmpAllocation extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        //in case you were wondering, there really is no rhyme or reason to these choices for maxDistance and register
        RegAllocation.allocate(block, 1, X86Register.A, false, true, null);
        RegAllocation.allocate(block, -1, X86Register.D, false, true, null);
    }
}
