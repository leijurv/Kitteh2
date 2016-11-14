/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.X86Emitter;
import compiler.X86Register;
import compiler.type.TypeInt16;
import compiler.type.TypeInt32;
import compiler.type.TypeInt64;
import compiler.type.TypeInt8;
import compiler.type.TypeNumerical;
import compiler.type.TypeStruct;

/**
 *
 * @author leijurv
 */
public class TACConst extends TACStatement {
    public String destName;
    public VarInfo dest;
    public String sourceName;
    public VarInfo source;
    public TACConst(String var, String val) {
        this.destName = var;
        this.sourceName = val;
    }
    @Override
    public String toString0() {
        return (dest == null ? destName : dest) + " = " + (source != null ? source : "CONST " + sourceName);
    }
    @Override
    public void onContextKnown() {
        dest = destName.startsWith(X86Register.REGISTER_PREFIX) ? null : context.getRequired(destName);
        try {//im tired ok? i know this is mal
            Double.parseDouble(sourceName);
        } catch (NumberFormatException ex) {
            if (!sourceName.startsWith("\"")) {
                source = context.get(sourceName);
                if (source == null) {
                    throw new IllegalStateException("I honestly can't think of a way that this could happen. but idk it might");
                }
            }
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        move(destName, dest, source, sourceName, emit);
    }
    public static void move(String destName, VarInfo dest, VarInfo source, String sourceName, X86Emitter emit) {
        if (dest != null && source != null && !dest.getType().equals(source.getType())) {
            throw new RuntimeException(source + " " + dest);
        }
        String destination = destName.startsWith(X86Register.REGISTER_PREFIX) ? destName : dest.x86();
        if (dest != null && dest.getType() instanceof TypeStruct) {
            //oh god
            TACPointerDeref.moveStruct(source.getStackLocation(), "%rbp", dest.getStackLocation(), ((TypeStruct) dest.getType()).struct, emit);
            return;
        }
        TypeNumerical type = destName.startsWith(X86Register.REGISTER_PREFIX) ? typeFromRegister(destName) : (TypeNumerical) dest.getType();
        if (source != null && type.getSizeBytes() != source.getType().getSizeBytes()) {
            throw new RuntimeException(source + " " + sourceName + " " + dest + " " + destName + " " + type + " " + source.getType());
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
                        throw new IllegalStateException();
                }
            case 3:
                switch (reg.charAt(0)) {
                    case 'e':
                        return new TypeInt32();
                    case 'r':
                        return new TypeInt64();
                    default:
                        throw new IllegalStateException();
                }
            default:
                throw new IllegalStateException();
        }
    }
}
