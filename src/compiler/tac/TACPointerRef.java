/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.x86.X86Const;
import compiler.x86.X86Emitter;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACPointerRef extends TACStatement {
    int offset;
    public TACPointerRef(String source, String dest, int offset) {
        super(source, dest);
        this.offset = offset;
    }
    @Override
    protected void onContextKnown() {
        if (!(params[1].getType() instanceof TypePointer)) {
            throw new IllegalStateException("what");
        }
        if (!((TypePointer) params[1].getType()).pointingTo().equals(params[0].getType())) {
            throw new IllegalStateException("what");
        }
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(paramNames);
    }
    @Override
    public List<String> modifiedVariables() {
        return Arrays.asList();
    }
    @Override
    public String toString0() {
        //return "Put the value " + source + " into the location specified by " + dest;
        return "*" + params[1] + " = " + params[0];
    }
    @Override
    public void printx86(X86Emitter emit) {
        if (params[0].getType() instanceof TypeNumerical) {
            TypeNumerical d = (TypeNumerical) ((TypePointer) params[1].getType()).pointingTo();
            String source;
            if (params[0] instanceof X86Const || params[0] instanceof X86TypedRegister) {
                source = params[0].x86();
            } else {
                source = X86Register.C.getRegister(d).x86();
                emit.addStatement("mov" + d.x86typesuffix() + " " + params[0].x86() + ", " + source);
            }
            String othersource;
            if (params[1] instanceof X86Const || params[1] instanceof X86TypedRegister) {
                othersource = params[1].x86();
            } else {
                othersource = X86Register.A.getRegister((TypeNumerical) params[1].getType()).x86();
                emit.addStatement("movq " + params[1].x86() + ", " + othersource);
            }
            String o = offset == 0 ? "" : "" + offset;
            emit.addStatement("mov" + d.x86typesuffix() + " " + source + ", " + o + "(" + othersource + ")");
        } else if (params[0].getType() instanceof TypeStruct) {
            TypeStruct ts = (TypeStruct) params[0].getType();
            emit.addStatement("movq " + params[1].x86() + ", %rax");
            TACPointerDeref.moveStruct(((Context.VarInfo) params[0]).getStackLocation(), "%rbp", offset, "%rax", ts, emit);
        } else {
            throw new InvalidPathException("", "");
        }
    }
}
