/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.x86.X86Const;
import compiler.x86.X86Emitter;
import compiler.x86.X86Format;
import compiler.x86.X86FunctionArg;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import java.nio.channels.CancelledKeyException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACFunctionCall extends TACStatement {
    public static final List<X86Register> SYSCALL_REGISTERS = Collections.unmodifiableList(Arrays.asList(new X86Register[]{
        X86Register.A,
        X86Register.DI,
        X86Register.SI,
        X86Register.D, /*X86Register.R10,
        X86Register.R8,
        X86Register.R9*/}));
    public static final List<X86Register> RETURN_REGISTERS = Collections.unmodifiableList(Arrays.asList(new X86Register[]{
        X86Register.A,
        //NOT %rbx because apparently that's like not allowed and stuff (system v abi)
        X86Register.C,
        X86Register.D
    //yes you can only have 3 returns, sue me
    }));
    private final String[] resultName;
    private final FunctionHeader header;
    private X86Param[] result;
    public TACFunctionCall(FunctionHeader header, List<String> paramNames, String... result) {
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
        return (result.length == 0 ? "" : Arrays.asList(result) + " = ") + "CALLFUNC " + header.name + "(" + Arrays.asList(params) + ")";
    }
    @Override
    public void setVars() {
        super.setVars();
        onContextKnown();
    }
    @Override
    public void replace(String toReplace, String replaceWith, X86Param infoWith) {
        for (int i = 0; i < resultName.length; i++) {
            if (resultName[i].equals(toReplace)) {
                resultName[i] = replaceWith;
                result[i] = infoWith;
                return;
            }
        }
        super.replace(toReplace, replaceWith, infoWith);
    }
    @Override
    public void onContextKnown() {
        result = new X86Param[resultName.length];
        for (int i = 0; i < resultName.length; i++) {
            result[i] = context.getRequired(resultName[i]);
        }
    }
    public String calling() {
        return header.name;
    }
    public int argsSize() {
        return header.inputs().stream().mapToInt(Type::getSizeBytes).sum();
    }
    public int totalStack() {
        return context.getTotalStackSize();
    }
    @Override
    public void printx86(X86Emitter emit) {
        if (header.name.equals("syscall")) {
            for (int i = 0; i < params.length; i++) {
                TypeNumerical type = (TypeNumerical) params[i].getType();
                if (i >= SYSCALL_REGISTERS.size()) {
                    throw new IllegalStateException("Syscall only takes " + SYSCALL_REGISTERS.size() + " arguments, in registers " + SYSCALL_REGISTERS);
                }
                emit.addStatement("mov" + type.x86typesuffix() + " " + params[i].x86() + ", " + SYSCALL_REGISTERS.get(i).getRegister(type).x86());
            }
            emit.addStatement("syscall");
            printRet(emit);
            return;
        }
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
                return;
            }
        }
        int stackLocation = 0;
        for (int i = 0; i < params.length; i++) {
            TypeNumerical type = (TypeNumerical) header.inputs().get(i);
            if (!type.equals(params[i].getType())) {
                if (header.name.endsWith("__print")) {
                    if (params[i].getType().getSizeBytes() != 8) {
                        if (params[i] instanceof X86Const) {
                            emit.addStatement("movq " + params[i].x86() + ", %rax");
                        } else {
                            emit.addStatement("movs" + ((TypeNumerical) params[i].getType()).x86typesuffix() + "q " + params[i].x86() + ", %rax");
                        }
                        emit.addStatement("movq %rax, " + stackLocation + "(%rsp)");
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
        printRet(emit);
    }
    private void printRet(X86Emitter emit) {
        for (int i = 0; i < result.length; i++) {
            TypeNumerical ret = (TypeNumerical) result[i].getType();
            emit.addStatement("mov" + ret.x86typesuffix() + " " + RETURN_REGISTERS.get(i).getRegister(ret) + ", " + result[i].x86());
        }
    }
}
