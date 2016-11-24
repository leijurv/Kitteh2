/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import java.util.Arrays;

/**
 *
 * @author leijurv
 */
public class OptimizationSettings {
    private final boolean[] enabled = new boolean[TACOptimizer.opt.size()];
    private final boolean staticValues;
    public OptimizationSettings(boolean tac, boolean staticValues) {
        Arrays.fill(enabled, tac);
        this.staticValues = staticValues;
    }
    public boolean staticValues() {
        return staticValues;
    }
    public void setEnabled(int i, boolean b) {
        enabled[i] = b;
    }
    public boolean run(Class<? extends TACOptimization> o) {
        return enabled[TACOptimizer.opt.indexOf(o)];
    }
    public static final OptimizationSettings ALL = new OptimizationSettings(true, true);
    public static final OptimizationSettings NONE = new OptimizationSettings(false, false);
}
