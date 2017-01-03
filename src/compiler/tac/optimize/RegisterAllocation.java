/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACCast;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACStandard;
import compiler.tac.TACStatement;
import compiler.type.Type;
import compiler.type.TypeNumerical;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class RegisterAllocation extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        wew:
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i) instanceof TACStandard || block.get(i) instanceof TACCast) {
                List<String> modVars = block.get(i).modifiedVariables();
                if (modVars.size() != 1) {
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
                int lastUsage = lastUsage(block, mod);
                if (lastUsage <= i) {
                    throw new RuntimeException(block + "");
                }
                //System.out.println(mod + "  " + (lastUsage - i) + " last usage " + block.get(lastUsage) + " setting " + block.get(i));
                for (int j = i + 1; j < lastUsage; j++) {
                    if (block.get(j) instanceof TACFunctionCall) {
                        continue wew;
                    }
                }
                if (lastUsage - i == 1) {
                    //ok
                    X86TypedRegister xtr = X86Register.B.getRegister((TypeNumerical) lmao);
                    System.out.println("REPALCE " + block);
                    for (int j = i; j <= lastUsage; j++) {
                        block.get(j).replace(mod, xtr.x86(), xtr);
                    }
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
