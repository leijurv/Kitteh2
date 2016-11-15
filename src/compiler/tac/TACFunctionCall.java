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
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import java.nio.channels.CancelledKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author leijurv
 */
public class TACFunctionCall extends TACStatement {
    String resultName;
    FunctionHeader header;
    VarInfo result;
    public final ArrayList<String> paramNames;
    public final ArrayList<VarInfo> params;
    public TACFunctionCall(String result, FunctionHeader header, ArrayList<String> paramNames) {
        this.resultName = result;
        this.header = header;
        this.paramNames = paramNames;
        this.params = new ArrayList<>();
    }
    @Override
    public List<String> requiredVariables() {
        return paramNames;
    }
    @Override
    public String toString0() {
        ArrayList<String> p = IntStream.range(0, paramNames.size()).mapToObj(i -> params.get(i) == null ? paramNames.get(i) : params.get(i).toString()).collect(Collectors.toCollection(ArrayList::new));
        return (resultName == null ? "" : result + " = ") + "CALLFUNC " + header.name + "(" + p + ")";
    }
    @Override
    public void onContextKnown() {
        if (resultName != null) {
            result = context.getRequired(resultName);
        }
        if (!params.isEmpty()) {
            throw new ArithmeticException();
        }
        params.clear();
        params.addAll(paramNames.stream().map(name -> context.getRequired(name)).collect(Collectors.toList()));
    }
    @Override
    public void printx86(X86Emitter emit) {
        int argsSize = header.inputs().stream().mapToInt(type -> type.getSizeBytes()).sum();
        int toSubtract = -context.getTotalStackSize() + argsSize + 10;//The +10 puts in a little more space than is strictly necesary, but it made it work in an unknown edge case I can't remember
        toSubtract /= 16;
        toSubtract++;
        toSubtract *= 16;//toSubtract needs to be a multiple of 16 for alignment reasons
        emit.addStatement("subq $" + toSubtract + ", %rsp");
        if (header.name.equals("malloc")) {
            emit.addStatement("xorq %rdi, %rdi");//clear out the top of the register
            emit.addStatement("movl " + (params.get(0) == null ? "$" + paramNames.get(0) : params.get(0).x86()) + ", %edi");
            /*emit.addStatement("callq _malloc");
            emit.addStatement("addq $" + toSubtract + ", %rsp");
            return;*/
        }
        if (header.name.equals("KEYWORD" + Keyword.PRINT)) {
            //this is some 100% top quality code right here btw. it's not a hack i PROMISE
            if (params.size() != 1 || !(header.inputs().get(0) instanceof TypeNumerical)) {
                throw new CancelledKeyException();
            }
            TypeNumerical type = (TypeNumerical) (params.get(0) == null ? header.inputs().get(0) : params.get(0).getType());
            emit.addStatement("leaq lldformatstring(%rip), %rdi");//lol rip
            emit.addStatement("movb $0, %al");//to be honest I don't know what this does, but when I run printf in C, the resulting ASM has this line beforehand. *shrug*. also if you remove it there's sometimes a segfault, which is FUN
            emit.addStatement("xorq %rdx, %rdx");
            TACConst.move(X86Register.D.getRegister(type), null, params.get(0), paramNames.get(0), emit);
            if (type.getSizeBytes() == 8) {
                emit.addStatement("movq %rdx, %rsi");//why esi? idk. again, i'm just copying gcc output asm
            } else {
                emit.addStatement("movs" + type.x86typesuffix() + "q " + X86Register.D.getRegister(type) + ", %rsi");
            }
            if (type instanceof TypePointer) {
                emit.addStatement("movq %rdx, %rdi");//comment out this line if you want print(ptr) to print out the pointer address instead of the asciz string at that pointer
            }
            emit.addStatement("callq _printf");//I understand this one at least XD
            emit.addStatement("addq $" + toSubtract + ", %rsp");
            return;
        }
        int stackLocation = 0;
        for (int i = 0; i < params.size(); i++) {
            TypeNumerical type = (TypeNumerical) header.inputs().get(i);
            String dest = stackLocation + "(%rsp)";
            if (params.get(i) == null) {
                emit.addStatement("mov" + type.x86typesuffix() + " $" + paramNames.get(i) + ", " + dest);
            } else {
                emit.addStatement("mov" + type.x86typesuffix() + " " + params.get(i).x86() + ", " + X86Register.D.getRegister(type));
                emit.addStatement("mov" + type.x86typesuffix() + " " + X86Register.D.getRegister(type) + ", " + dest);
            }
            //move onto stack pointer in increasing order
            stackLocation += type.getSizeBytes();
        }
        emit.addStatement("callq _" + header.name);
        if (result != null) {
            TypeNumerical ret = (TypeNumerical) result.getType();
            emit.addStatement("mov" + ret.x86typesuffix() + " " + X86Register.A.getRegister(ret) + ", " + result.x86());
        }
        emit.addStatement("addq $" + toSubtract + ", %rsp");
    }
}
