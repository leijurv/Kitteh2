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
public class TACFunctionCall extends TACStatement {
    String resultName;
    String funcName;
    VarInfo result;
    public TACFunctionCall(String result, String funcName) {
        this.resultName = result;
        this.funcName = funcName;
    }
    @Override
    public String toString0() {
        return result + " = CALLFUNC " + funcName;
    }
    @Override
    public void onContextKnown() {
        result = context.get(resultName);
    }
}
