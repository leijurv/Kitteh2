/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;

/**
 *
 * @author leijurv
 */
public class TACFunctionParam extends TACStatement {
    int paramInd;
    String paramName;
    VarInfo param;
    public TACFunctionParam(String paramName, int paramInd) {
        this.paramName = paramName;
        this.paramInd = paramInd;
    }
    @Override
    protected void onContextKnown() {
        param = context.get(paramName);
    }
    @Override
    public String toString0() {
        return "param " + paramInd + " = " + param;
    }
}
