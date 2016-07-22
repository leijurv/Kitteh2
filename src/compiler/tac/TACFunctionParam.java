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
public class TACFunctionParam extends TACStatement {//todo parameter locations
    int paramInd;
    String paramName;
    VarInfo param;
    public TACFunctionParam(String paramName, int paramInd) {
        this.paramName = paramName;
        this.paramInd = paramInd;
    }
    @Override
    protected void onContextKnown() {
        param = context.getRequired(paramName);
    }
    @Override
    public String toString0() {
        return "param " + paramInd + " = " + param;
    }
    @Override
    public void printx86(X86Emitter emit) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
