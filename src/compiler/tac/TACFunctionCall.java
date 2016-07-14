/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class TACFunctionCall extends TACStatement {
    String result;
    String funcName;
    ArrayList<String> argNames;
    public TACFunctionCall(String result, String funcName, ArrayList<String> argNames) {
        this.result = result;
        this.funcName = funcName;
        this.argNames = argNames;
    }
    @Override
    public String toString() {
        return (result == null ? "" : result + " = ") + "CALLFUNC " + funcName + argNames.stream().collect(Collectors.joining(",", "(", ")"));
    }
}
