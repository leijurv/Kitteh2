/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.IREmitter;
import compiler.tac.TACStatement;
import java.util.ArrayList;

/**
 *
 * @author leijurv
 */
public class TACOptimizer {
    public static ArrayList<TACStatement> optimize(IREmitter emitted) {
        ArrayList<TACStatement> input = emitted.getResult();
        ArrayList<TACStatement> prev;
        int num = 0;
        final TACOptimization[] optimizations = {
            new UselessTempVars(),
            new RedundantCalculations(),
            new ConstantCasting(),
            new JumpOver(),
            new UnusedVariables(),
            new DeadCode(),
            new UnusedAssignment(),
            new DoubleJump()
        };
        do {
            prev = new ArrayList<>(input);
            for (TACOptimization optim : optimizations) {
                input = optim.go(input);
            }
            System.out.println("Pass " + (++num) + ". Prev num statements: " + prev.size() + " Current num statements: " + input.size());
        } while (!prev.equals(input));
        return input;
    }
}
