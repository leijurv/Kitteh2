/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.type.TypeBoolean;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACJumpBoolVar extends TACJump {
    public final boolean invert;
    public TACJumpBoolVar(String varName, int jumpTo, boolean invert) {
        super(jumpTo, varName);
        this.invert = invert;
    }
    @Override
    protected void onContextKnown() {
        if (!(params[0].getType() instanceof TypeBoolean)) {
            throw new IllegalStateException("There is laterally no way this could happen. But I guess it did. lolripyou");
        }
    }
    @Override
    public List<X86Param> requiredVariables() {
        return Arrays.asList(params[0]);
    }
    @Override
    public String toString0() {
        return "jump to " + jumpTo + " if " + (invert ? "not " : "") + params[0];
    }
    @Override
    public void printx86(X86Emitter emit) {
        emit.addStatement("cmpb $0, " + params[0].x86());
        if (invert) {
            emit.addStatement("je " + emit.lineToLabel(jumpTo));
        } else {
            emit.addStatement("jne " + emit.lineToLabel(jumpTo));
        }
    }
}
