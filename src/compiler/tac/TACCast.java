/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.tac;
import compiler.type.TypeFloat;
import compiler.type.TypeInt32;
import compiler.type.TypeNumerical;
import compiler.x86.X86Emitter;
import compiler.x86.X86Param;
import compiler.x86.X86Register;
import compiler.x86.X86TypedRegister;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class TACCast extends TACStatement {
    public TACCast(X86Param input, X86Param dest) {
        super(input, dest);
        TypeNumerical inp = (TypeNumerical) input.getType();
        TypeNumerical out = (TypeNumerical) dest.getType();
        if (inp.equals(out)) {
            //this is a really annoying exception
            //if you do long a=(long)0 for example, that's a long being casted to a long because of integral literal type inference
            //when static values is OFF, this is an exception, because redundant casts aren't removed, and the code gets to here
            //so when static values are OFF this shouldn't throw an exception because its normal ish
            //but we C A N T E V E N T E L L if static values are on or off
            throw new IllegalStateException(input + " " + dest + " " + inp.getSizeBytes() + " " + out.getSizeBytes() + " " + inp);
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
        return params[1] + " = (" + params[1].getType() + ") " + params[0];
    }
    @Override
    public void printx86(X86Emitter emit) {
        cast(params[0], params[1], emit);
    }
    public static void cast(X86Param input, X86Param dest, X86Emitter emit) {
        TypeNumerical inp = (TypeNumerical) input.getType();
        TypeNumerical out = (TypeNumerical) dest.getType();
        if (out instanceof TypeFloat) {
            if (!(inp instanceof TypeInt32)) {
                throw new IllegalStateException("noplease");
            }
            X86Param aoeu = X86Register.XMM0.getRegister(new TypeFloat());
            emit.addStatement("cvtsi2ssl " + input.x86() + ", " + aoeu.x86());//kill me
            emit.move(aoeu, dest);
            return;
        }
        X86Param dst;
        if (dest instanceof X86TypedRegister) {
            dst = dest;
        } else {
            dst = X86Register.C.getRegister(out);
        }
        if (inp.getSizeBytes() >= out.getSizeBytes()) {
            //down cast
            if (inp.equals(out)) {
                throw new IllegalStateException("literally impossible");
            }
            if (dest instanceof X86TypedRegister) {
                emit.move(input, ((X86TypedRegister) dest).getRegister().getRegister(inp));
            } else {
                if (input instanceof X86TypedRegister) {
                    emit.move(((X86TypedRegister) input).getRegister().getRegister(out), dest);
                    return;
                } else {
                    emit.move(input, X86Register.C.getRegister(inp));
                }
            }
        } else {
            //up cast
            emit.cast(input, dst);
        }
        if (!(dest instanceof X86TypedRegister)) {
            emit.uncheckedMove(dst, dest);
        }
    }
}
