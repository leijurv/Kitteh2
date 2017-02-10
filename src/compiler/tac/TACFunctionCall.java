/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.util.Obfuscator;
import compiler.asm.ASMConst;
import compiler.x86.X86Emitter;
import compiler.x86.X86Format;
import compiler.x86.X86FunctionArg;
import compiler.x86.X86Register;
import compiler.x86.X86TempRegister;
import compiler.x86.X86TypedRegister;
import java.nio.channels.CancelledKeyException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import compiler.asm.ASMParam;

/**
 *
 * @author leijurv
 */
public class TACFunctionCall extends TACStatement {
    public static final List<X86Register> SYSCALL_REGISTERS = Collections.unmodifiableList(Arrays.asList(new X86Register[]{
        X86Register.A,
        X86Register.DI,
        X86Register.SI,
        X86Register.D,
        X86Register.R10,
        X86Register.R8,
        X86Register.R9
    }));
    public static final List<X86Register> RETURN_REGISTERS = Collections.unmodifiableList(Arrays.asList(new X86Register[]{
        X86Register.A,
        //NOT %rbx because apparently that's like not allowed and stuff (system v abi)
        X86Register.C,
        X86Register.D
    //yes you can only have 3 returns, sue me
    }));
    private final String[] resultName;
    private final FunctionHeader header;
    private ASMParam[] result;
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
    public void replace(String toReplace, String replaceWith, ASMParam infoWith) {
        for (int i = 0; i < resultName.length; i++) {
            if (resultName[i].equals(toReplace)) {
                resultName[i] = replaceWith;
                result[i] = infoWith;
            }
        }
        try {
            super.replace(toReplace, replaceWith, infoWith);
        } catch (RuntimeException ex) {//TODO fix hack
        }
    }
    @Override
    public void regReplace(String toReplace, X86Register replaceWith) {
        for (int i = 0; i < resultName.length; i++) {
            if (resultName[i].equals(toReplace)) {
                X86TypedRegister xtr = new X86TempRegister(replaceWith, (TypeNumerical) result[i].getType(), toReplace);
                result[i] = xtr;
                resultName[i] = xtr.x86();
            }
        }
        super.regReplace(toReplace, replaceWith);
    }
    @Override
    public void onContextKnown() {
        result = new ASMParam[resultName.length];
        for (int i = 0; i < resultName.length; i++) {
            result[i] = context.getRequired(resultName[i]);
        }
    }
    public String calling() {
        return header.name;
    }
    public int numArgs() {
        return params.length;
    }
    public int stackSpaceRequired() {
        return header.inputs().stream().mapToInt(Type::getSizeBytes).sum() - context.getTotalStackSize();
    }
    @Override
    public void printx86(X86Emitter emit) {
        boolean stack = true;
        if (header.name.equals("syscall")) {//TODO maybe a TACSyscall separate from TACFunctionCall
            for (int i = 0; i < params.length; i++) {
                if (i >= SYSCALL_REGISTERS.size()) {
                    throw new IllegalStateException("Syscall only takes " + SYSCALL_REGISTERS.size() + " arguments, in registers " + SYSCALL_REGISTERS);
                }
                emit.move(params[i], SYSCALL_REGISTERS.get(i));
            }
            emit.addStatement("syscall");
            printRet(emit);
            return;
        }
        if (header.name.equals("malloc")) {
            ASMParam t = X86Register.DI.getRegister(new TypeInt64());
            if (params[0] instanceof ASMConst) {
                emit.moveStr(params[0].x86(), t);
            } else {
                emit.cast(params[0], t);
            }
            stack = false;
        }
        if (header.name.equals("free")) {
            emit.move(params[0], X86Register.DI);
            stack = false;
        }
        if (header.name.endsWith("__print")) {
            //this is some 100% top quality code right here btw. it's not a hack i PROMISE
            if (params.length != 1 || !(params[0].getType() instanceof TypeNumerical)) {
                throw new CancelledKeyException();
            }
            TypeNumerical type = (TypeNumerical) params[0].getType();
            if (type instanceof TypeFloat) {
                emit.addStatement("leaq floatformatstring(%rip), %rdi");//lol rip
                emit.move(new ASMConst("1", new TypeInt8()), X86Register.A);//to be honest I don't know what this does, but when I run printf in C, the resulting ASM has this line beforehand. *shrug*. also if you remove it there's sometimes a segfault, which is FUN
                emit.addStatement("cvtss2sd " + params[0].x86() + ", %xmm0");
                emit.addStatement(X86Format.MAC ? "callq _printf" : "callq printf");//I understand this one at least XD
                return;
            }
        }
        int stackLocation = 0;
        for (int i = 0; stack && i < params.length; i++) {
            TypeNumerical type = (TypeNumerical) header.inputs().get(i);
            ASMParam dest = new X86FunctionArg(stackLocation, type);
            if (!type.equals(params[i].getType())) {
                if (header.name.endsWith("__print")) {
                    if (params[i].getType().getSizeBytes() != 8) {
                        if (params[i] instanceof ASMConst) {
                            emit.move(new ASMConst(((ASMConst) params[i]).getValue(), new TypeInt64()), X86Register.A);
                        } else {
                            emit.cast(params[i], X86Register.A.getRegister(new TypeInt64()));
                        }
                        emit.move(X86Register.A, dest);
                        stackLocation += 8;
                        continue;
                    }
                } else if (header.name.equals("free") && params[i].getType() instanceof TypePointer) {
                    //this is fine
                } else {
                    throw new RuntimeException(this + " was " + params[i].getType() + " expected " + type);
                }
            }
            TACConst.move(dest, params[i], emit);
            //move onto stack pointer in increasing order
            stackLocation += type.getSizeBytes();
        }
        String name = header.name;
        if (!compiler.Main.ALLOW_CLI || compiler.Compiler.obfuscate()) {
            name = Obfuscator.obfuscate(name);
        }
        emit.addStatement("callq " + (X86Format.MAC ? "_" : "") + name);
        printRet(emit);
    }
    private void printRet(X86Emitter emit) {
        for (int i = 0; i < result.length; i++) {
            emit.move(RETURN_REGISTERS.get(i), result[i]);
        }
    }
}
