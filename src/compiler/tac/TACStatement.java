/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.Context.VarInfo;
import compiler.type.TypeNumerical;
import compiler.x86.X86Const;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import compiler.x86.X86TempRegister;
import compiler.x86.X86TypedRegister;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public abstract class TACStatement {
    protected Context context;
    private TempVarUsage tvu;
    protected String[] paramNames;
    public X86Param[] params;
    public TACStatement(X86Param[] params) {
        this.paramNames = new String[params.length];
        this.params = params;
        this.context = null;
    }
    public TACStatement(String... paramNames) {
        this.paramNames = paramNames;
        params = new X86Param[paramNames.length];
        this.context = null;
    }
    public final void setContext(Context context) {
        this.context = context;
        if (context == null) {
            throw new IllegalArgumentException();
        }
        this.tvu = context.getTempVarUsage();//copy this because it's gonna be reset later
        setVars();
        onContextKnown();
    }
    public void copyFrom(TACStatement copyingFrom) {
        context = copyingFrom.context;
        tvu = copyingFrom.tvu;
    }
    public void setVars() {
        for (int i = 0; i < paramNames.length; i++) {
            params[i] = get(paramNames[i]);
            if (params[i] == null) {
                throw new NullPointerException(paramNames[i]);
            }
        }
    }
    public void regReplace(X86Param toReplace, X86Register replaceWith) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals(toReplace)) {
                X86TypedRegister xtr = new X86TempRegister(replaceWith, (TypeNumerical) params[i].getType(), toReplace.toString());
                params[i] = xtr;
                paramNames[i] = xtr.x86();
            }
        }
    }
    public void replace(X86Param toReplace, X86Param infoWith) {
        if (infoWith == null || toReplace == null) {
            throw new IllegalStateException(this + " " + toReplace + " " + infoWith);
        }
        if (infoWith.toString().contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX) || toReplace.toString().contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX)) {
            throw new RuntimeException("REPLACING " + this + " " + toReplace + " " + infoWith);
        }
        boolean f = false;
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals(toReplace)) {
                params[i] = infoWith;
                paramNames[i] = infoWith.toString();
                if (infoWith instanceof X86Const && !infoWith.getType().equals(params[i].getType())) {
                    params[i] = new X86Const(((X86Const) infoWith).getValue(), (TypeNumerical) params[i].getType());
                } else {
                    params[i] = infoWith;
                }
                f = true;
            }
        }
        if (!f) {
            throw new IllegalStateException(toReplace + " not found in " + Arrays.asList(paramNames) + " " + modifiedVariables() + " " + requiredVariables() + " " + Arrays.asList(params) + " " + this);
        }
    }
    abstract protected void onContextKnown();
    final public String toString(boolean printFull) {
        context.printFull = printFull;
        return toString();
    }
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
    public abstract List<X86Param> requiredVariables();
    public abstract List<X86Param> modifiedVariables();
    public final List<VarInfo> modifiedVarInfos() {
        return modifiedVariables().stream().filter(VarInfo.class::isInstance).map(VarInfo.class::cast).collect(Collectors.toList());
    }
    protected X86Param get(String name) {
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(name) && params[i] != null) {
                return params[i];
            }
        }
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
    protected X86Param otherget(String name) {
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
    public boolean usesDRegister() {
        return false;
    }
}
