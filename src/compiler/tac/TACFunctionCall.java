/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.Keyword;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.type.Type;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.x86.X86Emitter;
import compiler.x86.X86Format;
import compiler.x86.X86FunctionArg;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import java.nio.channels.CancelledKeyException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACFunctionCall extends TACStatement {
    String resultName;
    FunctionHeader header;
    VarInfo result;
    public TACFunctionCall(String result, FunctionHeader header, List<String> paramNames) {
        super(paramNames.toArray(new String[]{}));
        this.resultName = result;
        this.header = header;
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(paramNames);
    }
    @Override
    public List<String> modifiedVariables() {
        return resultName == null ? Arrays.asList() : Arrays.asList(resultName);
    }
    @Override
    public String toString0() {
        return (resultName == null ? "" : result + " = ") + "CALLFUNC " + header.name + "(" + Arrays.asList(params) + ")";
    }
    @Override
    public void setVars() {
        super.setVars();
        onContextKnown();
    }
    @Override
    public void onContextKnown() {
        if (resultName != null) {
            result = context.getRequired(resultName);
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        int argsSize = header.inputs().stream().mapToInt(Type::getSizeBytes).sum();
        int toSubtract = -context.getTotalStackSize() + argsSize + 10;//The +10 puts in a little more space than is strictly necesary, but it made it work in an unknown edge case I can't remember
        toSubtract /= 16;
        toSubtract++;
        toSubtract *= 16;//toSubtract needs to be a multiple of 16 for alignment reasons
        emit.addStatement("subq $" + toSubtract + ", %rsp");
        if (header.name.equals("malloc") || header.name.equals("calloc")) {
            emit.addStatement("xorq %rdi, %rdi");//clear out the top of the register
            emit.addStatement("movl " + params[0].x86() + ", %edi");
            //emit.addStatement("movq $1, %rsi");
            emit.addStatement("movslq " + params[0].x86() + ", %rsi");
            /*emit.addStatement("callq _malloc");
            emit.addStatement("addq $" + toSubtract + ", %rsp");
            return;*/
        }
        if (header.name.equals("free")) {
            emit.addStatement("movq " + params[0].x86() + ", %rdi");
        }
        if (header.name.equals(Keyword.PRINT.toString())) {
            //this is some 100% top quality code right here btw. it's not a hack i PROMISE
            if (params.length != 1 || !(params[0].getType() instanceof TypeNumerical)) {
                throw new CancelledKeyException();
            }
            emit.addStatement("leaq lldformatstring(%rip), %rdi");//lol rip
            emit.addStatement("movb $0, %al");//to be honest I don't know what this does, but when I run printf in C, the resulting ASM has this line beforehand. *shrug*. also if you remove it there's sometimes a segfault, which is FUN
            emit.addStatement("xorq %rdx, %rdx");
            TypeNumerical type = (TypeNumerical) params[0].getType();
            TACConst.move(X86Register.D.getRegister(type), params[0], emit);
            if (type.getSizeBytes() == 8) {
                emit.addStatement("movq %rdx, %rsi");//why esi? idk. again, i'm just copying gcc output asm
            } else {
                emit.addStatement("movs" + type.x86typesuffix() + "q " + X86Register.D.getRegister(type) + ", %rsi");
            }
            if (type instanceof TypePointer) {
                emit.addStatement("movq %rdx, %rdi");//comment out this line if you want print(ptr) to print out the pointer address instead of the asciz string at that pointer
            }
            emit.addStatement(X86Format.MAC ? "callq _printf" : "callq printf");//I understand this one at least XD
            emit.addStatement("addq $" + toSubtract + ", %rsp");
            return;
        }
        int stackLocation = 0;
        for (int i = 0; i < params.length; i++) {
            TypeNumerical type = (TypeNumerical) header.inputs().get(i);
            if (!type.equals(params[i].getType())) {
                if (header.name.equals("free") && params[i].getType() instanceof TypePointer) {
                    //this is fine
                } else {
                    throw new RuntimeException();
                }
            }
            X86Param dest = new X86FunctionArg(stackLocation, type);
            TACConst.move(dest, params[i], emit);
            //move onto stack pointer in increasing order
            stackLocation += type.getSizeBytes();
        }
        emit.addStatement("callq " + (X86Format.MAC ? "_" : "") + header.name);
        if (result != null) {
            TypeNumerical ret = (TypeNumerical) result.getType();
            emit.addStatement("mov" + ret.x86typesuffix() + " " + X86Register.A.getRegister(ret) + ", " + result.x86());
        }
        emit.addStatement("addq $" + toSubtract + ", %rsp");
    }
}
