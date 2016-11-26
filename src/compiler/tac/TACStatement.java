/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.x86.X86Emitter;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public abstract class TACStatement {
    public Context context;
    public TempVarUsage tvu;
    public final void setContext(Context context) {
        this.context = context;
        this.tvu = context.getTempVarUsage();//copy this because it's gonna be reset later
        onContextKnown();
    }
    abstract protected void onContextKnown();
    @Override
    final public String toString() {//all calls to toString should check the context first, and this results in a single error message for every case where the context isn't already set
        if (context == null) {
            throw new IllegalStateException("Context not set " + getClass());
        }
        return toString0();
    }
    public abstract String toString0();
    public abstract void printx86(X86Emitter emit);
    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == getClass() && toString().equals(o.toString());
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    public abstract List<String> requiredVariables();
    public abstract List<String> modifiedVariables();
    public final List<VarInfo> modifiedVariableInfos() {
        return modifiedVariables().stream().filter(x -> !x.startsWith("%")).map(this::get).collect(Collectors.toList());
    }
    private VarInfo get(String name) {
        if (context.get(name) != null) {
            return context.get(name);
        }
        if (tvu == null) {
            throw new RuntimeException("Some optimization didn't copy over tvu...");
        }
        if (tvu.getInfo(name) != null) {
            return tvu.getInfo(name);
        }
        throw new RuntimeException("Neither " + context + " nor " + tvu + " have " + name);
    }
}
