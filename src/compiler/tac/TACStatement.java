/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
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
    public X86Param[] params;
    public TACStatement(X86Param... params) {
        this.params = params;
    }
    public void regReplace(X86Param toReplace, X86Register replaceWith) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals(toReplace)) {
                X86TypedRegister xtr = new X86TempRegister(replaceWith, (TypeNumerical) params[i].getType(), toReplace.toString());
                params[i] = xtr;
            }
        }
    }
    public void replace(X86Param toReplace, X86Param infoWith) {
        if (infoWith == null || toReplace == null) {
            throw new IllegalStateException(this + " " + toReplace + " " + infoWith);
        }
        if (infoWith.toString().contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX) || toReplace.toString().contains(TempVarUsage.TEMP_STRUCT_FIELD_INFIX)) {
            throw new IllegalStateException("REPLACING " + this + " " + toReplace + " " + infoWith);
        }
        boolean f = false;
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals(toReplace)) {
                params[i] = infoWith;
                if (infoWith instanceof X86Const && !infoWith.getType().equals(params[i].getType())) {
                    params[i] = new X86Const(((X86Const) infoWith).getValue(), (TypeNumerical) params[i].getType());
                } else {
                    params[i] = infoWith;
                }
                f = true;
            }
        }
        if (!f) {
            throw new IllegalStateException(toReplace + " not found in " + Arrays.asList(params) + " " + modifiedVariables() + " " + requiredVariables() + " " + Arrays.asList(params) + " " + this);
        }
    }
    public String toString(boolean printFull) {
        for (X86Param param : params) {
            if (param instanceof VarInfo) {
                ((VarInfo) param).getContext().printFull = printFull;
            }
        }
        return toString();
    }
    @Override
    public abstract String toString();
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
    public boolean usesDRegister() {
        return false;
    }
}
