/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
import compiler.type.TypeStruct;
import compiler.x86.X86Emitter;
import compiler.x86.X86Register;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.ClosedWatchServiceException;
import java.util.Arrays;
import java.util.FormatterClosedException;
import java.util.List;
import javax.management.openmbean.InvalidOpenTypeException;

/**
 *
 * @author leijurv
 */
public class TACConst extends TACStatement {
    public TACConst(String var, String val) {
        super(val, var);
    }
    @Override
    public List<String> requiredVariables() {
        return Arrays.asList(paramNames[0]);
    }
    @Override
    public List<String> modifiedVariables() {
        return Arrays.asList(paramNames[1]);
    }
    @Override
    public String toString0() {
        return (params[1] == null ? paramNames[1] : params[1]) + " = " + (params[0] != null ? params[0] : "CONST " + paramNames[0]);
    }
    @Override
    public void setVars() {
        params[1] = paramNames[1].startsWith(X86Register.REGISTER_PREFIX) ? null : get(paramNames[1]);
        try {//im tired ok? i know this is mal
            Double.parseDouble(paramNames[0]);
        } catch (NumberFormatException ex) {
            if (!paramNames[0].startsWith("\"")) {
                params[0] = get(paramNames[0]);
                if (params[0] == null) {
                    throw new IllegalStateException("I honestly can't think of a way that this could happen. but idk it might");
                }
            }
        }
    }
    @Override
    public void onContextKnown() {
        if (params[0] != null && params[1] != null && !params[0].getType().equals(params[1].getType())) {
            throw new RuntimeException("lol " + params[0] + " " + params[1] + " " + params[0].getType() + " " + params[1].getType());
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        move(paramNames[1], params[1], params[0], paramNames[0], emit);
    }
    public static void move(String destName, VarInfo dest, VarInfo source, String sourceName, X86Emitter emit) {
        if (dest != null && source != null && !dest.getType().equals(source.getType())) {
            throw new UnsupportedCharsetException(source + " " + dest + " " + source.getType() + " " + dest.getType());
        }
        String destination = destName.startsWith(X86Register.REGISTER_PREFIX) ? destName : dest.x86();
        if (dest != null && dest.getType() instanceof TypeStruct) {
            //oh god
            TACPointerDeref.moveStruct(source.getStackLocation(), "%rbp", dest.getStackLocation(), ((TypeStruct) dest.getType()).struct, emit);
            return;
        }
        TypeNumerical type = destName.startsWith(X86Register.REGISTER_PREFIX) ? typeFromRegister(destName) : (TypeNumerical) dest.getType();
        if (source != null && type.getSizeBytes() != source.getType().getSizeBytes()) {
            throw new InvalidOpenTypeException(source + " " + sourceName + " " + dest + " " + destName + " " + type + " " + source.getType());
        }
        if (source == null) {
            emit.addStatement("mov" + type.x86typesuffix() + " $" + sourceName + ", " + destination);
        } else if (destName.startsWith(X86Register.REGISTER_PREFIX)) {
            emit.addStatement("mov" + type.x86typesuffix() + " " + source.x86() + ", " + destName);
        } else {
            emit.addStatement("mov" + type.x86typesuffix() + " " + source.x86() + ", " + X86Register.C.getRegister(type));
            emit.addStatement("mov" + type.x86typesuffix() + " " + X86Register.C.getRegister(type) + ", " + destination);
        }
    }
    public static TypeNumerical typeFromRegister(String reg) {
        if (reg.startsWith(X86Register.REGISTER_PREFIX)) {
            return typeFromRegister(reg.substring(1));
        }
        switch (reg.length()) {
            case 2:
                switch (reg.charAt(1)) {
                    case 'l':
                        return new TypeInt8();
                    case 'x':
                        return new TypeInt16();
                    default:
                        throw new IllegalBlockingModeException();
                }
            case 3:
                switch (reg.charAt(0)) {
                    case 'e':
                        return new TypeInt32();
                    case 'r':
                        return new TypeInt64();
                    default:
                        throw new FormatterClosedException();
                }
            default:
                throw new ClosedWatchServiceException();
        }
    }
}
