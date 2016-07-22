/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.X86Emitter;

/**
 *
 * @author leijurv
 */
public class TACReturn extends TACStatement {
    @Override
    protected void onContextKnown() {
    }
    @Override
    public String toString0() {
        return "return";
    }
    @Override
    public void printx86(X86Emitter emit) {
        emit.addStatement("popq %rbp");
        emit.addStatement("retq");
    }
}
