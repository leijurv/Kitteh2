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
import compiler.type.TypePointer;
import compiler.type.TypeStruct;
import compiler.x86.X86Emitter;
import compiler.x86.X86Memory;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACPointerDeref extends TACStatement {
    private final int offset;
    public TACPointerDeref(X86Param deref, X86Param dest, int offset) {
        super(deref, dest);
        this.offset = offset;
        if (!((TypePointer) params[0].getType()).pointingTo().equals(params[1].getType())) {
            throw new IllegalStateException();
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
        //return "Dereference " + source + " into " + dest;
        return params[1] + " = *" + params[0] + (offset == 0 ? "" : "+" + offset);
    }
    @Override
    public void printx86(X86Emitter emit) {
        X86Param source = params[0];
        X86Param dest = params[1];
        X86TypedRegister loc = emit.putInRegister(source, X86Register.A);
        X86Param memLoc = new X86Memory(offset, loc.getRegister(), dest.getType());
        if (dest.getType() instanceof TypeNumerical) {
            if (dest instanceof X86TypedRegister) {
                emit.move(memLoc, dest);
            } else {
                X86Param alt1 = emit.alternative(memLoc, false);
                if (alt1 != null) {
                    if (compiler.Compiler.verbose()) {
                        emit.addComment("SMART Replacing deref with more efficient one given previous move.");
                        emit.addComment(memLoc.x86() + " is known to be equal to " + alt1.x86());
                        emit.addComment("Move is now");
                    }
                    emit.move(alt1, dest);
                } else {
                    emit.move(memLoc, X86Register.C);
                    emit.move(X86Register.C, dest);
                }
            }
        } else if (dest.getType() instanceof TypeStruct) {
            TypeStruct ts = (TypeStruct) dest.getType();
            moveStruct(offset, loc.getRegister(), ((VarInfo) dest).getStackLocation(), X86Register.BP, ts, emit);
        } else {
            throw new InvalidPathException("", "");
        }
    }
    public static void moveStruct(int sourceStackLocation, X86Register sourceRegister, int destLocation, X86Register destRegister, TypeStruct struct, X86Emitter emit) {
        int size = struct.getSizeBytes();
        //this is a really bad way to do this
        //still.
        //even though its a little smarter now
        int i = 0;
        for (TypeNumerical tn : new TypeNumerical[]{new TypeInt64(), new TypeInt32(), new TypeInt16(), new TypeInt8()}) {//ordered from largest to smallest for efficient moving
            while (i + tn.getSizeBytes() <= size) {
                X86Memory sr = new X86Memory(i + sourceStackLocation, sourceRegister, tn);
                X86Memory ds = new X86Memory(destLocation + i, destRegister, tn);
                TACConst.move(ds, sr, emit);
                i += tn.getSizeBytes();
            }
        }
    }
}
