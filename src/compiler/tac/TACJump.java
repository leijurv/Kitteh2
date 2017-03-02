/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACJump extends TACStatement {
    protected int jumpTo;
    public TACJump(int jumpTo, X86Param... paramNames) {
        super(paramNames);
        this.jumpTo = jumpTo;
    }
    @Override
    public String toString() {
        return "jmp " + jumpTo;
    }
    @Override
    public void printx86(X86Emitter emit) {
        emit.addStatement("jmp " + emit.lineToLabel(jumpTo));
    }
    public int jumpTo() {
        return jumpTo;
    }
    public void setJumpTo(int n) {
        jumpTo = n;
    }
    @Override
    public List<X86Param> requiredVariables() {
        return Arrays.asList();
    }
    @Override
    public final List<X86Param> modifiedVariables() {//a jump definitely can't modify anything, so final
        return Arrays.asList();
    }
}
