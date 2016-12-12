/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.tac.IREmitter;
import compiler.tac.TACStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACOptimizer {
    public static final List<Class<? extends TACOptimization>> opt = Collections.unmodifiableList(Arrays.asList(
            UselessTempVars.class,
            RedundantCalculations.class,
            ConstantCasting.class,
            JumpOver.class,
            UnusedVariables.class,
            DeadCode.class,
            UnusedAssignment.class,
            DoubleJump.class,
            CommonSubexpression.class,
            SelfSet.class,
            AfterReturn.class,
            ConditionalDoubleJump.class
    ));
    public static List<TACStatement> optimize(IREmitter emitted, OptimizationSettings settings) {
        List<TACStatement> input = emitted.getResult();
        List<TACStatement> prev;
        //int num = 0;
        do {
            prev = new ArrayList<>(input);
            for (Class<? extends TACOptimization> optim : opt) {
                if (settings.run(optim)) {
                    try {
                        input = optim.newInstance().go(input);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException("idk man", e);
                    }
                    /*compiler.Context.printFull = false;//for debugging purposes
                    System.out.println(optim);
                    for (int i = 0; i < input.size(); i++) {
                        System.out.println(i + ":   " + input.get(i));
                    }
                    System.out.println();*/
                }
            }
            //System.out.println("Pass " + (++num) + ". Prev num statements: " + prev.size() + " Current num statements: " + input.size());
        } while (!prev.equals(input));
        return input;
    }
}
