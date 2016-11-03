/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.Keyword;
import compiler.X86Emitter;
import compiler.X86Register;
import compiler.type.TypeNumerical;
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
        return result + " = CALLFUNC " + funcName + "(" + params + ")";
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
        if (funcName.equals("KEYWORD" + Keyword.PRINT)) {
            //this is some 100% top quality code right here btw. it's not a hack i PROMISE
            if (params.size() != 1 || !(params.get(0).getType() instanceof TypeNumerical)) {
                throw new IllegalStateException();
            }
            TypeNumerical type = (TypeNumerical) (params.get(0).getType());
            emit.addStatement("subq $16, %rsp");//I really don't know why, but only 16 works here
            emit.addStatement("leaq	L_.str(%rip), %rdi");//lol rip
            emit.addStatement("movb $0, %al");//to be honest I don't know what this does, but when I run printf in C, the resulting ASM has this line beforehand. *shrug*. also if you remove it there's sometimes a segfault, which is FUN
            emit.addStatement("xorl %edx, %edx");
            emit.addStatement("mov" + type.x86typesuffix() + " " + params.get(0).x86() + ", " + X86Register.D.getRegister(type));
            emit.addStatement("movl %edx, %esi");//why esi? idk. again, i'm just copying gcc output asm
            emit.addStatement("callq _printf");//I understand this one at least XD
            emit.addStatement("addq $16, %rsp");
            return;
        }
        int argsSize = params.stream().map(varinfo -> varinfo.getType()).mapToInt(type -> type.getSizeBytes()).sum();
        int toSubtract = -context.getTotalStackSize() + argsSize + 10;//The +10 puts in a little more space than is strictly necesary, but it made it work in an unknown edge case I can't remember
        emit.addStatement("subq $" + toSubtract + ", %rsp");
        int stackLocation = 0;
        for (VarInfo param : params) {
            TypeNumerical type = (TypeNumerical) param.getType();
            String register = X86Register.D.getRegister(type);
            emit.addStatement("mov" + type.x86typesuffix() + " " + param.x86() + ", " + register);
            emit.addStatement("mov" + type.x86typesuffix() + " " + register + ", " + stackLocation + "(%rsp)");//move onto stack pointer in increasing order
            stackLocation += type.getSizeBytes();
        }
        emit.addStatement("callq _" + funcName);
        if (result != null) {
            TypeNumerical ret = (TypeNumerical) result.getType();
            emit.addStatement("mov" + ret.x86typesuffix() + " " + X86Register.A.getRegister(ret) + ", " + result.x86());
        }
        emit.addStatement("addq $" + toSubtract + ", %rsp");
    }
}
