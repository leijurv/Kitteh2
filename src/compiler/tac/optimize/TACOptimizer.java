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
        do {
            prev = new ArrayList<>(input);
            input = new UselessTempVars(input).go();
            input = new RedundantCalculations(input).go();
            input = new ConstantCasting(input).go();
            input = new JumpOver(input).go();
            input = new UnusedVariables(input).go();
            System.out.println("Pass " + (++num) + ". Prev num statements: " + prev.size() + " Current num statements: " + input.size());
        } while (!prev.equals(input));
        return input;
    }
}
