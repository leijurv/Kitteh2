/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac.optimize;
import compiler.command.CommandDefineFunction;
import compiler.tac.TACStatement;
import compiler.util.Pair;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class OptimizationSettings {
    public static final OptimizationSettings ALL = new OptimizationSettings(true, true);
    public static final OptimizationSettings NONE = new OptimizationSettings(false, false);
    private final boolean[] enabled = new boolean[TACOptimizer.opt.size()];
    private final boolean staticValues;
    public OptimizationSettings(boolean tac, boolean staticValues) {
        if (tac) {
            Arrays.fill(enabled, true);
        }
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
    public Pair<String, List<TACStatement>> coloncolon(CommandDefineFunction com) {//i love using :: syntax...
        return new Pair<>(com.getHeader().name, com.totac(this));
    }
}
