/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.x86.X86Const;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
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
        return params[1] + " = " + params[0];
    }
    public static X86TypedRegister sin(TypeNumerical type, String name) {
        for (X86Register r : X86Register.values()) {
            if (r.getRegister1(type, true).equals(name)) {//forgive me father, for i have sinned
                return r.getRegister(type);
            }
        }
        throw new RuntimeException();
    }
    @Override
    public void setVars() {
        if (paramNames[1].startsWith(X86Register.REGISTER_PREFIX)) {
            TypeNumerical type = X86Register.typeFromRegister(paramNames[1]);
            params[1] = sin(type, paramNames[1]);
            if (!params[1].x86().equals(paramNames[1])) {//verify
                throw new IllegalStateException(params[1].x86() + " " + paramNames[1]);
            }
        } else {
            params[1] = get(paramNames[1]);
        }
        TypeNumerical des = params[1].getType() instanceof TypeStruct ? null : (TypeNumerical) params[1].getType();
        try {//im tired ok? i know this is mal
            Double.parseDouble(paramNames[0]);
            params[0] = new X86Const(paramNames[0], des);
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
            if (params[0].getType().getSizeBytes() == params[1].getType().getSizeBytes() && params[1] instanceof X86TypedRegister) {
                //when returning a bool, it has to use the %al register which is technically a TypeInt8 not a TypeBoolean
                //so dont throw an error
                return;
            }
            throw new RuntimeException("lol " + params[0] + " " + params[1] + " " + params[0].getType() + " " + params[1].getType());
        }
    }
    @Override
    public void printx86(X86Emitter emit) {
        move(params[1], params[0], emit);
    }
    public static void move(X86Param dest, X86Param source, X86Emitter emit) {
        if (dest instanceof VarInfo && source instanceof VarInfo && !dest.getType().equals(source.getType())) {
            throw new UnsupportedCharsetException(source + " " + dest + " " + source.getType() + " " + dest.getType());
        }
        if (dest.getType() instanceof TypeStruct) {
            //oh god
            TypeNumerical t = new TypePointer<>(dest.getType());
            TACPointerDeref.moveStruct(((VarInfo) source).getStackLocation(), X86Register.BP.getRegister(t), ((VarInfo) dest).getStackLocation(), X86Register.BP.getRegister(t), ((TypeStruct) dest.getType()), emit);
            return;
        }
        TypeNumerical type = (TypeNumerical) dest.getType();
        if (type.getSizeBytes() != source.getType().getSizeBytes()) {
            if (source instanceof X86Const) {
                source = new X86Const(((X86Const) source).getValue(), type);
            } else {
                throw new InvalidOpenTypeException(source + " " + dest + " " + type + " " + source.getType());
            }
        }
        if (source instanceof X86Const || dest instanceof X86TypedRegister || source instanceof X86TypedRegister) {
            emit.uncheckedMove(source, dest);
        } else {
            if (source.getType() instanceof TypeFloat) {
                X86Param r = X86Register.XMM0.getRegister(type);
                emit.move(source, r);
                emit.move(r, dest);
                return;
            }
            X86Param alt = emit.alternative(source, (TypeNumerical) source.getType(), false);
            if (alt != null) {
                if (compiler.Compiler.verbose()) {
                    emit.addComment("SMART Replacing double move with more efficient one given previous move. Move was previously:");
                    emit.addComment(source.x86() + " -> C register -> " + dest.x86());
                    emit.addComment("Move is now");
                }
                emit.uncheckedMove(alt, dest);
            } else {
                X86Param r = X86Register.C.getRegister(type);
                emit.move(source, r);
                emit.move(r, dest);
            }
        }
    }
}
