/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context;
import compiler.command.CommandDefineFunction.FunctionHeader;
import compiler.type.Type;
import compiler.type.TypeFloat;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.util.Obfuscator;
import compiler.x86.X86Const;
import compiler.x86.X86Emitter;
import compiler.x86.X86Format;
import compiler.x86.X86Function;
import compiler.x86.X86FunctionArg;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import static compiler.x86.X86Register.*;
import compiler.x86.X86TempRegister;
import compiler.x86.X86TypedRegister;
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
        A,
        DI,
        SI,
        D,
        R10,
        R8,
        R9
    }));
    public static final List<X86Register> RETURN_REGISTERS = Collections.unmodifiableList(Arrays.asList(new X86Register[]{
        A,
        //NOT %rbx because apparently that's like not allowed and stuff (system v abi)
        C,
        D
    //yes you can only have 3 returns, sue me
    }));
    private final FunctionHeader header;
    private final X86Param[] result;
    private final Context context;
    public TACFunctionCall(Context context, FunctionHeader header, List<X86Param> paramNames, X86Param... result) {
        super(paramNames.toArray(new X86Param[paramNames.size()]));
        this.result = result;
        this.header = header;
        this.context = context;
    }
    @Override
    public List<X86Param> requiredVariables() {
        return Arrays.asList(params);
    }
    @Override
    public List<X86Param> modifiedVariables() {
        return result == null ? Arrays.asList() : Arrays.asList(result);
    }
    @Override
    public String toString() {
        return (result.length == 0 ? "" : Arrays.asList(result) + " = ") + "CALLFUNC " + header.name + "(" + Arrays.asList(params) + ")";
    }
    @Override
    public void replace(X86Param toReplace, X86Param infoWith) {
        for (int i = 0; i < result.length; i++) {
            if (result[i].equals(toReplace)) {
                result[i] = infoWith;
            }
        }
        try {
            super.replace(toReplace, infoWith);
        } catch (RuntimeException ex) {//TODO fix hack
        }
    }
    @Override
    public String toString(boolean printFull) {
        for (X86Param res : result) {
            if (res instanceof Context.VarInfo) {
                ((Context.VarInfo) res).getContext().printFull = printFull;
            }
        }
        return super.toString(printFull);
    }
    @Override
    public void regReplace(X86Param toReplace, X86Register replaceWith) {
        for (int i = 0; i < result.length; i++) {
            if (result[i].equals(toReplace)) {
                X86TypedRegister xtr = new X86TempRegister(replaceWith, (TypeNumerical) result[i].getType(), toReplace.toString());
                result[i] = xtr;
            }
        }
        super.regReplace(toReplace, replaceWith);
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
        if ("syscall".equals(header.name)) {//TODO maybe a TACSyscall separate from TACFunctionCall
            for (int i = 0; i < params.length; i++) {
                if (i >= SYSCALL_REGISTERS.size()) {
                    throw new IllegalStateException("Syscall only takes " + SYSCALL_REGISTERS.size() + " arguments, in registers " + SYSCALL_REGISTERS);
                }
                emit.move(params[i], SYSCALL_REGISTERS.get(i));
            }
            emit.addStatement("syscall");
            emit.clearRegisters(C, R11);
            printRet(emit);
            return;
        }
        if ("malloc".equals(header.name)) {
            X86Param t = DI.getRegister(new TypeInt64());
            if (params[0] instanceof X86Const) {
                emit.move(new X86Const(((X86Const) params[0]).getValue(), new TypeInt64()), t);
            } else {
                emit.cast(params[0], t);
            }
            stack = false;
        }
        if ("free".equals(header.name)) {
            emit.move(params[0], DI);
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
                emit.move(new X86Const("1", new TypeInt8()), A);//to be honest I don't know what this does, but when I run printf in C, the resulting ASM has this line beforehand. *shrug*. also if you remove it there's sometimes a segfault, which is FUN
                emit.addStatement("cvtss2sd " + params[0].x86() + ", %xmm0");
                emit.addStatement(X86Format.MAC ? "callq _printf" : "callq printf");//I understand this one at least XD
                emit.clearRegisters(A, C, D, SI, DI, R8, R9, R10, R11);
                return;
            }
        }
        int stackLocation = 0;
        for (int i = 0; stack && i < params.length; i++) {
            TypeNumerical type = (TypeNumerical) header.inputs().get(i);
            X86Param dest = new X86FunctionArg(stackLocation, type);
            if (!type.equals(params[i].getType())) {
                if (header.name.endsWith("__print")) {
                    if (params[i].getType().getSizeBytes() != 8) {
                        if (params[i] instanceof X86Const) {
                            emit.move(new X86Const(((X86Const) params[i]).getValue(), new TypeInt64()), dest);
                        } else {
                            emit.cast(params[i], A.getRegister(new TypeInt64()));
                            emit.move(A, dest);
                        }
                        stackLocation += 8;
                        continue;
                    }
                } else if (!"free".equals(header.name) || !(params[i].getType() instanceof TypePointer)) {//free of any pointer is ok
                    throw new IllegalStateException(this + " was " + params[i].getType() + " expected " + type);
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
        if (stack) {
            X86Function calling = emit.map().get(header.name);
            emit.clearRegisters(calling.allUsed());
            emit.clearRegisters(SP);//TODO i chose to make the optimization correct even when called functions modify their own arguments
            //should I disallow modifying arguments? or should I allow it, even though it can't let (%rsp) stay as it is between calls
            //TODO calling a function could modify heap locations relative to unmodified registers possibly
            //so maybe clear -x(%anyRegister) but not the registers themselves
        } else {
            emit.clearRegisters(A, C, D, SI, DI, R8, R9, R10, R11);
        }
        printRet(emit);
    }
    private void printRet(X86Emitter emit) {
        for (int i = 0; i < result.length; i++) {
            emit.move(RETURN_REGISTERS.get(i), result[i]);
        }
    }
}
