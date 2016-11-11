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
public class TACCast extends TACStatement {
    String inputName;
    VarInfo input;
    String destName;
    VarInfo dest;
    public TACCast(String inputName, String dest) {
        this.inputName = inputName;
        this.destName = dest;
    }
    @Override
    protected void onContextKnown() {
        input = context.getRequired(inputName);
        dest = context.getRequired(destName);
    }
    @Override
    public String toString0() {
        return dest + "= (" + dest.getType() + ") " + input;
    }
    @Override
    public void printx86(X86Emitter emit) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
