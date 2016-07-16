/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
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
    ArrayList<VarInfo> argInfos;
    public TACFunctionCall(String result, String funcName, ArrayList<String> argNames) {
        this.result = result;
        this.funcName = funcName;
        this.argNames = argNames;
    }
    @Override
    public String toString0() {
        if (argInfos == null) {
            throw new IllegalStateException("I hope you're Dora because I'm going to need a Map to get out of this one");
        }
        return (result == null ? "" : result + " = ") + "CALLFUNC " + funcName + argInfos.stream().map(x -> x.toString()).collect(Collectors.joining(",", "(", ")"));
    }
    @Override
    public void onContextKnown() {
        argInfos = new ArrayList<>(argNames.size());
        for (int i = 0; i < argNames.size(); i++) {
            argInfos.add(context.get(argNames.get(i)));
        }
    }
}
