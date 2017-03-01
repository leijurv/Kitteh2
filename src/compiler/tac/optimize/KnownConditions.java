/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.TACJump;
import compiler.tac.TACJumpBoolVar;
import compiler.tac.TACStatement;
import compiler.x86.X86Param;
import java.util.HashMap;
import java.util.List;

/**
 * jump if a
 *
 * ... (a not modified, no jump destinations)
 *
 * "jump if a" can be replaced with an equivalent unconditional jump, and "jump
 * if not a" can be removed.
 *
 * @author leijurv
 */
public class KnownConditions extends TACOptimization {
    @Override
    protected void run(List<TACStatement> block, int blockBegin) {
        HashMap<X86Param, Boolean> known = new HashMap<>();
        for (int i = 0; i < block.size(); i++) {
            for (X86Param str : block.get(i).modifiedVariables()) {
                known.remove(str);
            }
            if (block.get(i) instanceof TACJumpBoolVar) {
                TACJumpBoolVar tjbr = (TACJumpBoolVar) block.get(i);
                X86Param variable = tjbr.params[0];
                Boolean alreadyKnown = known.get(variable);
                if (alreadyKnown == null) {
                    boolean knownToBe = tjbr.invert;
                    //if invert is true, its a jump if not a, so a must be true for the jump to not fire
                    //if invert is false, its a jump if a, so a must be false for the jump to not fire
                    known.put(variable, knownToBe);
                } else {
                    boolean willJumpExecute = alreadyKnown ^ tjbr.invert;
                    //if known to be true, and invert is false (normal), jump will execute
                    if (willJumpExecute) {
                        TACJump repl = new TACJump(tjbr.jumpTo());//replace with unconditional jump to the same destination
                        repl.copyFrom(tjbr);
                        block.set(i, repl);
                    } else {
                        block.remove(i);//it won't happen, so just remove
                    }
                }
            }
        }
    }
}
