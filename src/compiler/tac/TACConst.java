/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.Context.VarInfo;
import compiler.type.TypeFloat;
import compiler.type.TypeNumerical;
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
    public TACConst(X86Param var, X86Param val) {//TODO this order switch is ACTUALLY ridiculous
        super(new X86Param[]{val, var});
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
    public List<X86Param> requiredVariables() {
        return Arrays.asList(params[0]);
    }
    @Override
    public List<X86Param> modifiedVariables() {
        return Arrays.asList(params[1]);
    }
    @Override
    public String toString() {
        return params[1] + " = " + params[0];
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
            TACPointerDeref.moveStruct(((VarInfo) source).getStackLocation(), X86Register.BP, ((VarInfo) dest).getStackLocation(), X86Register.BP, ((TypeStruct) dest.getType()), emit);
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
            if (emit.redundant(source, dest)) {
                if (compiler.Compiler.verbose()) {
                    emit.addComment("not doing move from " + source + " to " + dest);
                }
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
