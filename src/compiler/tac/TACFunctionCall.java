/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.type.Type;
import compiler.type.TypeFloat;
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
    private final String resultName;
    private final FunctionHeader header;
    private X86Param result;
    public TACFunctionCall(String result, FunctionHeader header, List<String> paramNames) {
        super(paramNames.toArray(new String[paramNames.size()]));
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
    public String calling() {
        return header.name;
    }
    @Override
    public void printx86(X86Emitter emit) {
        int argsSize = header.inputs().stream().mapToInt(Type::getSizeBytes).sum();
        int toSubtract = -context.getTotalStackSize() + argsSize + 10;//The +10 puts in a little more space than is strictly necesary, but it made it work in an unknown edge case I can't remember
        toSubtract /= 16;
        toSubtract++;
        toSubtract *= 16;//toSubtract needs to be a multiple of 16 for alignment reasons
        if (header.name.equals("syscall")) {
            X86Register[] registers = {X86Register.A, X86Register.DI, X86Register.SI, X86Register.D, X86Register.R10, X86Register.R8, X86Register.R9};
            for (int i = 0; i < params.length; i++) {
                TypeNumerical type = (TypeNumerical) params[i].getType();
                String lol = params[i].x86();
                emit.addStatement("mov" + type.x86typesuffix() + " " + lol + ", " + registers[i].getRegister(type).x86());
            }
            emit.addStatement("syscall");
            if (result != null) {
                TypeNumerical ret = (TypeNumerical) result.getType();
                emit.addStatement("mov" + ret.x86typesuffix() + " " + X86Register.A.getRegister(ret) + ", " + result.x86());
            }
            return;
        }
        emit.addStatement("subq $" + toSubtract + ", %rsp");
        if (header.name.equals("malloc")) {
            emit.addStatement("xorq %rdi, %rdi");//clear out the top of the register
            emit.addStatement("movl " + params[0].x86() + ", %edi");
            /*emit.addStatement("callq _malloc");
            emit.addStatement("addq $" + toSubtract + ", %rsp");
            return;*/
        }
        if (header.name.equals("free")) {
            emit.addStatement("movq " + params[0].x86() + ", %rdi");
        }
        if (header.name.endsWith("__print")) {
            //this is some 100% top quality code right here btw. it's not a hack i PROMISE
            if (params.length != 1 || !(params[0].getType() instanceof TypeNumerical)) {
                throw new CancelledKeyException();
            }
            TypeNumerical type = (TypeNumerical) params[0].getType();
            if (type instanceof TypeFloat) {
                emit.addStatement("leaq floatformatstring(%rip), %rdi");//lol rip
                emit.addStatement("movb $1, %al");//to be honest I don't know what this does, but when I run printf in C, the resulting ASM has this line beforehand. *shrug*. also if you remove it there's sometimes a segfault, which is FUN
                emit.addStatement("cvtss2sd " + params[0].x86() + ", %xmm0");
                emit.addStatement(X86Format.MAC ? "callq _printf" : "callq printf");//I understand this one at least XD
                emit.addStatement("addq $" + toSubtract + ", %rsp");
                return;
            }
        }
        int stackLocation = 0;
        for (int i = 0; i < params.length; i++) {
            TypeNumerical type = (TypeNumerical) header.inputs().get(i);
            if (!type.equals(params[i].getType())) {
                if (header.name.endsWith("__print")) {
                    if (params[i].getType().getSizeBytes() != 8) {
                        if (params[i] instanceof VarInfo) {
                            emit.addStatement("movs" + ((TypeNumerical) params[i].getType()).x86typesuffix() + "q " + params[i].x86() + ", %rsi");
                        } else {
                            emit.addStatement("movq " + params[i].x86() + ", %rsi");
                        }
                        emit.addStatement("movq %rsi, " + stackLocation + "(%rsp)");
                        stackLocation += 8;
                        continue;
                    }
                } else if (header.name.equals("free") && params[i].getType() instanceof TypePointer) {
                    //this is fine
                } else {
                    throw new RuntimeException(this + " was " + params[i].getType() + " expected " + type);
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
