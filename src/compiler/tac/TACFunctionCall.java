/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.X86Emitter;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class TACFunctionCall extends TACStatement {
    String resultName;
    String funcName;
    VarInfo result;
    ArrayList<String> paramNames;
    ArrayList<VarInfo> params;
    public TACFunctionCall(String result, String funcName, ArrayList<String> paramNames) {
        this.resultName = result;
        this.funcName = funcName;
        this.paramNames = paramNames;
    }
    @Override
    public String toString0() {
        return result + " = CALLFUNC " + funcName + "(" + paramNames + ")";
    }
    @Override
    public void onContextKnown() {
        if (resultName != null) {
            result = context.getRequired(resultName);
        }
        params = paramNames.stream().map(name -> context.getRequired(name)).collect(Collectors.toCollection(ArrayList::new));
    }
    @Override
    public void printx86(X86Emitter emit) {
        int argsSize = params.stream().map(varinfo -> varinfo.getType()).mapToInt(type -> type.getSizeBytes()).sum();
        int toSubtract = -context.getTotalStackSize() + argsSize + 10;//why not
        emit.addStatement("subq $" + toSubtract + ", %rsp");
        int stackLocation = 0;
        for (VarInfo param : params) {
            emit.addStatement("movl " + param.x86() + ", %edx");
            emit.addStatement("movl %edx, " + stackLocation + "(%rsp)");//move onto stack pointer in increasing order
            stackLocation += param.getType().getSizeBytes();
        }
        emit.addStatement("callq _" + funcName);
        emit.addStatement("movl %eax, " + result.x86());
        emit.addStatement("addq $" + toSubtract + ", %rsp");
    }
}
