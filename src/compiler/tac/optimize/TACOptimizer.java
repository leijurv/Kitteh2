/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.Compiler;
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
    private TACOptimizer() {
    }
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
            ConditionalDoubleJump.class,
            KnownConditions.class
    ));
    transient final static private int[] usefulnessCount = new int[opt.size()];
    static volatile private int count = 0;
    public static List<TACStatement> optimize(IREmitter emitted, OptimizationSettings settings) {
        List<TACStatement> input = emitted.getResult();
        List<TACStatement> prev;
        //int num = 0;
        boolean[] didAnything = new boolean[opt.size()];
        boolean metrics = Compiler.metrics();
        do {
            prev = new ArrayList<>(input);
            for (Class<? extends TACOptimization> optim : opt) {
                if (settings.run(optim)) {
                    String before = metrics ? input + "" : "";
                    try {
                        input = optim.newInstance().go(input);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException("idk man", e);
                    }
                    if (metrics) {
                        didAnything[opt.indexOf(optim)] |= !before.equals(input + "");
                    }
                    /*System.out.println(optim);
                    for (int i = 0; i < input.size(); i++) {
                        System.out.println(i + ":   " + input.get(i).toString(true));
                    }
                    System.out.println();*/
                }
            }
            //System.out.println("Pass " + (++num) + ". Prev num statements: " + prev.size() + " Current num statements: " + input.size());
        } while (!prev.equals(input));
        if (metrics && settings.run(opt.get(0))) {
            for (int i = 0; i < didAnything.length; i++) {
                System.out.println(opt.get(i) + " " + didAnything[i]);
                if (didAnything[i]) {
                    usefulnessCount[i]++;
                }
            }
            count++;
            for (int i = 0; i < didAnything.length; i++) {
                double lol = (double) usefulnessCount[i] / (double) count;
                System.out.println(opt.get(i) + " " + usefulnessCount[i] + "/" + count + " " + lol);
            }
        }
        if (settings.staticValues()) {//TODO better flag for these final register optimizations
            //input = new TmpAllocation().go(input);
        }
        return input;
    }
}
