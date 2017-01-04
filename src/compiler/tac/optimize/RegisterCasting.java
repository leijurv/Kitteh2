/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACCast;
import compiler.tac.TACStatement;
import compiler.x86.X86TypedRegister;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class RegisterCasting extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACCast) {
                TACCast tc = (TACCast) block.get(i);
                if (tc.params[0] instanceof X86TypedRegister && tc.params[1] instanceof X86TypedRegister && tc.paramNames[0].equals(tc.paramNames[1])) {
                    block.remove(i);
                    i--;
                }
            }
        }
    }
}
