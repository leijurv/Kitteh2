/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.X86Emitter;

/**
 *
 * @author leijurv
 */
public class TACConst extends TACStatement {
    String varName;
    VarInfo var;
    String val;
    VarInfo vall;
    public TACConst(String var, String val) {
        this.varName = var;
        this.val = val;
    }
    @Override
    public String toString0() {
        return (var == null ? varName : var) + " = " + (vall != null ? vall : "CONST " + val);
    }
    @Override
    public void onContextKnown() {
        var = varName.startsWith("%") ? null : context.getRequired(varName);
        try {//im tired ok? i know this is mal
            Double.parseDouble(val);
        } catch (NumberFormatException ex) {
            if (!val.startsWith("\"")) {
                vall = context.get(val);
                if (vall == null) {
                    throw new IllegalStateException("I honestly can't think of a way that this could happen. but idk it might");
                }
            }
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        String wew = varName.startsWith("%") ? varName : var.x86();
        if (vall == null) {
            emit.addStatement("movl $" + val + ", " + wew);
        } else {
            if (varName.startsWith("%")) {
                emit.addStatement("movl " + vall.x86() + ", " + varName);
            } else {
                emit.addStatement("movl " + vall.x86() + ", %ebx");
                emit.addStatement("movl %ebx, " + wew);
            }
        }
    }
}
