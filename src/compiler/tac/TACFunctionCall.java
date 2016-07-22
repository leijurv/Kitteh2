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
        if (resultName != null) {
            result = context.getRequired(resultName);
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
