/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.X86Emitter;
import compiler.X86Register;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeNumerical;

/**
 *
 * @author leijurv
 */
public class TACConst extends TACStatement {
    public final String varName;
    public VarInfo var;
    public final String val;
    public VarInfo vall;
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
        move(varName, var, vall, val, emit);
    }
    public static void move(String varName, VarInfo var, VarInfo vall, String val, X86Emitter emit) {
        String wew = varName.startsWith("%") ? varName : var.x86();
        TypeNumerical type = varName.startsWith("%") ? typeFromRegister(varName) : (TypeNumerical) var.getType();
        if (vall == null) {
            emit.addStatement("mov" + type.x86typesuffix() + " $" + val + ", " + wew);
        } else if (varName.startsWith("%")) {
            emit.addStatement("mov" + type.x86typesuffix() + " " + vall.x86() + ", " + varName);
        } else {
            emit.addStatement("mov" + type.x86typesuffix() + " " + vall.x86() + ", " + X86Register.B.getRegister(type));
            emit.addStatement("mov" + type.x86typesuffix() + " " + X86Register.B.getRegister(type) + ", " + wew);
        }
    }
    public static TypeNumerical typeFromRegister(String reg) {
        if (reg.startsWith("%")) {
            return typeFromRegister(reg.substring(1));
        }
        if (reg.length() == 2) {
            return new TypeInt16();
        }
        switch (reg.charAt(0)) {
            case 'e':
                return new TypeInt32();
            case 'r':
                return new TypeInt64();
            default:
                throw new IllegalStateException();
        }
    }
}
