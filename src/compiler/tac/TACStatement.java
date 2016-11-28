/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.x86.X86Emitter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public abstract class TACStatement {
    public Context context;
    public TempVarUsage tvu;
    public String[] paramNames;
    public VarInfo[] params;
    public TACStatement() {
        paramNames = new String[0];
        params = new VarInfo[0];
    }
    public TACStatement(String... paramNames) {
        this.paramNames = paramNames;
        params = new VarInfo[paramNames.length];
    }
    public final void setContext(Context context) {
        this.context = context;
        this.tvu = context.getTempVarUsage();//copy this because it's gonna be reset later
        setVars();
        onContextKnown();
    }
    public void setVars() {
        for (int i = 0; i < paramNames.length; i++) {
            params[i] = get(paramNames[i]);
        }
    }
    public final void replace(String toReplace, String replaceWith, VarInfo infoWith) {
        if (infoWith == null) {
            System.out.println("REPLACE " + toReplace + " " + replaceWith + " " + infoWith);
        }
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(toReplace)) {
                paramNames[i] = replaceWith;
                params[i] = infoWith;
                return;
            }
        }
        throw new IllegalStateException(toReplace + " not found in " + Arrays.asList(paramNames));
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
    protected VarInfo get(String name) {
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
