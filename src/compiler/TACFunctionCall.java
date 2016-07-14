/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 *
 * @author leijurv
 */
public class TACFunctionCall extends TACStatement {
    String result;
    String funcName;
    public TACFunctionCall(String result, String funcName) {
        this.result = result;
        this.funcName = funcName;
    }
    @Override
    public String toString() {
        return result + " = " + funcName + "()";
    }
}
